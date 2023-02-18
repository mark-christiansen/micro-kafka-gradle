package com.mycompany.kafka.producer;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Collections;
import java.util.Map;

@Factory
public class Config {

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
    public DataGenerator dataGenerator() {
        return new DataGenerator(Collections.EMPTY_MAP);
    }
}