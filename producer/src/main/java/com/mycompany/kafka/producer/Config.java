package com.mycompany.kafka.producer;

import com.mycompany.kafka.schemas.SchemaLoader;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import io.micronaut.core.naming.conventions.StringConvention;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Factory
public class Config {

    private static final String SCHEMA_REGISTRY_AUTH = "schema.registry.auth";

    private final Environment environment;

    public Config(Environment environment) {
        this.environment = environment;
    }

    @Singleton
    public Producer<Long, GenericRecord> producer() {
        Map<String, Object> map = environment.getProperties("kafka.producer");
        return new KafkaProducer<>(map);
    }

    @Singleton
    public AdminClient adminClient() {
        Map<String, Object> map = environment.getProperties("kafka.admin");
        return AdminClient.create(map);
    }

    @Singleton
    public SchemaLoader schemaLoader() throws IOException {
        String schemasPath =  environment.getProperty("application.schemas.path", String.class, "schemas");
        return new SchemaLoader(schemasPath);
    }
}