package com.mycompany.kafka.streams;

import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.streams.KeyValue;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;

public class TestDataHelper {

    private final String schemasPath;

    public TestDataHelper(String schemasPath) {
        this.schemasPath = schemasPath;
    }

    public KeyValue<Long, GenericRecord> createCustomer() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 249L;

        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("customer"));
        value.put("id", key);
        value.put("firstName", new Utf8("Robert"));
        value.put("lastName", new Utf8("Hennen"));
        value.put("creditCardNumber", new Utf8("555-555-555-555"));
        value.put("created", 1000L);
        value.put("updated", 1000L);

        return new KeyValue<>(key, value);
    }

    public void verifyCustomerKey(Long in, Long out) {
        Assertions.assertEquals(in, out);
    }

    public void verifyCustomerValue(GenericRecord in, GenericRecord out) {
        Assertions.assertEquals(in.get("id"), out.get("id"));
        Assertions.assertEquals(in.get("firstName"), out.get("firstName"));
        Assertions.assertEquals(in.get("lastName"), out.get("lastName"));
        Assertions.assertEquals(in.get("creditCardNumber"), out.get("creditCardNumber"));
        Assertions.assertEquals(in.get("created"), out.get("created"));
        Assertions.assertEquals(in.get("updated"), out.get("updated"));
    }

    private void assertString(Utf8 expected, String actual) {
        if (expected == null) {
            Assertions.assertNull(actual);
        } else {
            Assertions.assertEquals(expected.toString(), actual);
        }
    }

    private ByteBuffer toBytes(int i) {
        return ByteBuffer.wrap(new BigInteger(String.valueOf(i)).toByteArray());
    }
}
