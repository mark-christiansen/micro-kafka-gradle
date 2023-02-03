package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.SerdeCreator;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class StatelessTopologyBuilder {

    private static final Logger log = LoggerFactory.getLogger(StatelessTopologyBuilder.class);

    private final String inputTopic;
    private final String outputTopic;
    private final String failureOutputTopic;
    private final boolean inMemoryStateStores;
    private final SerdeCreator serdes;
    private final Producer<Long, GenericRecord> errorHandler;
    private final SchemaRegistryClient schemaRegistryClient;

    public StatelessTopologyBuilder(Map<String, Object> props,
                                    Producer<Long, GenericRecord> errorHandler,
                                    SchemaRegistryClient schemaRegistryClient,
                                    SerdeCreator serdes) {

        this.inputTopic = (String) props.get("input.topic");
        this.outputTopic = (String) props.get("output.topic");
        this.failureOutputTopic = (String) props.get("failure.output.topic");
        this.inMemoryStateStores = Boolean.parseBoolean((String) props.get("in.memory.state.stores"));
        this.errorHandler = errorHandler;
        this.schemaRegistryClient = schemaRegistryClient;
        this.serdes = serdes;
    }

    public Topology build(Properties streamProperties) {

        // uncomment to get output schema by latest version
        //String subject = outputTopic + "-value";
        //final Schema outputTopicSchema = new Schema.Parser().parse(this.schemaRegistryClient.getLatestSchemaMetadata(subject).getSchema());
        // get output schema by ID
        //final Schema outputTopicSchema = new Schema.Parser().parse(this.schemaRegistryClient.getSchemaById(outputTopicSchemaId).canonicalString());

        log.info("Subscribing to input topic {}", inputTopic);
        final StreamsBuilder builder = new StreamsBuilder();
        builder.stream(inputTopic, Consumed.with(Serdes.Long(), serdes.createGenericSerde(false)))
                .transformValues(new StatelessTransformerSupplier("consumer"))
                .map((k, v) -> {
                    // perform stateless transform
                    return new KeyValue<>(k, v);
                })
                .to(outputTopic, Produced.with(Serdes.Long(), serdes.createGenericSerde(false)));
        return builder.build(streamProperties);
    }

    private Materialized<Long, GenericRecord, KeyValueStore<Bytes, byte[]>> getStateStore(String storeName,
                                                                                          Serde<Long> keySerde,
                                                                                          Serde<GenericRecord> valueSerde) {
        KeyValueBytesStoreSupplier supplier = inMemoryStateStores ? Stores.inMemoryKeyValueStore(storeName) :
                Stores.persistentKeyValueStore(storeName);
        return Materialized .<Long, GenericRecord>as(supplier).withKeySerde(keySerde).withValueSerde(valueSerde);
    }

    private static class StatelessTransformerSupplier implements ValueTransformerWithKeySupplier<Long, GenericRecord, GenericRecord> {

        private String tag;

        public StatelessTransformerSupplier(String tag) {
            this.tag = tag;
        }

        @Override
        public ValueTransformerWithKey<Long, GenericRecord, GenericRecord> get() {
            return new StatelessTransformer(tag);
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return ValueTransformerWithKeySupplier.super.stores();
        }
    }

    private static class StatelessTransformer implements ValueTransformerWithKey<Long, GenericRecord, GenericRecord> {

        private ProcessorContext processorContext;
        private String tag;

        public StatelessTransformer(String tag) {
            this.tag = tag;
        }

        @Override
        public void init(ProcessorContext processorContext) {
            this.processorContext = processorContext;
        }

        @Override
        public GenericRecord transform(Long key, GenericRecord value) {
            Headers headers = processorContext.headers();
            log.info( tag + " headers:");
            for (Header header : headers) {
                String headerValue = new String(header.value());
                log.info(header.key() + "=" + headerValue);
            }
            return value;
        }

        @Override
        public void close() {
        }
    }
}
