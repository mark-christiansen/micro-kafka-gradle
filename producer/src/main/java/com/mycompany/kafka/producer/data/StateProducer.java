package com.mycompany.kafka.producer.data;

import com.github.javafaker.Address;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

import java.util.HashSet;
import java.util.Set;

public class StateProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "state";

    public StateProducer(Producer<Long, GenericRecord> producer,
                         SchemaLoader schemaLoader,
                         String topicName,
                         long messages,
                         int batchSize,
                         long frequencyMs) {
        super(producer, schemaLoader, topicName, SCHEMA_NAME, messages, batchSize, frequencyMs);
    }

    protected GenericRecord generate(long count, Schema schema) {

        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
        recordBuilder.set(ID_FIELD, count);

        // make sure generated state code is unique

        Set<Object> codes = uniqueValues.computeIfAbsent("code", key -> new HashSet<>());
        String abbr;
        do {
            Address address = faker.address();
            abbr = address.stateAbbr().toUpperCase();
        } while(codes.contains(abbr));
        codes.add(abbr);

        recordBuilder.set("code", abbr);
        recordBuilder.set("description", State.getName(abbr));
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
