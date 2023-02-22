package com.mycompany.kafka.producer.data;

import com.github.javafaker.Name;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

public class ContactProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "contact";

    public ContactProducer(Producer<Long, GenericRecord> producer,
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

        // make sure generated ssn is unique
        String ssn = (String) getUniqueValue("ssn", ()-> faker.regexify("[0-9]{3}-[0-9]{2}-[0-9]{4}"));
        recordBuilder.set("ssn", ssn);

        Name name = faker.name();
        recordBuilder.set("firstName", name.firstName());
        recordBuilder.set("lastName", name.lastName());
        recordBuilder.set("middleName", faker.name().firstName());
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
