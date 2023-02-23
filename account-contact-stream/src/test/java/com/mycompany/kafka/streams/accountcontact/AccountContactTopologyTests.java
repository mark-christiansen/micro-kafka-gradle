package com.mycompany.kafka.streams.accountcontact;

import com.mycompany.kafka.model.canonical.AccountContact;
import com.mycompany.kafka.model.canonical.Contact;
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

@DisplayName("Account Contact topology tests")
public class AccountContactTopologyTests extends TopologyTest {

    private static final String ACCOUNT_CONTACT_TOPIC = "input.accountcontact";
    private static final String CONTACT_TOPIC = "input.contact";
    private static final String CONTACT_ROLE_TOPIC = "input.contactrole";
    private static final String OUTPUT_TOPIC = "output";

    private final Map<String, Object> config = new HashMap<>();
    private final Producer<Long, GenericRecord> errorHandler = new MockProducer<>();
    private AccountContactTopologyBuilder topologyBuilder;

    @Override
    @BeforeEach
    public void setup() {

        super.setup();

        config.put("account.contact.topic", ACCOUNT_CONTACT_TOPIC);
        config.put("contact.topic", CONTACT_TOPIC);
        config.put("contact.role.topic", CONTACT_ROLE_TOPIC);
        config.put("output.topic", OUTPUT_TOPIC);
        config.put("failure.output.topic", "error");
        config.put("in.memory.state.stores", "false");

        topologyBuilder = new AccountContactTopologyBuilder(config, errorHandler,
                new com.mycompany.kafka.streams.serdes.Serdes(schemaRegistryClient, schemaRegistryConfig));
    }

    @Test
    public void testContactThenAccountContact() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> contactInputSerdes = getContactInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> contactRoleInputTopic = testDriver.createInputTopic(
                    CONTACT_ROLE_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<String, Contact> contactInputTopic = testDriver.createInputTopic(
                    CONTACT_TOPIC, contactInputSerdes.key.serializer(), contactInputSerdes.value.serializer());
            TestInputTopic<Long, GenericRecord> accountContactInputTopic = testDriver.createInputTopic(
                    ACCOUNT_CONTACT_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestOutputTopic<String, AccountContact> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas/source");

            // produce contact role
            KeyValue<Long, GenericRecord> contactRole = dataHelper.createPrimaryContactRole();
            contactRoleInputTopic.pipeInput(contactRole.key, contactRole.value);

            // produce contact
            KeyValue<Long, GenericRecord> sourceContact = dataHelper.createContact();
            KeyValue<String, Contact> contact = dataHelper.canonizeContact(sourceContact);
            contactInputTopic.pipeInput(contact.key, contact.value);

            // produce account contact
            KeyValue<Long, GenericRecord> accountContact = dataHelper.createAccountContact();
            accountContactInputTopic.pipeInput(accountContact.key, accountContact.value);

            // first record contains the account contact and contact role info
            TestRecord<String, AccountContact> out = outputTopic.readRecord();
            dataHelper.verifyAccountContactKey(accountContact.key, out.key());
            dataHelper.verifyAccountContactValue(accountContact.value, contactRole.value, contact.value, out.value());
        }
    }

    @Test
    public void testAccountContactThenContact() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> contactInputSerdes = getContactInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> contactRoleInputTopic = testDriver.createInputTopic(
                    CONTACT_ROLE_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<String, Contact> contactInputTopic = testDriver.createInputTopic(
                    CONTACT_TOPIC, contactInputSerdes.key.serializer(), contactInputSerdes.value.serializer());
            TestInputTopic<Long, GenericRecord> accountContactInputTopic = testDriver.createInputTopic(
                    ACCOUNT_CONTACT_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestOutputTopic<String, AccountContact> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas/source");

            // produce contact role
            KeyValue<Long, GenericRecord> contactRole = dataHelper.createPrimaryContactRole();
            contactRoleInputTopic.pipeInput(contactRole.key, contactRole.value);

            // produce account contact
            KeyValue<Long, GenericRecord> accountContact = dataHelper.createAccountContact();
            accountContactInputTopic.pipeInput(accountContact.key, accountContact.value);

            // produce contact
            KeyValue<Long, GenericRecord> sourceContact = dataHelper.createContact();
            KeyValue<String, Contact> contact = dataHelper.canonizeContact(sourceContact);
            contactInputTopic.pipeInput(contact.key, contact.value);

            // first record contains just the account contact info
            TestRecord<String, AccountContact> out = outputTopic.readRecord();
            dataHelper.verifyAccountContactKey(accountContact.key, out.key());
            dataHelper.verifyAccountContactValue(accountContact.value, contactRole.value, null, out.value());

            // second record contains the account contact and contact info
            out = outputTopic.readRecord();
            dataHelper.verifyAccountContactKey(accountContact.key, out.key());
            dataHelper.verifyAccountContactValue(accountContact.value, contactRole.value, contact.value, out.value());
        }
    }

    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde = new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde = new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<Contact>> getContactInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<Contact> valueSerde = new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> getOutputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<AccountContact> valueSerde =  new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
}