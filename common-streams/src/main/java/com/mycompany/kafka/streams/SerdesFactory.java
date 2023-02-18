package com.mycompany.kafka.streams;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Factory
public class SerdesFactory {

    private static final String KAFKA_STREAMS_PROPS_PREFIX = "kafka.streams";
    private static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    private static final String SCHEMA_CACHE_CAPACITY = "schema.cache.capacity";

    @Inject
    private Environment environment;

    public SerdesFactory(Environment environment) {
        this.environment = environment;
    }

    @Singleton
    public SchemaRegistryClient schemaRegistryClient() {

        Map<String, Object> streamsProps = environment.getProperties(KAFKA_STREAMS_PROPS_PREFIX);

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
    public Serdes serdes() {
        Map<String, Object> streamsProps = environment.getProperties(KAFKA_STREAMS_PROPS_PREFIX);
        return new Serdes(schemaRegistryClient(), streamsProps);
    }
}
