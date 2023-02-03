package com.mycompany.kafka.consumer;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Map;

@Factory
public class Config {

    private final String topicName;
    private final long dayOffset;
    private final long pollTimeoutSecs;
    private final long commitTimeoutSecs;
    private final Environment environment;

    public Config(@Property(name = "application.topic") String topicName,
                  @Property(name = "application.offset.days") long dayOffset,
                  @Property(name = "application.poll.timeout.secs") long pollTimeoutSecs,
                  @Property(name = "application.commit.timeout.secs") long commitTimeoutSecs,
                  Environment environment) {
        this.topicName = topicName;
        this.dayOffset = dayOffset;
        this.pollTimeoutSecs = pollTimeoutSecs;
        this.commitTimeoutSecs = commitTimeoutSecs;
        this.environment = environment;
    }

    @Singleton
    public Consumer<Long, GenericRecord> consumer() {
        Map<String, Object> map = environment.getProperties("kafka.consumer");
        return new KafkaConsumer<>(map);
    }

    @Singleton
    public GenericRecordConsumer genericRecordConsumer() {
        return new GenericRecordConsumer(consumer(), topicName, dayOffset, pollTimeoutSecs, commitTimeoutSecs);
    }
}