package com.mycompany.kafka.producer.data;

import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

public class AccountContactProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "accountcontact";

    private final long accountCount;
    private final long contactCount;
    private final long contactRoleCount;

    public AccountContactProducer(Producer<Long, GenericRecord> producer,
                                  SchemaLoader schemaLoader,
                                  String topicName,
                                  long messages,
                                  int batchSize,
                                  long frequencyMs,
                                  long accountCount,
                                  long contactCount,
                                  long contactRoleCount) {
        super(producer, schemaLoader, topicName, SCHEMA_NAME, messages, batchSize, frequencyMs);
        this.accountCount = accountCount;
        this.contactCount = contactCount;
        this.contactRoleCount = contactRoleCount;
    }

    protected GenericRecord generate(long count, Schema schema) {

        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
        recordBuilder.set(ID_FIELD, count);

        recordBuilder.set("accountId", faker.number().numberBetween(0L, accountCount));
        recordBuilder.set("contactId", faker.number().numberBetween(0L, contactCount));
        recordBuilder.set("contactRoleId", faker.number().numberBetween(0L, contactRoleCount));
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
