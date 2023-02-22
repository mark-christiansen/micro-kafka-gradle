package com.mycompany.kafka.producer.data;

import com.github.javafaker.Address;
import com.github.javafaker.Country;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.Producer;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CountryProducer extends AbstractProducer {

    private static final String SCHEMA_NAME = "country";

    public CountryProducer(Producer<Long, GenericRecord> producer,
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

        // make sure generated country code is unique
        Set<Object> codes = uniqueValues.computeIfAbsent("code", key -> new HashSet<>());
        String code;
        String name;
        do {
            Country country = faker.country();
            code = country.countryCode2().toUpperCase();
            name = new Locale("English", code).getDisplayCountry();
        } while(codes.contains(code));
        codes.add(code);

        recordBuilder.set("code", code);
        recordBuilder.set("description", name);
        recordBuilder.set("created", System.currentTimeMillis());

        return recordBuilder.build();
    }
}
