package com.mycompany.kafka.streams;

import com.mycompany.kafka.streams.metrics.KafkaStreamConfiguration;
import com.mycompany.kafka.streams.metrics.KafkaStreamsRegistry;
import com.mycompany.kafka.streams.serdes.Serdes;
import com.mycompany.kafka.streams.topology.TopologyBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.streams.Topology;

import java.util.Map;
import java.util.Properties;

@Factory
public class StreamFactory {

    private final Environment environment;
    private final KafkaStreamsRegistry kafkaStreamsRegistry;
    private final Serdes serdes;

    public StreamFactory(Environment environment,
                         KafkaStreamsRegistry kafkaStreamsRegistry,
                         Serdes serdes) {
        this.environment = environment;
        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.serdes = serdes;
    }

    @Singleton
    @Named("applicationProperties")
    public Map<String, Object> applicationProperties() {
        return environment.getProperties("application");
    }

    @Singleton
    @Named("streamProperties")
    public Properties streamProperties() {
        return convert(environment.getProperties("kafka.streams"));
    }

    @Singleton
    public Producer<Long, GenericRecord> producer() {
        Map<String, Object> producerProps = environment.getProperties("kafka.producer");
        return new  KafkaProducer<>(producerProps);
    }

    @Singleton
    public KafkaStreamConfiguration kafkaStreamsConfiguration(TopologyBuilder topologyBuilder) {
        Properties streamProps = streamProperties();
        Topology topology = topologyBuilder.build(streamProps);
        return kafkaStreamsRegistry.register(topology, streamProps);
    }

    private Properties convert(Map<String, Object> map) {
        Properties props = new Properties();
        props.putAll(map);
        return props;
    }
}
