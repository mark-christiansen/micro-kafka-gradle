package com.mycompany.kafka.streams.contactaddress;

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
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Map;
import java.util.Properties;

@Factory
public class ContactAddressTopologyBuilder extends AbstractTopologyBuilder {

    private static final String CONTACT_ADDRESS_SOURCE = "com.mycompany.kafka.model.source.ContactAddress";
    private static final String CONTACT_ADDRESS_TOPIC = "contact.address.topic";
    private static final String COUNTRY_TOPIC = "country.topic";
    private static final String STATE_TOPIC = "state.topic";
    private static final String OUTPUT_TOPIC = "output.topic";
    private static final String ERROR_TOPIC = "failure.output.topic";
    private static final String IN_MEMORY_STATE_STORES = "in.memory.state.stores";
    private static final String COUNTRY_STATE_STORE = "country";
    private static final String STATE_STATE_STORE = "state";

    private final String contactAddressInputTopic;
    private final String countryInputTopic;
    private final String stateInputTopic;
    private final String outputTopic;
    private final String failureOutputTopic;
    private final com.mycompany.kafka.streams.serdes.Serdes serdes;
    private final Producer<Long, GenericRecord> errorHandler;

    private final StateStoreMaterializer<Long, GenericRecord> sourceStateStoreMaterializer;

    public ContactAddressTopologyBuilder(@Named("applicationProperties") Map<String, Object> props,
                                         Producer<Long, GenericRecord> errorHandler,
                                         com.mycompany.kafka.streams.serdes.Serdes serdes) {

        this.contactAddressInputTopic = (String) props.get(CONTACT_ADDRESS_TOPIC);
        this.countryInputTopic = (String) props.get(COUNTRY_TOPIC);
        this.stateInputTopic = (String) props.get(STATE_TOPIC);
        this.outputTopic = (String) props.get(OUTPUT_TOPIC);
        this.failureOutputTopic = (String) props.get(ERROR_TOPIC);
        this.errorHandler = errorHandler;
        this.serdes = serdes;
        boolean inMemoryStateStores = Boolean.parseBoolean((String) props.get(IN_MEMORY_STATE_STORES));
        this.sourceStateStoreMaterializer = new StateStoreMaterializer<>(inMemoryStateStores);
    }

    public Topology build(@Named("streamProperties") Properties streamProperties) {

        final StreamsBuilder builder = new StreamsBuilder();

        // Country GlobalKTable
        GlobalKTable<Long, GenericRecord> countryTable = builder.globalTable(countryInputTopic,
                this.sourceStateStoreMaterializer.getStateStore(COUNTRY_STATE_STORE,
                        Serdes.Long(), serdes.createGenericSerde(false)));

        // State GlobalKTable
        GlobalKTable<Long, GenericRecord> stateTable = builder.globalTable(stateInputTopic,
                this.sourceStateStoreMaterializer.getStateStore(STATE_STATE_STORE,
                        Serdes.Long(), serdes.createGenericSerde(false)));

        // Contact Address Source Topic
        builder.stream(contactAddressInputTopic,
                        Consumed.with(org.apache.kafka.common.serialization.Serdes.Long(), serdes.createGenericSerde(false)))
                // filter out just the contact address records
                .filter(((key, value) -> CONTACT_ADDRESS_SOURCE.equalsIgnoreCase(value.getSchema().getFullName())))
                // map GenericRecord to ContactAddress
                .map((key, value) -> {

                    long sourceId = (Long) value.get("id");
                    String addressId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT_ADDRESS.toString(),
                            sourceId).toString();
                    String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                            (Long) value.get("contactId")).toString();

                    ContactAddress address = new ContactAddress();
                    address.setId(addressId);
                    address.setContactId(contactId);
                    address.setAddressLine1(asString(value.get("addressLine1")));
                    address.setAddressLine2(asString(value.get("addressLine2")));
                    address.setAddressLine3(asString(value.get("addressLine3")));
                    address.setCity(asString(value.get("city")));
                    address.setState(String.valueOf(value.get("stateId")));
                    address.setCountry(String.valueOf(value.get("countryId")));
                    address.setPostalCode(asString(value.get("postalCode")));
                    address.setCreated(asInstant(value.get("created")));
                    address.setUpdated(asInstant(value.get("updated")));
                    return new KeyValue<>(addressId, address);

                })
                // join state GenericRecord to ContactAddress
                .leftJoin(stateTable,
                        (key, value) -> value.getState() != null ? Long.parseLong(value.getState()) : null,
                        (value1, value2) -> {
                            value1.setState(asString(value2.get("code")));
                            return value1;
                        })
                // join country GenericRecord to ContactAddress
                .leftJoin(countryTable,
                        (key, value) -> value.getCountry() != null ? Long.parseLong(value.getCountry()) : null,
                        (value1, value2) -> {
                            value1.setCountry(asString(value2.get("code")));
                            return value1;
                        })
                // send to output topic
                .to(outputTopic, Produced.with(Serdes.String(), serdes.createContactAddressSerde()));
        return builder.build(streamProperties);
    }
}