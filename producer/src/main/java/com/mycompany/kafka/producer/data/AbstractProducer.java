package com.mycompany.kafka.producer.data;

import com.github.javafaker.Faker;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public abstract class AbstractProducer implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProducer.class);

    protected static final String ID_FIELD = "id";

    protected final Faker faker = new Faker();
    private final Producer<Long, GenericRecord> producer;
    private final SchemaLoader schemaLoader;
    private final String topicName;
    private final String schemaName;
    private final long messages;
    private final int batchSize;
    private final long frequencyMs;
    protected final Map<String, Set<Object>> uniqueValues = new HashMap<>();

    public AbstractProducer(Producer<Long, GenericRecord> producer,
                            SchemaLoader schemaLoader,
                            String topicName,
                            String schemaName,
                            long messages,
                            int batchSize,
                            long frequencyMs) {
        this.producer = producer;
        this.schemaLoader = schemaLoader;
        this.topicName = topicName;
        this.schemaName = schemaName;
        this.messages = messages;
        this.batchSize = batchSize;
        this.frequencyMs = frequencyMs;
    }

    @Override
    public Boolean call() throws Exception {

        Schema schema = schemaLoader.getSchema(schemaName);
        long count = 0;
        try {
            while (count < messages) {
                long currentBatch = count + batchSize < messages ? batchSize : messages - count;
                List<GenericRecord> records = generate(schema, count, (int) currentBatch);
                for (GenericRecord record : records) {
                    producer.send(new ProducerRecord<>(topicName, (Long) record.get(ID_FIELD), record));
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
            uniqueValues.clear();
        }

        return true;
    }

    protected abstract GenericRecord generate(long count, Schema schema);

    protected Object getUniqueValue(String key, Supplier<Object> supplier) {

        Set<Object> uniqueSet = uniqueValues.computeIfAbsent(key, k -> new HashSet<>());
        Object value;
        do {
            value = supplier.get();
        } while(uniqueSet.contains(value));
        uniqueSet.add(value);
        return value;
    }

    private List<GenericRecord> generate(Schema schema, long count, int batchSize) {
        List<GenericRecord> records = new ArrayList<>(batchSize);
        for (long i = 0; i < batchSize; i++) {
            records.add(generate(count + i, schema));
        }
        return records;
    }
}
