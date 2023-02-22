package com.mycompany.kafka.producer.data;

import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

import java.util.HashSet;
import java.util.Set;

public class ContactRoleProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "contactrole";

    public ContactRoleProducer(Producer<Long, GenericRecord> producer,
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

        // make sure generated contact role code is unique
        Set<Object> codes = uniqueValues.computeIfAbsent("code", key -> new HashSet<>());
        String code;
        do {
            code = faker.letterify("?", true);
        } while(codes.contains(code));
        codes.add(code);

        recordBuilder.set("code", code);
        recordBuilder.set("description", faker.company().profession());
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
