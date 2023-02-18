package com.mycompany.kafka.streams;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
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

@DisplayName("Stream tests")
public class StreamTest extends TopologyTest {

    private final Map<String, Object> config = new HashMap<>();
    private final Producer<Long, GenericRecord> errorHandler = new MockProducer<>();
    private TopologyBuilder topologyBuilder;

    @Override
    @BeforeEach
    public void setup() {

        super.setup();

        config.put("input.topic", "input");
        config.put("output.topic", "output");
        config.put("failure.output.topic", "error");
        config.put("in.memory.state.stores", "false");

        topologyBuilder = new TopologyBuilder(config, errorHandler, schemaRegistryClient, serdes);
    }

    @Test
    public void test() throws IOException {

        KeyValue<Serdes.LongSerde, GenericAvroSerde> inputSerdes = getInputSerdes(schemaRegistryClient);
        KeyValue<Serdes.LongSerde, GenericAvroSerde> outputSerdes = getOutputSerdes(schemaRegistryClient);

        Topology topology = topologyBuilder.build(kafkaConfig);
        try (TopologyTestDriver testDriver = new TopologyTestDriver(topology, kafkaConfig)) {

            TestInputTopic<Long, GenericRecord> inputTopic = testDriver.createInputTopic(
                    "input", inputSerdes.key.serializer(), inputSerdes.value.serializer());
            TestOutputTopic<Long, GenericRecord> outputTopic = testDriver.createOutputTopic(
                    "output", outputSerdes.key.deserializer(), outputSerdes.value.deserializer());

            TestDataHelper dataHelper = new TestDataHelper("schemas");
            KeyValue<Long, GenericRecord> in = dataHelper.createContact();
            inputTopic.pipeInput(in.key, in.value);
            TestRecord<Long, GenericRecord> out = outputTopic.readRecord();

            dataHelper.verifyKey(in.key, out.key());
            dataHelper.verifyContactValue(in.value, out.value());
        }
    }

    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getInputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde =  new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde =  new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
    private KeyValue<Serdes.LongSerde, GenericAvroSerde> getOutputSerdes(SchemaRegistryClient schemaRegistry) {

        Serdes.LongSerde keySerde =  new Serdes.LongSerde();
        keySerde.configure(schemaRegistryConfig, true);
        GenericAvroSerde valueSerde =  new GenericAvroSerde(schemaRegistry);
        valueSerde.configure(schemaRegistryConfig, false);
        return new KeyValue<>(keySerde, valueSerde);
    }
}
