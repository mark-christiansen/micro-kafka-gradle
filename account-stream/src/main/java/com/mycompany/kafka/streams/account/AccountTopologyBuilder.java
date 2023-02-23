package com.mycompany.kafka.streams.account;

import com.mycompany.kafka.model.canonical.Account;
import com.mycompany.kafka.model.canonical.AccountContact;
import com.mycompany.kafka.model.source.Source;
import com.mycompany.kafka.streams.CanonicalId;
import com.mycompany.kafka.streams.StateStoreMaterializer;
import com.mycompany.kafka.streams.topology.AbstractTopologyBuilder;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Factory
public class AccountTopologyBuilder extends AbstractTopologyBuilder {

    private static final String ACCOUNT_SOURCE = "com.mycompany.kafka.model.source.Account";
    private static final String ACCOUNT_TOPIC = "account.topic";
    private static final String ACCOUNT_CONTACT_TOPIC = "account.contact.topic";
    private static final String OUTPUT_TOPIC = "output.topic";
    private static final String ERROR_TOPIC = "failure.output.topic";
    private static final String IN_MEMORY_STATE_STORES = "in.memory.state.stores";
    private static final String ACCOUNT_STATE_STORE = "account";

    private final String accountInputTopic;
    private final String accountContactInputTopic;
    private final String outputTopic;
    private final String failureOutputTopic;
    private final com.mycompany.kafka.streams.serdes.Serdes serdes;
    private final Producer<Long, GenericRecord> errorHandler;
    private final StateStoreMaterializer<String, Account> accountStateStoreMaterializer;

    public AccountTopologyBuilder(@Named("applicationProperties") Map<String, Object> props,
                                  Producer<Long, GenericRecord> errorHandler,
                                  com.mycompany.kafka.streams.serdes.Serdes serdes) {

        this.accountInputTopic = (String) props.get(ACCOUNT_TOPIC);
        this.accountContactInputTopic = (String) props.get(ACCOUNT_CONTACT_TOPIC);
        this.outputTopic = (String) props.get(OUTPUT_TOPIC);
        this.failureOutputTopic = (String) props.get(ERROR_TOPIC);
        this.errorHandler = errorHandler;
        this.serdes = serdes;
        boolean inMemoryStateStores = Boolean.parseBoolean((String) props.get(IN_MEMORY_STATE_STORES));
        this.accountStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
    }

    public Topology build(@Named("streamProperties") Properties streamProperties) {

        final StreamsBuilder builder = new StreamsBuilder();

        // Account Source Topic
        KGroupedStream<String, Account> accountStream = builder.stream(accountInputTopic,
                        Consumed.with(Serdes.Long(), serdes.createGenericSerde(false)))
                // filter out just the account records
                .filter(((key, value) -> ACCOUNT_SOURCE.equalsIgnoreCase(value.getSchema().getFullName())))
                // map GenericRecord to Account
                .map((key, value) -> {

                    String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                            (Long) value.get("id")).toString();

                    Account account = new Account();
                    account.setId(accountId);
                    account.setAccountNumber(asString(value.get("accountNumber")));
                    account.setAccountType(asString(value.get("accountType")));
                    account.setCreated(asInstant(value.get("created")));
                    account.setUpdated(asInstant(value.get("updated")));
                    return new KeyValue<>(accountId, account);
                })
                // create grouped stream to aggregate below - this will not cause repartition
                .groupByKey(Grouped.with("account-group-by-key", Serdes.String(), serdes.createAccountSerde()));

        // Account Contact Canonical Topic
        KGroupedStream<String, AccountContact> accountContactStream = builder.stream(accountContactInputTopic,
                        Consumed.with(Serdes.String(), serdes.createAccountContactSerde()))
                // create grouped stream to aggregate below - this will cause repartition because aggregating by account ID
                .groupBy((key, value) -> value.getAccountId(),
                        Grouped.with("account-contact-group-by-account-id", Serdes.String(), serdes.createAccountContactSerde()));

        // combine the account data into a account aggregate
        CogroupedKStream<String, Account> cogroupAccountStream = accountStream.cogroup((key, value, aggregate) -> {
            aggregate.setId(value.getId());
            aggregate.setAccountNumber(value.getAccountNumber());
            aggregate.setAccountType(value.getAccountType());
            aggregate.setCreated(value.getCreated());
            aggregate.setUpdated(value.getUpdated());
            return aggregate;
        });

        CogroupedKStream<String, Account> cogroupAccountContactStream = cogroupAccountStream.cogroup(accountContactStream, (key, value, aggregate) -> {

            // if the account contact arrives before the account, the account object will be empty so populate the ID here
            if (aggregate.getId() == null) {
                aggregate.setId(value.getAccountId());
                aggregate.setCreated(value.getCreated());
            }

            // initialize the account contact list on the account if it hasn't been done
            List<AccountContact> contacts = aggregate.getContacts();
            if (contacts == null) {
                contacts = new ArrayList<>();
                aggregate.setContacts(contacts);
            }

            // see if this account contact has already been attached to this account
            AccountContact existingContact = null;
            for (AccountContact contact : contacts) {
                if (contact.getId().equals(value.getId())) {
                    existingContact = contact;
                    break;
                }
            }

            // create new account contact if it wasn't found above and update ID fields
            if (existingContact == null) {
                existingContact = new AccountContact();
                existingContact.setId(value.getId());
                existingContact.setAccountId(value.getAccountId());
                existingContact.setCreated(value.getCreated());
                contacts.add(existingContact);
            }

            // update all account contact fields from the incoming contact
            existingContact.setContactRole(value.getContactRole());
            existingContact.setContact(value.getContact());
            existingContact.setUpdated(value.getUpdated());
            return aggregate;
        });

        // create account table from combined account and account contact info above
        KTable<String, Account> accountTable = cogroupAccountContactStream.aggregate(Account::new,
                accountStateStoreMaterializer.getStateStore(ACCOUNT_STATE_STORE, Serdes.String(), serdes.createAccountSerde()));
        // output account table to output topic
        accountTable.toStream()
                // don't send accounts to output topic that only have account contacts on them
                .filter((key, value) -> value.getAccountNumber() != null)
                .to(outputTopic, Produced.with(Serdes.String(), serdes.createAccountSerde()));

        return builder.build(streamProperties);
    }
}