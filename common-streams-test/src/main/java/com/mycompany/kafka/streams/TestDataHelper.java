package com.mycompany.kafka.streams;

import com.mycompany.kafka.model.canonical.Account;
import com.mycompany.kafka.model.canonical.AccountContact;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDataHelper {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    private final SchemaLoader schemaLoader;

    public TestDataHelper(String schemasPath) throws IOException {

        URL schemaUrl = this.getClass().getClassLoader().getResource(schemasPath);
        assert schemaUrl != null;
        String schemaFilepath = schemaUrl.getFile();
        this.schemaLoader = new SchemaLoader(schemaFilepath, true);
    }

    public KeyValue<String, Account> canonizeAccount(KeyValue<Long, GenericRecord> kv) {

        GenericRecord value = kv.value;
        String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                (Long) value.get("id")).toString();
        Account account = new Account();
        account.setId(accountId);
        account.setAccountNumber(asString(value.get("accountNumber")));
        account.setAccountType(asString(value.get("accountType")));
        account.setCreated(asInstant(value.get("created")));
        account.setUpdated(asInstant(value.get("updated")));
        return new KeyValue<>(accountId, account);
    }

    public KeyValue<String, AccountContact> canonizeAccountContact(KeyValue<Long, GenericRecord> kv) {

        GenericRecord value = kv.value;
        String accountContactId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT_CONTACT.toString(),
                (Long) value.get("id")).toString();
        String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                (Long) value.get("accountId")).toString();
        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) value.get("contactId")).toString();

        AccountContact accountContact = new AccountContact();
        accountContact.setId(accountContactId);
        accountContact.setAccountId(accountId);

        KeyValue<Long, GenericRecord> primaryContactRole = createPrimaryContactRole();
        KeyValue<Long, GenericRecord> secondaryContactRole = createSecondaryContactRole();

        long contactRoleId = (Long) value.get("contactRoleId");
        if (contactRoleId == (Long) secondaryContactRole.value.get("id")) {
            accountContact.setContactRole(asString(secondaryContactRole.value.get("code")));
        } else {
            accountContact.setContactRole(asString(primaryContactRole.value.get("code")));
        }

        KeyValue<String, Contact> contactKV = canonizeContact(createContact());
        accountContact.setContact(contactKV.value);
        accountContact.getContact().setId(contactId);
        accountContact.setCreated(asInstant(value.get("created")));
        accountContact.setUpdated(asInstant(value.get("updated")));
        return new KeyValue<>(accountContactId, accountContact);
    }

    public KeyValue<String, Contact> canonizeContact(KeyValue<Long, GenericRecord> kv) {

        GenericRecord value = kv.value;
        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) value.get("id")).toString();
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setFirstName(asString(value.get("firstName")));
        contact.setMiddleName(asString(value.get("middleName")));
        contact.setLastName(asString(value.get("lastName")));
        contact.setSocialSecurityNumber(asString(value.get("ssn")));
        contact.setBirthDate(asLocalDate(value.get("birthDate")));
        contact.setCreated(asInstant(value.get("created")));
        contact.setUpdated(asInstant(value.get("updated")));
        return new KeyValue<>(contactId, contact);
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

        KeyValue<Long, GenericRecord> state = createState();
        address.setState(asString(state.value.get("code")));
        KeyValue<Long, GenericRecord> country = createCountry();
        address.setCountry(asString(country.value.get("code")));

        address.setPostalCode(asString(value.get("postalCode")));
        address.setCreated(asInstant(value.get("created")));
        address.setUpdated(asInstant(value.get("updated")));
        return new KeyValue<>(addressId, address);
    }

    public KeyValue<Long, GenericRecord> createAccount() {

        Long key = 5L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("account"));
        value.put("id", key);
        value.put("accountNumber", new Utf8("ABCD-DEFGH-1234-5678"));
        value.put("accountType", new Utf8("A"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createAccountContact() {

        Long key = 7L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("accountcontact"));
        value.put("id", key);
        value.put("accountId", 5L);
        value.put("contactId", 4L);
        value.put("contactRoleId", 6L);
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createAccountContact2() {

        Long key = 9L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("accountcontact"));
        value.put("id", key);
        value.put("accountId", 5L);
        value.put("contactId", 7L);
        value.put("contactRoleId", 10L);
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createContact() {

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

    public KeyValue<Long, GenericRecord> createContact2() {

        Long key = 8L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("contact"));
        value.put("id", key);
        value.put("firstName", new Utf8("Billy"));
        value.put("middleName", new Utf8("Dee"));
        value.put("lastName", new Utf8("Williams"));
        value.put("ssn", new Utf8("111-11-11111"));
        value.put("birthDate", toEpochDay("04/06/1937"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        value.put("updated", toEpochMilli("12/13/2005 15:30:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createContactAddress() {

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

    public KeyValue<Long, GenericRecord> createPrimaryContactRole() {

        Long key = 6L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("contactrole"));
        value.put("id", key);
        value.put("code", new Utf8("P"));
        value.put("description", new Utf8("Primary Account Contact"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createSecondaryContactRole() {

        Long key = 10L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("contactrole"));
        value.put("id", key);
        value.put("code", new Utf8("S"));
        value.put("description", new Utf8("Secondary Account Contact"));
        value.put("created", toEpochMilli("09/17/2003 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createCountry() {

        Long key = 1L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("country"));
        value.put("id", key);
        value.put("code", new Utf8("US"));
        value.put("description", new Utf8("United States"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public KeyValue<Long, GenericRecord> createState() {

        Long key = 2L;
        GenericRecord value = new GenericData.Record(schemaLoader.getSchema("state"));
        value.put("id", key);
        value.put("code", new Utf8("MO"));
        value.put("description", new Utf8("Missouri"));
        value.put("created", toEpochMilli("09/01/2001 12:00:00"));
        return new KeyValue<>(key, value);
    }

    public void verifyAccountKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(), in).toString();
        assertEquals(inString, out);
    }

    public void verifyAccountValue(GenericRecord expectedAccount, Account actualAccount) {

        String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                (Long) expectedAccount.get("id")).toString();
        assertEquals(accountId, actualAccount.getId());
        assertString(expectedAccount.get("accountNumber"), actualAccount.getAccountNumber());
        assertString(expectedAccount.get("accountType"), actualAccount.getAccountType());
        assertInstant(expectedAccount.get("created"), actualAccount.getCreated());
        assertInstant(expectedAccount.get("updated"), actualAccount.getUpdated());
    }

    public void verifyAccountValue(GenericRecord expectedAccount, List<AccountContact> expectedAccountContacts, Account actualAccount) {

        verifyAccountValue(expectedAccount, actualAccount);

        // verify expected account contacts are attached to actual account
        List<AccountContact> actualAccountContacts = actualAccount.getContacts();
        assertNotNull(actualAccountContacts, "account contact list wasn't initialized");
        assertEquals(expectedAccountContacts.size(), actualAccountContacts.size(), "account contact list size doesn't match");
        for (int i = 0; i < expectedAccountContacts.size(); i++) {
            verifyAccountContactValue(expectedAccountContacts.get(i), actualAccountContacts.get(i));
        }
    }

    public void verifyAccountContactKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT_CONTACT.toString(), in).toString();
        assertEquals(inString, out);
    }

    public void verifyAccountContactValue(GenericRecord expectedAccountContact,
                                          GenericRecord expectedContactRole,
                                          Contact expectedContact,
                                          AccountContact actualAccountContact) {

        String accountContactId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT_CONTACT.toString(),
                (Long) expectedAccountContact.get("id")).toString();
        String accountId = CanonicalId.getId(Source.SOURCE.toString(), Source.ACCOUNT.toString(),
                (Long) expectedAccountContact.get("accountId")).toString();
        assertEquals(accountContactId, actualAccountContact.getId());
        assertEquals(accountId, actualAccountContact.getAccountId());
        assertString(expectedContactRole.get("code"), actualAccountContact.getContactRole());
        assertInstant(expectedAccountContact.get("created"), actualAccountContact.getCreated());
        assertInstant(expectedAccountContact.get("updated"), actualAccountContact.getUpdated());

        // verify contact
        Contact actualContact = actualAccountContact.getContact();
        if (expectedContact != null) {
            assertNotNull(actualContact, "contact wasn't initialized");
            verifyContactValue(expectedContact, actualContact);
        }
    }

    public void verifyAccountContactValue(AccountContact expectedAccountContact,
                                          AccountContact actualAccountContact) {

        assertEquals(expectedAccountContact.getId(), actualAccountContact.getId());
        assertEquals(expectedAccountContact.getAccountId(), actualAccountContact.getAccountId());
        assertEquals(expectedAccountContact.getContactRole(), actualAccountContact.getContactRole());
        if (expectedAccountContact.getContact() != null) {
            verifyContactValue(expectedAccountContact.getContact(), actualAccountContact.getContact());
        } else {
            assertNull(actualAccountContact.getContact());
        }
        assertEquals(expectedAccountContact.getCreated(), actualAccountContact.getCreated());
        assertEquals(expectedAccountContact.getUpdated(), actualAccountContact.getUpdated());

        // verify contact
        Contact expectedContact = expectedAccountContact.getContact();
        Contact actualContact = actualAccountContact.getContact();
        if (expectedContact != null) {
            assertNotNull(actualContact, "contact wasn't initialized");
            verifyContactValue(expectedContact, actualContact);
        } else {
            assertNull(actualContact);
        }
    }

    public void verifyContactKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(), in).toString();
        assertEquals(inString, out);
    }

    public void verifyContactValue(GenericRecord expectedContact, Contact actualContact) {

        String contactId = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT.toString(),
                (Long) expectedContact.get("id")).toString();
        assertEquals(contactId, actualContact.getId());
        assertString(expectedContact.get("firstName"), actualContact.getFirstName());
        assertString(expectedContact.get("middleName"), actualContact.getMiddleName());
        assertString(expectedContact.get("lastName"), actualContact.getLastName());
        assertString(expectedContact.get("ssn"), actualContact.getSocialSecurityNumber());
        assertLocalDate(expectedContact.get("birthDate"), actualContact.getBirthDate());
        assertInstant(expectedContact.get("created"), actualContact.getCreated());
        assertInstant(expectedContact.get("updated"), actualContact.getUpdated());
    }

    public void verifyContactValue(Contact expectedContact, Contact actualContact) {

        assertEquals(expectedContact.getId(), actualContact.getId());
        assertEquals(expectedContact.getFirstName(), actualContact.getFirstName());
        assertEquals(expectedContact.getMiddleName(), actualContact.getMiddleName());
        assertEquals(expectedContact.getLastName(), actualContact.getLastName());
        assertEquals(expectedContact.getSocialSecurityNumber(), actualContact.getSocialSecurityNumber());
        assertEquals(expectedContact.getBirthDate(), actualContact.getBirthDate());
        assertEquals(expectedContact.getCreated(), actualContact.getCreated());
        assertEquals(expectedContact.getUpdated(), actualContact.getUpdated());

        List<ContactAddress> expectedAddresses = expectedContact.getAddresses();
        List<ContactAddress> actualAddresses = actualContact.getAddresses();
        if (expectedAddresses == null) {
            assertNull(actualAddresses, "contact address list should not have been initialized");
        } else if (expectedAddresses.isEmpty()) {
            assertTrue(actualAddresses.isEmpty(), "contact address list should be empty");
        } else {
            for (int i = 0; i < expectedAddresses.size(); i++) {
                verifyContactAddressValue(expectedAddresses.get(i), actualAddresses.get(i));
            }
        }
    }

    public void verifyContactValue(GenericRecord expectedContact, ContactAddress expectedAddress, Contact actualContact) {

        verifyContactValue(expectedContact, actualContact);

        // verify address is attached to expectedContact
        assertNotNull(actualContact.getAddresses(), "contact address list wasn't initialized");
        assertEquals(1, actualContact.getAddresses().size(), "contact address list is empty");
        ContactAddress actualAddress = actualContact.getAddresses().get(0);
        verifyContactAddressValue(expectedAddress, actualAddress);
    }

    public void verifyContactAddressKey(Long in, String out) {
        String inString = CanonicalId.getId(Source.SOURCE.toString(), Source.CONTACT_ADDRESS.toString(), in).toString();
        assertEquals(inString, out);
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

    public void verifyKey(Long in, Long out) {
        assertEquals(in, out);
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
