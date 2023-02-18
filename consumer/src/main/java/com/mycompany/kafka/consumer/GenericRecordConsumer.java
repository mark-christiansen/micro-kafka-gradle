package com.mycompany.kafka.consumer;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

public class GenericRecordConsumer extends AbstractGenericRecordConsumer<Long, GenericRecord> {

    private static final Logger log = LoggerFactory.getLogger(GenericRecordConsumer.class);

    private final long commitTimeoutSecs;

    public GenericRecordConsumer(Consumer<Long, GenericRecord> consumer,
                                 String topicName,
                                 long dayOffset,
                                 long pollTimeoutSecs,
                                 long commitTimeoutSecs) {
        super(consumer, topicName, dayOffset, pollTimeoutSecs);
        this.commitTimeoutSecs = commitTimeoutSecs;
    }

    @Override
    public void consume(ConsumerRecords<Long, GenericRecord> records) {
        log.info("Consumed {} records", records.count());
        records.forEach(r -> {
            log.info("Record " + r.key() + " headers:");
            for (Header header : r.headers()) {
                log.info(header.key() + "=" + new String(header.value(), StandardCharsets.UTF_8));
            }
            Long key = r.key();
            GenericRecord value = r.value();
            if (value != null) {
                Map<String, Object> values = getRecordValues(value.getSchema(), value);
                log.info("{}: {}", key, values);
            }
        });
        consumer.commitSync(Duration.ofSeconds(commitTimeoutSecs));
    }
}