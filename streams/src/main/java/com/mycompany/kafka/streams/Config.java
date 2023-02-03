package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.SerdeCreator;
import com.mycompany.kafka.common.streams.StreamsLifecycle;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.streams.Topology;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Factory
public class Config {

    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    private static final String SCHEMA_CACHE_CAPACITY = "schema.cache.capacity";
    private static final String SCHEMA_REGISTRY_AUTH = "schema.registry.auth";
    private static final String STATELESS = "stateless";
    @Property(name="application.stream.type")
    private String streamType;

    @Inject
    private Environment environment;

    @Singleton
    public SchemaRegistryClient schemaRegistryClient() {

        Map<String, Object> streamsProps = environment.getProperties("kafka.streams");

        // pull out schema registry properties from kafka properties to pass to schema registry client
        Map<String, Object> schemaProps = new HashMap<>();
        for (Map.Entry<String, Object> entry : streamsProps.entrySet()) {
            String propertyName = entry.getKey();
            if (propertyName.startsWith("schema.registry.") || propertyName.startsWith("basic.auth.")) {
                schemaProps.put(propertyName, entry.getValue());
            }
        }
        return new CachedSchemaRegistryClient((String) streamsProps.get(SCHEMA_REGISTRY_URL),
                (Integer) streamsProps.get(SCHEMA_CACHE_CAPACITY), schemaProps);
    }

    @Singleton
    public Producer<Long, GenericRecord> producer() {
        Map<String, Object> producerProps = environment.getProperties("kafka.producer");
        return new  KafkaProducer<>(producerProps);
    }

    @Singleton
    public SerdeCreator serdeCreator() {
        Map<String, Object> streamsProps = environment.getProperties("kafka.streams");
        return new SerdeCreator(prepare(streamsProps), schemaRegistryClient());
    }

    @Singleton
    public StreamsLifecycle streamsLifecycle(ApplicationContext applicationContext) {

        Map<String, Object> appProps = environment.getProperties("application");
        Map<String, Object> streamProps = environment.getProperties("kafka.streams");
        Topology topology;
        if (streamType.equals(STATELESS)) {
            topology = new StatelessTopologyBuilder(appProps, producer(), schemaRegistryClient(), serdeCreator())
                    .build(convert(streamProps));
        } else {
            topology = new StatefulTopologyBuilder(appProps, producer(), schemaRegistryClient(), serdeCreator())
                    .build(convert(streamProps));
        }
        return new StreamsLifecycle(topology, appProps, convert(prepare(streamProps)), applicationContext);
    }

    private Map<String, Object> prepare(Map<String, Object> props) {

        boolean auth = Boolean.parseBoolean((String) props.get(SCHEMA_REGISTRY_AUTH));
        if (!auth) {
            props.remove(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE);
            props.remove(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG);
        }
        return props;
    }

    private Properties convert(Map<String, Object> map) {
        Properties props = new Properties();
        props.putAll(map);
        return props;
    }
}