package com.mycompany.kafka.producer;

import com.mycompany.kafka.schemas.SchemaLoader;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.io.IOException;
import java.util.Map;

@Factory
public class ApplicationFactory {

    private static final String LOAD_SCHEMAS_FROM_CLASSPATH = "application.load.schemas.from.classpath";
    private static final String SCHEMAS_PATH = "application.schemas.path";

    private final Environment environment;

    public ApplicationFactory(Environment environment) {
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
        String schemasPath = environment.getProperty(SCHEMAS_PATH, String.class, "schemas/source");
        Boolean loadSchemasFromClasspath = environment.getProperty(LOAD_SCHEMAS_FROM_CLASSPATH, Boolean.class, true);
        return new SchemaLoader(schemasPath, Boolean.TRUE.equals(loadSchemasFromClasspath));
    }
}