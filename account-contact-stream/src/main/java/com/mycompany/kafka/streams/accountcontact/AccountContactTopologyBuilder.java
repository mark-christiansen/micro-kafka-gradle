package com.mycompany.kafka.streams.accountcontact;

import com.mycompany.kafka.model.canonical.AccountContact;
import com.mycompany.kafka.model.canonical.Contact;
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

import java.util.Map;
import java.util.Properties;

@Factory
public class AccountContactTopologyBuilder extends AbstractTopologyBuilder {

    private static final String ACCOUNT_CONTACT_SOURCE = "com.mycompany.kafka.model.source.AccountContact";
    private static final String ACCOUNT_CONTACT_TOPIC = "account.contact.topic";
    private static final String CONTACT_TOPIC = "contact.topic";
    private static final String CONTACT_ROLE_TOPIC = "contact.role.topic";
    private static final String OUTPUT_TOPIC = "output.topic";
    private static final String ERROR_TOPIC = "failure.output.topic";
    private static final String IN_MEMORY_STATE_STORES = "in.memory.state.stores";
    private static final String CONTACT_STATE_STORE = "contact";
    private static final String CONTACT_ROLE_STATE_STORE = "contact-role";
    private static final String ACCOUNT_CONTACT_STATE_STORE = "account-contact";
    private static final String ACCOUNT_CONTACT_CONTACT_STATE_STORE = "account-contact-contact";

    private final String accountContactInputTopic;
    private final String contactInputTopic;
    private final String contactRoleInputTopic;
    private final String outputTopic;
    private final String failureOutputTopic;
    private final com.mycompany.kafka.streams.serdes.Serdes serdes;
    private final Producer<Long, GenericRecord> errorHandler;
    private final StateStoreMaterializer<Long, GenericRecord> sourceStateStoreMaterializer;
    private final StateStoreMaterializer<String, AccountContact> accountContactStateStoreMaterializer;
    private final StateStoreMaterializer<String, Contact> contactStateStoreMaterializer;

    public AccountContactTopologyBuilder(@Named("applicationProperties") Map<String, Object> props,
                                         Producer<Long, GenericRecord> errorHandler,
                                         com.mycompany.kafka.streams.serdes.Serdes serdes) {

        this.accountContactInputTopic = (String) props.get(ACCOUNT_CONTACT_TOPIC);
        this.contactInputTopic = (String) props.get(CONTACT_TOPIC);
        this.contactRoleInputTopic = (String) props.get(CONTACT_ROLE_TOPIC);
        this.outputTopic = (String) props.get(OUTPUT_TOPIC);
        this.failureOutputTopic = (String) props.get(ERROR_TOPIC);
        this.errorHandler = errorHandler;
        this.serdes = serdes;
        boolean inMemoryStateStores = Boolean.parseBoolean((String) props.get(IN_MEMORY_STATE_STORES));
        this.sourceStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
        this.accountContactStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
        this.contactStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
    }

    public Topology build(@Named("streamProperties") Properties streamProperties) {

        final StreamsBuilder builder = new StreamsBuilder();

        // Contact Role GlobalKTable
        GlobalKTable<Long, GenericRecord> contactRoleTable = builder.globalTable(contactRoleInputTopic,
                this.sourceStateStoreMaterializer.getStateStore(CONTACT_ROLE_STATE_STORE,
                        org.apache.kafka.common.serialization.Serdes.Long(),
                        serdes.createGenericSerde(false)));

        // Contact Canonical Topic
        KTable<String, Contact> contactTable = builder.stream(contactInputTopic,
                        Consumed.with(Serdes.String(), serdes.createContactSerde()))
                .toTable(this.contactStateStoreMaterializer.getStateStore(CONTACT_STATE_STORE,
                        Serdes.String(), serdes.createContactSerde()));

        // Account Contact Source Topic
        KTable<String, AccountContact> accountContactTable = builder.stream(accountContactInputTopic,
                        Consumed.with(Serdes.Long(), serdes.createGenericSerde(false)))
                // filter out just the contact address records
                .filter(((key, value) -> ACCOUNT_CONTACT_SOURCE.equalsIgnoreCase(value.getSchema().getFullName())))
                // map GenericRecord to Account w/ Contact
                .map((key, value) -> {

                    String id = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT_CONTACT.toString(),
                            (Long) value.get("id")).toString();
                    String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                            (Long) value.get("accountId")).toString();
                    String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                            (Long) value.get("contactId")).toString();

                    AccountContact accountContact = new AccountContact();
                    accountContact.setId(id);
                    accountContact.setAccountId(accountId);
                    Contact contact = new Contact();
                    contact.setId(contactId);
                    accountContact.setContact(contact);
                    if (value.get("contactRoleId") != null) {
                        accountContact.setContactRole(String.valueOf(value.get("contactRoleId")));
                    }
                    accountContact.setCreated(asInstant(value.get("created")));
                    accountContact.setUpdated(asInstant(value.get("updated")));
                    return new KeyValue<>(id, accountContact);
                })
                // join Contact Role GenericRecord to AccountContact
                .leftJoin(contactRoleTable,
                        (key, value) -> value.getContactRole() != null ? Long.parseLong(value.getContactRole()) : null,
                        (value1, value2) -> {
                            value1.setContactRole(asString(value2.get("code")));
                            return value1;
                        })
                .toTable(this.accountContactStateStoreMaterializer.getStateStore(ACCOUNT_CONTACT_STATE_STORE,
                        Serdes.String(), serdes.createAccountContactSerde()));

        accountContactTable.leftJoin(contactTable,
                        accountContact -> accountContact.getContact().getId(),
                        (accountContact, contact) -> {
                            accountContact.setContact(contact);
                            return accountContact;
                        }, TableJoined.as(ACCOUNT_CONTACT_CONTACT_STATE_STORE))
                .toStream()
                .to(outputTopic, Produced.with(Serdes.String(), serdes.createAccountContactSerde()));

        return builder.build(streamProperties);
    }
}