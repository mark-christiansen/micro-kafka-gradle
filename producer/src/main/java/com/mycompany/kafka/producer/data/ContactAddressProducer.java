package com.mycompany.kafka.producer.data;

import com.github.javafaker.Address;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

public class ContactAddressProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "contactaddress";

    private final long contactCount;
    private final long stateCount;
    private final long countryCount;

    public ContactAddressProducer(Producer<Long, GenericRecord> producer,
                                  SchemaLoader schemaLoader,
                                  String topicName,
                                  long messages,
                                  int batchSize,
                                  long frequencyMs,
                                  long contactCount,
                                  long stateCount,
                                  long countryCount) {
        super(producer, schemaLoader, topicName, SCHEMA_NAME, messages, batchSize, frequencyMs);
        this.contactCount = contactCount;
        this.stateCount = stateCount;
        this.countryCount = countryCount;
    }

    protected GenericRecord generate(long count, Schema schema) {

        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);
        recordBuilder.set(ID_FIELD, count);

        recordBuilder.set("contactId", faker.number().numberBetween(0L, contactCount));

        Address address = faker.address();
        recordBuilder.set("addressLine1", address.streetAddress());
        recordBuilder.set("addressLine2", "");
        recordBuilder.set("addressLine3", "");
        recordBuilder.set("city", address.city());
        recordBuilder.set("stateId", faker.number().numberBetween(0L, stateCount));
        recordBuilder.set("countryId", faker.number().numberBetween(0L, countryCount));
        recordBuilder.set("postalCode", address.zipCode());
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
