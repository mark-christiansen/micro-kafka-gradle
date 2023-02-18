package com.mycompany.kafka.consumer;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractGenericRecordConsumer<K, V> {

    private static final Logger log = LoggerFactory.getLogger(AbstractGenericRecordConsumer.class);

    protected final Consumer<K, V> consumer;
    private final String topicName;
    private final long dayOffset;
    private final long pollTimeoutSecs;
    private final AtomicBoolean open = new AtomicBoolean(false);

    public AbstractGenericRecordConsumer(Consumer<K, V> consumer,
                                         String topicName,
                                         long dayOffset,
                                         long pollTimeoutSecs) {
        this.consumer = consumer;
        this.topicName = topicName;
        this.dayOffset = dayOffset;
        this.pollTimeoutSecs = pollTimeoutSecs;
    }

    public void start() {

        open.set(true);
        log.info("Consumer subscribing to topic {} at offset of {} days", topicName, dayOffset);
        subscribe(topicName, dayOffset);

        log.info("Consumer started");
        try {
            while (open.get()) {
                consume(consumer.poll(Duration.ofSeconds(pollTimeoutSecs)));
            }
        } catch (WakeupException e) {
            // ignore error if the consumer has been closed - WakeupException is expected on closing the consumer
            if (open.get()) {
                log.error("Error consuming messages", e);
                throw e;
            }
        } catch (Exception e) {
            log.error("Error consuming messages", e);
            throw e;
        } finally {
            consumer.close();
        }
        log.info("Consumer finished");
    }

    public abstract void consume(ConsumerRecords<K, V> records);

    public void stop() {
        open.set(false);
        consumer.wakeup();
    }

    protected void subscribe(String topicName, long dayOffset) {

        long offsetTimestamp = Instant.now().minus(dayOffset, ChronoUnit.DAYS).toEpochMilli();
        consumer.subscribe(Collections.singleton(topicName), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {}

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

                Map<TopicPartition, Long> timestamps = new HashMap<>();
                for (TopicPartition partition : partitions) {
                    timestamps.put(partition, offsetTimestamp);
                }

                Map<TopicPartition, OffsetAndTimestamp> offsets = consumer.offsetsForTimes(timestamps);
                for (TopicPartition partition : partitions) {
                    OffsetAndTimestamp offset = offsets.get(partition);
                    if (offset != null) {
                        consumer.seek(partition, offset.offset());
                    }
                }
            }
        });
    }

    protected Map<String, Object> getRecordValues(Schema schema, GenericRecord record) {

        Map<String, Object> values = new HashMap<>();
        for (Schema.Field field : schema.getFields()) {

            Schema fieldSchema = field.schema();
            switch (fieldSchema.getName()) {
                case "union":
                    Schema nonNullSchema = fieldSchema.getTypes().get(1);
                    values.put(field.name(), getFieldValue(field.name(), nonNullSchema, record));
                    break;
                case "record":
                    values.put(field.name(), getRecordValues(fieldSchema, (GenericRecord) record.get(field.name())));
                    break;
                default:
                    values.put(field.name(), getFieldValue(field.name(), fieldSchema, record));
            }
        }
        return values;
    }

    protected Object getFieldValue(String fieldName, Schema fieldSchema, GenericRecord record) {
        Object value = record.get(fieldName);
        if (value != null) {
            if ("string".equals(fieldSchema.getName())) {
                if (value instanceof Utf8) {
                    return new String(((Utf8) value).getBytes());
                } else {
                    return value;
                }
            }
        }
        return value;
    }
}
