package com.mycompany.kafka.streams;

import com.mycompany.kafka.model.canonical.Contact;
import com.mycompany.kafka.model.canonical.ContactAddress;
import com.mycompany.kafka.model.source.Source;
import com.mycompany.kafka.schemas.SchemaLoader;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.apache.kafka.streams.KeyValue;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TestDataHelper {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private final String schemasPath;

    public TestDataHelper(String schemasPath) {
        this.schemasPath = schemasPath;
    }

    public KeyValue<Long, GenericRecord> createCountry() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 1L;

        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("country"));
        value.put("id", key);
        value.put("code", new Utf8("US"));
        value.put("description", new Utf8("United States"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createState() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 2L;

        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("state"));
        value.put("id", key);
        value.put("code", new Utf8("MO"));
        value.put("description", new Utf8("Missouri"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createContactAddress() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 3L;

        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("contactaddress"));
        value.put("id", key);
        value.put("contactId", 4L);
        value.put("addressLine1", new Utf8("742 Evergreen Terrace"));
        value.put("addressLine2", new Utf8(""));
        value.put("addressLine3", new Utf8(""));
        value.put("city", new Utf8("Springfield"));
        value.put("stateId", 2L);
        value.put("countryId", 1L);
        value.put("postalCode", new Utf8("55555"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));

        return new KeyValue<>(key, value);
    }

    public KeyValue<String, ContactAddress> canonizeContactAddress(KeyValue<Long, GenericRecord> kv) {

        GenericRecord value = kv.value;
        String addressId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT_ADDRESS.toString(),
                (Long) value.get("id")).toString();
        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) value.get("contactId")).toString();
        ContactAddress address = new ContactAddress();
        address.setId(addressId);
        address.setContactId(contactId);
        address.setAddressLine1(asString(value.get("addressLine1")));
        address.setAddressLine2(asString(value.get("addressLine2")));
        address.setAddressLine3(asString(value.get("addressLine3")));
        address.setCity(asString(value.get("city")));
        // just pick aa state because we would need access to a State record to convert
        address.setState("MN");
        // just pick a country because we would need access to a Country record to convert
        address.setCountry("US");
        address.setPostalCode(asString(value.get("postalCode")));
        address.setCreated(asInstant(value.get("created")));
        address.setUpdated(asInstant(value.get("updated")));
        return new KeyValue<>(addressId, address);
    }

    public KeyValue<Long, GenericRecord> createContact() throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();

        SchemaLoader schemaLoader = new SchemaLoader(schemaFilepath);
        Long key = 4L;

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
        assertEquals(in, out);
    }

    public void verifyContactKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(), in).toString();
        assertEquals(inString, out);
    }

    public void verifyContactValue(GenericRecord expectedContact, Contact actualContact) {

        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) expectedContact.get("id")).toString();
        assertEquals(contactId, actualContact.get("id"));
        assertString(expectedContact.get("firstName"), actualContact.getFirstName());
        assertString(expectedContact.get("middleName"), actualContact.getMiddleName());
        assertString(expectedContact.get("lastName"), actualContact.getLastName());
        assertString(expectedContact.get("ssn"), actualContact.getSocialSecurityNumber());
        assertLocalDate(expectedContact.get("birthDate"), actualContact.getBirthDate());
        assertInstant(expectedContact.get("created"), actualContact.getCreated());
        assertInstant(expectedContact.get("updated"), actualContact.getUpdated());
    }

    public void verifyContactValue(GenericRecord expectedContact, ContactAddress expectedAddress, Contact actualContact) {

        verifyContactValue(expectedContact, actualContact);

        // verify address is attached to expectedContact
        assertNotNull(actualContact.getAddresses(), "contact address list wasn't initialized");
        assertEquals(1, actualContact.getAddresses().size(), "contact address list is empty");
        ContactAddress actualAddress = actualContact.getAddresses().get(0);
        verifyContactAddressValue(expectedAddress, actualAddress);
    }

    public void verifyContactAddressValue(ContactAddress expectedAddress, ContactAddress actualAddress) {

        assertEquals(expectedAddress.getId(), actualAddress.getId());
        assertEquals(expectedAddress.getContactId(), actualAddress.getContactId());
        assertEquals(expectedAddress.getAddressLine1(), actualAddress.getAddressLine1());
        assertEquals(expectedAddress.getAddressLine2(), actualAddress.getAddressLine2());
        assertEquals(expectedAddress.getAddressLine3(), actualAddress.getAddressLine3());
        assertEquals(expectedAddress.getCity(), actualAddress.getCity());
        assertEquals(expectedAddress.getState(), actualAddress.getState());
        assertEquals(expectedAddress.getCountry(), actualAddress.getCountry());
        assertEquals(expectedAddress.getPostalCode(), actualAddress.getPostalCode());
        assertEquals(expectedAddress.getCreated(), actualAddress.getCreated());
        assertEquals(expectedAddress.getUpdated(), actualAddress.getUpdated());
    }

    public void verifyContactAddressKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT_ADDRESS.toString(), in).toString();
        assertEquals(inString, out);
    }

    public void verifyContactAddressValue(GenericRecord address, GenericRecord state, GenericRecord country, ContactAddress out) {

        String addressId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT_ADDRESS.toString(),
                (Long) address.get("id")).toString();
        assertEquals(addressId, out.get("id"));
        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) address.get("contactId")).toString();
        assertEquals(contactId, out.get("contactId"));
        assertString(address.get("addressLine1"), out.getAddressLine1());
        assertString(address.get("addressLine2"), out.getAddressLine2());
        assertString(address.get("addressLine3"), out.getAddressLine3());
        assertString(address.get("city"), out.getCity());
        assertString(state.get("code"), out.getState());
        assertString(country.get("code"), out.getCountry());
        assertString(address.get("postalCode"), out.getPostalCode());
        assertInstant(address.get("created"), out.getCreated());
        assertInstant(address.get("updated"), out.getUpdated());
    }

    private void assertString(Object expected, String actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.toString(), actual);
        }
    }

    private void assertInstant(Object expected, Instant actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(Instant.ofEpochMilli((Long) expected), actual);
        }
    }

    private void assertLocalDate(Object expected, LocalDate actual) {
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(LocalDate.ofEpochDay((Integer) expected), actual);
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

    private String asString(Object value) {
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    private Instant asInstant(Object value) {
        if (value != null) {
            return  Instant.ofEpochMilli((Long) value);
        }
        return null;
    }

    private LocalDate asLocalDate(Object value) {
        if (value != null) {
            return LocalDate.ofEpochDay((Integer) value);
        }
        return null;
    }
}
