package com.mycompany.kafka.streams.contact;

import com.mycompany.kafka.model.canonical.Contact;
import com.mycompany.kafka.model.canonical.ContactAddress;
import com.mycompany.kafka.streams.TestDataHelper;
import com.mycompany.kafka.streams.TopologyTest;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.test.TestRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Contact topology tests")
public class ContactTopologyTests extends TopologyTest {

    private static final String CONTACT_TOPIC = "input.contact";
    private static final String CONTACT_ADDRESS_TOPIC = "input.contactaddress";
    private static final String OUTPUT_TOPIC = "output";

    private final Map<String, Object> config = new HashMap<>();
    private final Producer<Long, GenericRecord> errorHandler = new MockProducer<>();
    private ContactTopologyBuilder topologyBuilder;

    @Override
    @BeforeEach
    public void setup() {

        super.setup();

        config.put("contact.topic", CONTACT_TOPIC);
        config.put("contact.address.topic", CONTACT_ADDRESS_TOPIC);
        config.put("output.topic", OUTPUT_TOPIC);
        config.put("failure.output.topic", "error");
        config.put("in.memory.state.stores", "false");

        topologyBuilder = new ContactTopologyBuilder(config, errorHandler,
                new com.mycompany.kafka.streams.serdes.Serdes(schemaRegistryClient, schemaRegistryConfig));
    }

    @Test
    public void testContactThenAddress() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> contactInputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<ContactAddress>> addressInputSerdes = getContactAddressInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> contactInputTopic = testDriver.createInputTopic(
                    CONTACT_TOPIC, contactInputSerdes.key.serializer(), contactInputSerdes.value.serializer());
            TestInputTopic<String, ContactAddress> addressInputTopic = testDriver.createInputTopic(
                    CONTACT_ADDRESS_TOPIC, addressInputSerdes.key.serializer(), addressInputSerdes.value.serializer());
            TestOutputTopic<String, Contact> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas/source");

            // produce contact first
            KeyValue<Long, GenericRecord> contact = dataHelper.createContact();
            contactInputTopic.pipeInput(contact.key, contact.value);

            // produce contact address second
            KeyValue<Long, GenericRecord> sourceContactAddress = dataHelper.createContactAddress();
            KeyValue<String, ContactAddress> contactAddress = dataHelper.canonizeContactAddress(sourceContactAddress);
            addressInputTopic.pipeInput(contactAddress.key, contactAddress.value);

            // the first record has just the contact on it, no addresses
            TestRecord<String, Contact> out = outputTopic.readRecord();
            dataHelper.verifyContactKey(contact.key, out.key());
            dataHelper.verifyContactValue(contact.value, out.value());

            // the second record has address and contact info
            out = outputTopic.readRecord();
            dataHelper.verifyContactKey(contact.key, out.key());
            dataHelper.verifyContactValue(contact.value, contactAddress.value, out.value());
        }
    }

    @Test
    public void testAddressThenContact() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> contactInputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<ContactAddress>> addressInputSerdes = getContactAddressInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> contactInputTopic = testDriver.createInputTopic(
                    CONTACT_TOPIC, contactInputSerdes.key.serializer(), contactInputSerdes.value.serializer());
            TestInputTopic<String, ContactAddress> addressInputTopic = testDriver.createInputTopic(
                    CONTACT_ADDRESS_TOPIC, addressInputSerdes.key.serializer(), addressInputSerdes.value.serializer());
            TestOutputTopic<String, Contact> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas/source");

            // produce contact address first
            KeyValue<Long, GenericRecord> sourceContactAddress = dataHelper.createContactAddress();
            KeyValue<String, ContactAddress> contactAddress = dataHelper.canonizeContactAddress(sourceContactAddress);
            addressInputTopic.pipeInput(contactAddress.key, contactAddress.value);

            // produce contact second
            KeyValue<Long, GenericRecord> contact = dataHelper.createContact();
            contactInputTopic.pipeInput(contact.key, contact.value);

            // the first record has address and contact info
            TestRecord<String, Contact> out = outputTopic.readRecord();
            dataHelper.verifyContactKey(contact.key, out.key());
            dataHelper.verifyContactValue(contact.value, contactAddress.value, out.value());
        }
    }

    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde =  new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde =  new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<ContactAddress>> getContactAddressInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<ContactAddress> valueSerde = new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> getOutputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<Contact> valueSerde =  new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
}