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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TestDataHelper {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String schemasPath;

    public TestDataHelper(String schemasPath) {
        this.schemasPath = schemasPath;
    }

    public KeyValue<Long, GenericRecord> createContact() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 249L;

        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("contact"));
        value.put("id", key);
        value.put("firstName", new Utf8("Salvatore"));
        value.put("middleName", new Utf8("Robert"));
        value.put("lastName", new Utf8("Loggia"));
        value.put("ssn", new Utf8("555-55-5555"));
        value.put("birthDate", toEpochDay("01/03/1930"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));

        return new KeyValue<>(key, value);
    }

    public void verifyKey(Long in, Long out) {
        Assertions.assertEquals(in, out);
    }

    public void verifyContactValue(GenericRecord in, GenericRecord out) {
        Assertions.assertEquals(in.get("id"), out.get("id"));
        Assertions.assertEquals(in.get("firstName"), out.get("firstName"));
        Assertions.assertEquals(in.get("middleName"), out.get("middleName"));
        Assertions.assertEquals(in.get("lastName"), out.get("lastName"));
        Assertions.assertEquals(in.get("ssn"), out.get("ssn"));
        Assertions.assertEquals(in.get("birthDate"), out.get("birthDate"));
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

    private int toEpochDay(String date) {
        return (int) LocalDate.parse(date, dateFormatter).toEpochDay();
    }

    private long toEpochMilli(String dateTime) {
        return LocalDateTime.parse(dateTime, dateTimeFormatter)
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli();
    }

    private ByteBuffer toBytes(int i) {
        return ByteBuffer.wrap(new BigInteger(String.valueOf(i)).toByteArray());
    }
}
