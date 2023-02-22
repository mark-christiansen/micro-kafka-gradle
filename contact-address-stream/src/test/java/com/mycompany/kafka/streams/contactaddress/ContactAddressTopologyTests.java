package com.mycompany.kafka.streams.contactaddress;

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

@DisplayName("Contact Address topology tests")
public class ContactAddressTopologyTests extends TopologyTest {

    private static final String CONTACT_ADDRESS_TOPIC = "input.contactaddress";
    private static final String STATE_TOPIC = "input.state";
    private static final String COUNTRY_TOPIC = "input.country";
    private static final String OUTPUT_TOPIC = "output";

    private final Map<String, Object> config = new HashMap<>();
    private final Producer<Long, GenericRecord> errorHandler = new MockProducer<>();
    private ContactAddressTopologyBuilder topologyBuilder;

    @Override
    @BeforeEach
    public void setup() {

        super.setup();

        config.put("contact.address.topic", CONTACT_ADDRESS_TOPIC);
        config.put("state.topic", STATE_TOPIC);
        config.put("country.topic", COUNTRY_TOPIC);
        config.put("output.topic", OUTPUT_TOPIC);
        config.put("failure.output.topic", "error");
        config.put("in.memory.state.stores", "false");

        topologyBuilder = new ContactAddressTopologyBuilder(config, errorHandler,
                new com.mycompany.kafka.streams.serdes.Serdes(schemaRegistryClient, schemaRegistryConfig));
    }

    @Test
    public void test() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.StringSerde, SpecificAvroSerde<ContactAddress>> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> countryTopic = testDriver.createInputTopic(
                    COUNTRY_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<Long, GenericRecord> stateTopic = testDriver.createInputTopic(
                    STATE_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestInputTopic<Long, GenericRecord> inputTopic = testDriver.createInputTopic(
                    CONTACT_ADDRESS_TOPIC, inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestOutputTopic<String, ContactAddress> outputTopic = testDriver.createOutputTopic(
                    OUTPUT_TOPIC, outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas/source");
            KeyValue<Long, GenericRecord> state = dataHelper.createState();
            stateTopic.pipeInput(state.key, state.value);
            KeyValue<Long, GenericRecord> country = dataHelper.createCountry();
            countryTopic.pipeInput(country.key, country.value);

            KeyValue<Long, GenericRecord> address = dataHelper.createContactAddress();
            inputTopic.pipeInput(address.key, address.value);
            TestRecord<String, ContactAddress> out = outputTopic.readRecord();

            dataHelper.verifyContactAddressKey(address.key, out.key());
            dataHelper.verifyContactAddressValue(address.value, state.value, country.value, out.value());
        }
    }

    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde =  new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde =  new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
    private KeyValue<Serdes.StringSerde, SpecificAvroSerde<ContactAddress>> getOutputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.StringSerde keySerde =  new Serdes.StringSerde();
        keySerde.configure(schemaRegistryConfig, true);
        SpecificAvroSerde<ContactAddress> valueSerde =  new SpecificAvroSerde<>(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
}