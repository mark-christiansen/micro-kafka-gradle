package com.mycompany.kafka.producer;

import com.mycompany.kafka.schemas.SchemaLoader;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

@Singleton
@ConfigurationProperties("application")
public class GenericRecordProducer {

    private static final Logger log = LoggerFactory.getLogger(GenericRecordProducer.class);

    private final String topicName;
    private final long messages;
    private final String schemaName;
    private final int batchSize;
    private final long frequencyMs;
    private final String schemasPath;
    private final Producer<Long, GenericRecord> producer;
    private final DataGenerator dataGenerator = new DataGenerator(new HashMap<>());

    public GenericRecordProducer(@Property(name = "application.topic") String topicName,
                                 @Property(name = "application.messages") long messages,
                                 @Property(name = "application.schema") String schemaName,
                                 @Property(name = "application.batch.size") int batchSize,
                                 @Property(name = "application.frequency.ms") long frequencyMs,
                                 @Property(name = "application.schemas.path") String schemasPath,
                                 Producer<Long, GenericRecord> producer) {
        this.topicName = topicName;
        this.messages = messages;
        this.schemaName = schemaName;
        this.batchSize = batchSize;
        this.frequencyMs = frequencyMs;
        this.schemasPath = schemasPath;
        this.producer = producer;
    }

    public void start() throws IOException {

        SchemaLoader schemaLoader = new SchemaLoader(schemasPath);
        Schema schema = schemaLoader.getSchema(schemaName);
        if (schema == null) {
            throw new RuntimeException(format("Schema \"%s.avsc\" was not found in the classpath", schemaName));
        }

        log.info("Producer started");
        long count = 0;
        try {
            while (count < messages) {
                long currentBatch = count + batchSize < messages ? batchSize : messages - count;
                List<GenericRecord> records = convert(schema, dataGenerator.generate(schema, (int) currentBatch));
                for (GenericRecord record : records) {
                    producer.send(new ProducerRecord<>(topicName, (Long) record.get("id"), record));
                }
                producer.flush();
                log.info("Produced {} messages", batchSize);
                count += batchSize;
                try {
                    Thread.sleep(frequencyMs);
                } catch (InterruptedException ignored) {}
            }
        } catch (Exception e) {
            log.error("Error producing messages", e);
            throw e;
        } finally {
            producer.close();
        }
        log.info("Produced total of {} messages", count);
        log.info("Producer finished");
    }

    private List<GenericRecord> convert(Schema schema, List<Map<String, Object>> values) {

        List<GenericRecord> records = new ArrayList<>();
        for (Map<String, Object> value : values) {
            GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
            for (Schema.Field field : schema.getFields()) {
                recordBuilder.set(field, value.get(field.name()));
            }
            records.add(recordBuilder.build());
        }
        return records;
    }
}
