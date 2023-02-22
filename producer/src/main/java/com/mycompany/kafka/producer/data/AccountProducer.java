package com.mycompany.kafka.producer.data;

import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

public class AccountProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "account";

    public AccountProducer(Producer<Long, GenericRecord> producer,
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

        // make sure generated account number is unique
        String accountNumber = (String) getUniqueValue("accountNumber",
                ()-> faker.regexify("[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}"));

        recordBuilder.set("accountNumber", accountNumber);
        recordBuilder.set("accountType", faker.letterify("?", true));
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
