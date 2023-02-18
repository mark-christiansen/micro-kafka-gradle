package com.mycompany.kafka.streams;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.streams.Topology;

import java.util.Map;
import java.util.Properties;

@Factory
public class StreamFactory {

    private static final String SCHEMA_REGISTRY_AUTH = "schema.registry.auth";

    private final Environment environment;
    private final KafkaStreamsRegistry kafkaStreamsRegistry;
    private final Serdes serdes;
    private final SchemaRegistryClient schemaRegistryClient;

    public StreamFactory(Environment environment,
                         KafkaStreamsRegistry kafkaStreamsRegistry,
                         Serdes serdes,
                         SchemaRegistryClient schemaRegistryClient) {
        this.environment = environment;
        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.serdes = serdes;
        this.schemaRegistryClient = schemaRegistryClient;
    }

    @Singleton
    public Producer<Long, GenericRecord> producer() {
        Map<String, Object> producerProps = environment.getProperties("kafka.producer");
        return new  KafkaProducer<>(producerProps);
    }

    @Singleton
    public KafkaStreamConfiguration kafkaStreamsConfiguration() {

        Map<String, Object> appProps = environment.getProperties("application");
        Properties streamProps = convert(environment.getProperties("kafka.streams"));
        Topology topology = new TopologyBuilder(appProps, producer(), schemaRegistryClient, serdes)
                    .build(streamProps);
        return kafkaStreamsRegistry.register(topology, streamProps);
    }

    private Properties convert(Map<String, Object> map) {
        Properties props = new Properties();
        props.putAll(map);
        return props;
    }
}