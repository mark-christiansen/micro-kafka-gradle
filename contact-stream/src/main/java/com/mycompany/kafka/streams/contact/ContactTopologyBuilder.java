package com.mycompany.kafka.streams.contact;

import com.mycompany.kafka.model.canonical.Contact;
import com.mycompany.kafka.model.canonical.ContactAddress;
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
public class ContactTopologyBuilder extends AbstractTopologyBuilder {

    private static final String CONTACT_SOURCE = "com.mycompany.kafka.model.source.Contact";
    private static final String CONTACT_TOPIC = "contact.topic";
    private static final String CONTACT_ADDRESS_TOPIC = "contact.address.topic";
    private static final String OUTPUT_TOPIC = "output.topic";
    private static final String ERROR_TOPIC = "failure.output.topic";
    private static final String IN_MEMORY_STATE_STORES = "in.memory.state.stores";
    private static final String CONTACT_STATE_STORE = "contact";

    private final String contactInputTopic;
    private final String contactAddressInputTopic;
    private final String outputTopic;
    private final String failureOutputTopic;
    private final com.mycompany.kafka.streams.serdes.Serdes serdes;
    private final Producer<Long, GenericRecord> errorHandler;
    private final StateStoreMaterializer<String, Contact> contactStateStoreMaterializer;

    public ContactTopologyBuilder(@Named("applicationProperties") Map<String, Object> props,
                                  Producer<Long, GenericRecord> errorHandler,
                                  com.mycompany.kafka.streams.serdes.Serdes serdes) {

        this.contactInputTopic = (String) props.get(CONTACT_TOPIC);
        this.contactAddressInputTopic = (String) props.get(CONTACT_ADDRESS_TOPIC);
        this.outputTopic = (String) props.get(OUTPUT_TOPIC);
        this.failureOutputTopic = (String) props.get(ERROR_TOPIC);
        this.errorHandler = errorHandler;
        this.serdes = serdes;
        boolean inMemoryStateStores = Boolean.parseBoolean((String) props.get(IN_MEMORY_STATE_STORES));
        this.contactStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
    }

    public Topology build(@Named("streamProperties") Properties streamProperties) {

        final StreamsBuilder builder = new StreamsBuilder();

        // create contact address stream from canonical contact address stream topic
        KGroupedStream<String, ContactAddress> addressStream = builder.stream(contactAddressInputTopic,
                        Consumed.with(Serdes.String(), serdes.createContactAddressSerde()))
                // create grouped stream to aggregate below - this will cause repartition because aggregating by contact ID
                .groupBy((key, value) -> value.getContactId(),
                        Grouped.with("contact-address-group-by-contact-id", Serdes.String(), serdes.createContactAddressSerde()));

        // create contact stream from the source contact topic
        KGroupedStream<String, Contact> contactStream = builder.stream(contactInputTopic, Consumed.with(Serdes.Long(), serdes.createGenericSerde(false))
                )
                // filter out just the contact records
                .filter(((key, value) -> CONTACT_SOURCE.equalsIgnoreCase(value.getSchema().getFullName())))
                // map GenericRecord to Contact
                .map((key, value) -> {

                    // transform incoming GenericRecord into a canonical Contact
                    long sourceId = (Long) value.get("id");
                    String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                            sourceId).toString();

                    Contact contact = new Contact();
                    contact.setId(contactId);
                    contact.setFirstName(asString(value.get("firstName")));
                    contact.setMiddleName(asString(value.get("middleName")));
                    contact.setLastName(asString(value.get("lastName")));
                    contact.setSocialSecurityNumber(asString(value.get("ssn")));
                    contact.setBirthDate(asLocalDate(value.get("birthDate")));
                    contact.setCreated(asInstant(value.get("created")));
                    contact.setUpdated(asInstant(value.get("updated")));
                    return new KeyValue<>(contactId, contact);

                })
                // create grouped stream to aggregate below - this will not cause repartition
                .groupByKey(Grouped.with("contact-group-by-key", Serdes.String(), serdes.createContactSerde()));

        // combine the contact data into a contact aggregate
        CogroupedKStream<String, Contact> cogroupContactStream = contactStream.cogroup((key, value, aggregate) -> {
            aggregate.setId(value.getId());
            aggregate.setFirstName(value.getFirstName());
            aggregate.setMiddleName(value.getMiddleName());
            aggregate.setLastName(value.getLastName());
            aggregate.setSocialSecurityNumber(value.getSocialSecurityNumber());
            aggregate.setBirthDate(value.getBirthDate());
            aggregate.setCreated(value.getCreated());
            aggregate.setUpdated(value.getUpdated());
            return aggregate;
        });

        // combine the contact address data into a contact aggregate
        CogroupedKStream<String, Contact> cogroupContactAddressStream = cogroupContactStream.cogroup(addressStream, (key, value, aggregate) -> {

            // if the address arrives before the contact, the contact object will be empty so populate the ID here
            if (aggregate.getId() == null) {
                aggregate.setId(value.getContactId());
                aggregate.setCreated(value.getCreated());
            }

            // initialize the address list on the contact if it hasn't been done
            List<ContactAddress> addresses = aggregate.getAddresses();
            if (addresses == null) {
                addresses = new ArrayList<>();
                aggregate.setAddresses(addresses);
            }

            // see if this address has already been attached to this contact
            ContactAddress existingAddress = null;
            for (ContactAddress address : addresses) {
                if (address.getId().equals(value.getId())) {
                    existingAddress = address;
                    break;
                }
            }

            // create new contact address if it wasn't found above and update ID fields
            if (existingAddress == null) {
                existingAddress = new ContactAddress();
                existingAddress.setId(value.getId());
                existingAddress.setContactId(value.getContactId());
                existingAddress.setCreated(value.getCreated());
                addresses.add(existingAddress);
            }

            // update all contact address fields from the incoming address
            existingAddress.setAddressLine1(value.getAddressLine1());
            existingAddress.setAddressLine2(value.getAddressLine2());
            existingAddress.setAddressLine3(value.getAddressLine3());
            existingAddress.setCity(value.getCity());
            existingAddress.setState(value.getState());
            existingAddress.setCountry(value.getCountry());
            existingAddress.setPostalCode(value.getPostalCode());
            existingAddress.setUpdated(value.getUpdated());
            return aggregate;
        });

        // create contact table from combined contact and address info above
        KTable<String, Contact> contactTable = cogroupContactAddressStream.aggregate(Contact::new,
                contactStateStoreMaterializer.getStateStore(CONTACT_STATE_STORE, Serdes.String(), serdes.createContactSerde()));
        // output contact table to output topic
        contactTable.toStream()
                // don't send contacts to output topic that only have addresses on them
                .filter((key, value) -> value.getFirstName() != null && value.getLastName() != null)
                .to(outputTopic, Produced.with(Serdes.String(), serdes.createContactSerde()));
        return builder.build(streamProperties);
    }
}