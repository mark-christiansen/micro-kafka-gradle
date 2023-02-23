package com.mycompany.kafka.streams.account;

import com.mycompany.kafka.model.canonical.Account;
import com.mycompany.kafka.model.canonical.AccountContact;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@DisplayName("Account topology tests")
public class AccountTopologyTests extends TopologyTest {

    private static final String ACCOUNT_TOPIC = "input.account";
    private static final String ACCOUNT_CONTACT_TOPIC = "input.accountcontact";
    private static final String OUTPUT_TOPIC = "output";
    private static final String SCHEMA_SOURCE_DIR = "schemas/source";

    private final Map<String, Object> config = new HashMap<>();
    private final Producer<Long, GenericRecord> errorHandler = new MockProducer<>();
    private AccountTopologyBuilder topologyBuilder;

    @Override
    @BeforeEach
    public void setup() {

        super.setup();

        config.put("account.topic", ACCOUNT_TOPIC);
        config.put("account.contact.topic", ACCOUNT_CONTACT_TOPIC);
        config.put("output.topic", OUTPUT_TOPIC);
        config.put("failure.output.topic", "error");

        topologyBuilder = new AccountTopologyBuilder(config, errorHandler,
                new com.mycompany.kafka.streams.serdes.Serdes(schemaRegistryClient, schemaRegistryConfig));
    }

    @Test
    public void testAccountThenAccountContact() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> accountContactInputSerdes = getAccountContactInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Account>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> accountTopic = testDriver.createInputTopic(
                    ACCOUNT_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<String, AccountContact> accountContactInputTopic = testDriver.createInputTopic(
                    ACCOUNT_CONTACT_TOPIC, accountContactInputSerdes.key.serializer(), accountContactInputSerdes.value.serializer());
            TestOutputTopic<String, Account> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper(SCHEMA_SOURCE_DIR);

            // produce account
            KeyValue<Long, GenericRecord> in = dataHelper.createAccount();
            accountTopic.pipeInput(in.key, in.value);

            // produce account contact
            KeyValue<Long, GenericRecord> sourceAccountContact = dataHelper.createAccountContact();
            KeyValue<String, AccountContact> accountContact = dataHelper.canonizeAccountContact(sourceAccountContact);
            accountContactInputTopic.pipeInput(accountContact.key, accountContact.value);

            // the first record has account info but no account contacts
            TestRecord<String, Account> out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, out.value());

            // the second record has both account info and account contacts
            out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, Collections.singletonList(accountContact.value), out.value());
        }
    }

    @Test
    public void testAccountThenAccountContacts() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> accountContactInputSerdes = getAccountContactInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Account>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> accountTopic = testDriver.createInputTopic(
                    ACCOUNT_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<String, AccountContact> accountContactInputTopic = testDriver.createInputTopic(
                    ACCOUNT_CONTACT_TOPIC, accountContactInputSerdes.key.serializer(), accountContactInputSerdes.value.serializer());
            TestOutputTopic<String, Account> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper(SCHEMA_SOURCE_DIR);

            // produce account
            KeyValue<Long, GenericRecord> in = dataHelper.createAccount();
            accountTopic.pipeInput(in.key, in.value);

            // produce account contact 1
            KeyValue<Long, GenericRecord> sourceAccountContact1 = dataHelper.createAccountContact();
            KeyValue<String, AccountContact> accountContact1 = dataHelper.canonizeAccountContact(sourceAccountContact1);
            accountContactInputTopic.pipeInput(accountContact1.key, accountContact1.value);

            // produce account contact 2
            KeyValue<Long, GenericRecord> sourceAccountContact2 = dataHelper.createAccountContact2();
            KeyValue<String, AccountContact> accountContact2 = dataHelper.canonizeAccountContact(sourceAccountContact2);
            accountContactInputTopic.pipeInput(accountContact2.key, accountContact2.value);

            // the first record has account info but no account contacts
            TestRecord<String, Account> out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, out.value());

            // the second record has account info and first account contact
            out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, Collections.singletonList(accountContact1.value), out.value());

            // the third record has account info and both account contacts
            out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, Arrays.asList(accountContact1.value, accountContact2.value), out.value());
        }
    }

    @Test
    public void testAccountContactThenAccount() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> accountContactInputSerdes = getAccountContactInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<Account>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> accountTopic = testDriver.createInputTopic(
                    ACCOUNT_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<String, AccountContact> accountContactInputTopic = testDriver.createInputTopic(
                    ACCOUNT_CONTACT_TOPIC, accountContactInputSerdes.key.serializer(), accountContactInputSerdes.value.serializer());
            TestOutputTopic<String, Account> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper(SCHEMA_SOURCE_DIR);

            // produce account contact
            KeyValue<Long, GenericRecord> sourceAccountContact = dataHelper.createAccountContact();
            KeyValue<String, AccountContact> accountContact = dataHelper.canonizeAccountContact(sourceAccountContact);
            accountContactInputTopic.pipeInput(accountContact.key, accountContact.value);

            // produce account
            KeyValue<Long, GenericRecord> in = dataHelper.createAccount();
            accountTopic.pipeInput(in.key, in.value);

            // the first record has both account info and account contacts
            TestRecord<String, Account> out = outputTopic.readRecord();
            dataHelper.verifyAccountKey(in.key, out.key());
            dataHelper.verifyAccountValue(in.value, Collections.singletonList(accountContact.value), out.value());
        }
    }

    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde = new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde = new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<AccountContact>> getAccountContactInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<AccountContact> valueSerde = new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }

    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<Account>> getOutputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<Account> valueSerde =  new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
}
