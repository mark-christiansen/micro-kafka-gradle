/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.mycompany.kafka.model.canonical;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class Account extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 2814597640059993498L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Account\",\"namespace\":\"com.mycompany.kafka.model.canonical\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"accountNumber\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"accountType\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"contacts\",\"type\":[\"null\",{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"Contact\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"firstName\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"middleName\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"lastName\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"addresses\",\"type\":[\"null\",{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"ContactAddress\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"contactId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"addressLine1\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"addressLine2\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"addressLine3\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"city\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"state\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"country\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"postalCode\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"created\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"updated\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"source\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceType\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceId\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]},\"default\":[]}],\"default\":null},{\"name\":\"created\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"updated\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"source\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceType\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceId\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]},\"default\":[]}],\"default\":null},{\"name\":\"created\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"updated\",\"type\":[\"null\",{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}],\"default\":null},{\"name\":\"source\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceType\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"sourceId\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();
  static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.data.TimeConversions.TimestampMillisConversion());
  }

  private static final BinaryMessageEncoder<Account> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<Account> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<Account> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<Account> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<Account> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this Account to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a Account from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a Account instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static Account fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.String id;
  private java.lang.String accountNumber;
  private java.lang.String accountType;
  private java.util.List<com.mycompany.kafka.model.canonical.Contact> contacts;
  private java.time.Instant created;
  private java.time.Instant updated;
  private java.lang.String source;
  private java.lang.String sourceType;
  private java.lang.String sourceId;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public Account() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param accountNumber The new value for accountNumber
   * @param accountType The new value for accountType
   * @param contacts The new value for contacts
   * @param created The new value for created
   * @param updated The new value for updated
   * @param source The new value for source
   * @param sourceType The new value for sourceType
   * @param sourceId The new value for sourceId
   */
  public Account(java.lang.String id, java.lang.String accountNumber, java.lang.String accountType, java.util.List<com.mycompany.kafka.model.canonical.Contact> contacts, java.time.Instant created, java.time.Instant updated, java.lang.String source, java.lang.String sourceType, java.lang.String sourceId) {
    this.id = id;
    this.accountNumber = accountNumber;
    this.accountType = accountType;
    this.contacts = contacts;
    this.created = created;
    this.updated = updated;
    this.source = source;
    this.sourceType = sourceType;
    this.sourceId = sourceId;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return accountNumber;
    case 2: return accountType;
    case 3: return contacts;
    case 4: return created;
    case 5: return updated;
    case 6: return source;
    case 7: return sourceType;
    case 8: return sourceId;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = value$ != null ? value$.toString() : null; break;
    case 1: accountNumber = value$ != null ? value$.toString() : null; break;
    case 2: accountType = value$ != null ? value$.toString() : null; break;
    case 3: contacts = (java.util.List<com.mycompany.kafka.model.canonical.Contact>)value$; break;
    case 4: created = (java.time.Instant)value$; break;
    case 5: updated = (java.time.Instant)value$; break;
    case 6: source = value$ != null ? value$.toString() : null; break;
    case 7: sourceType = value$ != null ? value$.toString() : null; break;
    case 8: sourceId = value$ != null ? value$.toString() : null; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }


  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'accountNumber' field.
   * @return The value of the 'accountNumber' field.
   */
  public java.lang.String getAccountNumber() {
    return accountNumber;
  }


  /**
   * Sets the value of the 'accountNumber' field.
   * @param value the value to set.
   */
  public void setAccountNumber(java.lang.String value) {
    this.accountNumber = value;
  }

  /**
   * Gets the value of the 'accountType' field.
   * @return The value of the 'accountType' field.
   */
  public java.lang.String getAccountType() {
    return accountType;
  }


  /**
   * Sets the value of the 'accountType' field.
   * @param value the value to set.
   */
  public void setAccountType(java.lang.String value) {
    this.accountType = value;
  }

  /**
   * Gets the value of the 'contacts' field.
   * @return The value of the 'contacts' field.
   */
  public java.util.List<com.mycompany.kafka.model.canonical.Contact> getContacts() {
    return contacts;
  }


  /**
   * Sets the value of the 'contacts' field.
   * @param value the value to set.
   */
  public void setContacts(java.util.List<com.mycompany.kafka.model.canonical.Contact> value) {
    this.contacts = value;
  }

  /**
   * Gets the value of the 'created' field.
   * @return The value of the 'created' field.
   */
  public java.time.Instant getCreated() {
    return created;
  }


  /**
   * Sets the value of the 'created' field.
   * @param value the value to set.
   */
  public void setCreated(java.time.Instant value) {
    this.created = value;
  }

  /**
   * Gets the value of the 'updated' field.
   * @return The value of the 'updated' field.
   */
  public java.time.Instant getUpdated() {
    return updated;
  }


  /**
   * Sets the value of the 'updated' field.
   * @param value the value to set.
   */
  public void setUpdated(java.time.Instant value) {
    this.updated = value;
  }

  /**
   * Gets the value of the 'source' field.
   * @return The value of the 'source' field.
   */
  public java.lang.String getSource() {
    return source;
  }


  /**
   * Sets the value of the 'source' field.
   * @param value the value to set.
   */
  public void setSource(java.lang.String value) {
    this.source = value;
  }

  /**
   * Gets the value of the 'sourceType' field.
   * @return The value of the 'sourceType' field.
   */
  public java.lang.String getSourceType() {
    return sourceType;
  }


  /**
   * Sets the value of the 'sourceType' field.
   * @param value the value to set.
   */
  public void setSourceType(java.lang.String value) {
    this.sourceType = value;
  }

  /**
   * Gets the value of the 'sourceId' field.
   * @return The value of the 'sourceId' field.
   */
  public java.lang.String getSourceId() {
    return sourceId;
  }


  /**
   * Sets the value of the 'sourceId' field.
   * @param value the value to set.
   */
  public void setSourceId(java.lang.String value) {
    this.sourceId = value;
  }

  /**
   * Creates a new Account RecordBuilder.
   * @return A new Account RecordBuilder
   */
  public static com.mycompany.kafka.model.canonical.Account.Builder newBuilder() {
    return new com.mycompany.kafka.model.canonical.Account.Builder();
  }

  /**
   * Creates a new Account RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new Account RecordBuilder
   */
  public static com.mycompany.kafka.model.canonical.Account.Builder newBuilder(com.mycompany.kafka.model.canonical.Account.Builder other) {
    if (other == null) {
      return new com.mycompany.kafka.model.canonical.Account.Builder();
    } else {
      return new com.mycompany.kafka.model.canonical.Account.Builder(other);
    }
  }

  /**
   * Creates a new Account RecordBuilder by copying an existing Account instance.
   * @param other The existing instance to copy.
   * @return A new Account RecordBuilder
   */
  public static com.mycompany.kafka.model.canonical.Account.Builder newBuilder(com.mycompany.kafka.model.canonical.Account other) {
    if (other == null) {
      return new com.mycompany.kafka.model.canonical.Account.Builder();
    } else {
      return new com.mycompany.kafka.model.canonical.Account.Builder(other);
    }
  }

  /**
   * RecordBuilder for Account instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Account>
    implements org.apache.avro.data.RecordBuilder<Account> {

    private java.lang.String id;
    private java.lang.String accountNumber;
    private java.lang.String accountType;
    private java.util.List<com.mycompany.kafka.model.canonical.Contact> contacts;
    private java.time.Instant created;
    private java.time.Instant updated;
    private java.lang.String source;
    private java.lang.String sourceType;
    private java.lang.String sourceId;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.mycompany.kafka.model.canonical.Account.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.accountNumber)) {
        this.accountNumber = data().deepCopy(fields()[1].schema(), other.accountNumber);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.accountType)) {
        this.accountType = data().deepCopy(fields()[2].schema(), other.accountType);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.contacts)) {
        this.contacts = data().deepCopy(fields()[3].schema(), other.contacts);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.created)) {
        this.created = data().deepCopy(fields()[4].schema(), other.created);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.updated)) {
        this.updated = data().deepCopy(fields()[5].schema(), other.updated);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
      if (isValidValue(fields()[6], other.source)) {
        this.source = data().deepCopy(fields()[6].schema(), other.source);
        fieldSetFlags()[6] = other.fieldSetFlags()[6];
      }
      if (isValidValue(fields()[7], other.sourceType)) {
        this.sourceType = data().deepCopy(fields()[7].schema(), other.sourceType);
        fieldSetFlags()[7] = other.fieldSetFlags()[7];
      }
      if (isValidValue(fields()[8], other.sourceId)) {
        this.sourceId = data().deepCopy(fields()[8].schema(), other.sourceId);
        fieldSetFlags()[8] = other.fieldSetFlags()[8];
      }
    }

    /**
     * Creates a Builder by copying an existing Account instance
     * @param other The existing instance to copy.
     */
    private Builder(com.mycompany.kafka.model.canonical.Account other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.accountNumber)) {
        this.accountNumber = data().deepCopy(fields()[1].schema(), other.accountNumber);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.accountType)) {
        this.accountType = data().deepCopy(fields()[2].schema(), other.accountType);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.contacts)) {
        this.contacts = data().deepCopy(fields()[3].schema(), other.contacts);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.created)) {
        this.created = data().deepCopy(fields()[4].schema(), other.created);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.updated)) {
        this.updated = data().deepCopy(fields()[5].schema(), other.updated);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.source)) {
        this.source = data().deepCopy(fields()[6].schema(), other.source);
        fieldSetFlags()[6] = true;
      }
      if (isValidValue(fields()[7], other.sourceType)) {
        this.sourceType = data().deepCopy(fields()[7].schema(), other.sourceType);
        fieldSetFlags()[7] = true;
      }
      if (isValidValue(fields()[8], other.sourceId)) {
        this.sourceId = data().deepCopy(fields()[8].schema(), other.sourceId);
        fieldSetFlags()[8] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.String getId() {
      return id;
    }


    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setId(java.lang.String value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'accountNumber' field.
      * @return The value.
      */
    public java.lang.String getAccountNumber() {
      return accountNumber;
    }


    /**
      * Sets the value of the 'accountNumber' field.
      * @param value The value of 'accountNumber'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setAccountNumber(java.lang.String value) {
      validate(fields()[1], value);
      this.accountNumber = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'accountNumber' field has been set.
      * @return True if the 'accountNumber' field has been set, false otherwise.
      */
    public boolean hasAccountNumber() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'accountNumber' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearAccountNumber() {
      accountNumber = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'accountType' field.
      * @return The value.
      */
    public java.lang.String getAccountType() {
      return accountType;
    }


    /**
      * Sets the value of the 'accountType' field.
      * @param value The value of 'accountType'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setAccountType(java.lang.String value) {
      validate(fields()[2], value);
      this.accountType = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'accountType' field has been set.
      * @return True if the 'accountType' field has been set, false otherwise.
      */
    public boolean hasAccountType() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'accountType' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearAccountType() {
      accountType = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'contacts' field.
      * @return The value.
      */
    public java.util.List<com.mycompany.kafka.model.canonical.Contact> getContacts() {
      return contacts;
    }


    /**
      * Sets the value of the 'contacts' field.
      * @param value The value of 'contacts'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setContacts(java.util.List<com.mycompany.kafka.model.canonical.Contact> value) {
      validate(fields()[3], value);
      this.contacts = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'contacts' field has been set.
      * @return True if the 'contacts' field has been set, false otherwise.
      */
    public boolean hasContacts() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'contacts' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearContacts() {
      contacts = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'created' field.
      * @return The value.
      */
    public java.time.Instant getCreated() {
      return created;
    }


    /**
      * Sets the value of the 'created' field.
      * @param value The value of 'created'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setCreated(java.time.Instant value) {
      validate(fields()[4], value);
      this.created = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'created' field has been set.
      * @return True if the 'created' field has been set, false otherwise.
      */
    public boolean hasCreated() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'created' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearCreated() {
      created = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'updated' field.
      * @return The value.
      */
    public java.time.Instant getUpdated() {
      return updated;
    }


    /**
      * Sets the value of the 'updated' field.
      * @param value The value of 'updated'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setUpdated(java.time.Instant value) {
      validate(fields()[5], value);
      this.updated = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'updated' field has been set.
      * @return True if the 'updated' field has been set, false otherwise.
      */
    public boolean hasUpdated() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'updated' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearUpdated() {
      updated = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'source' field.
      * @return The value.
      */
    public java.lang.String getSource() {
      return source;
    }


    /**
      * Sets the value of the 'source' field.
      * @param value The value of 'source'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setSource(java.lang.String value) {
      validate(fields()[6], value);
      this.source = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'source' field has been set.
      * @return True if the 'source' field has been set, false otherwise.
      */
    public boolean hasSource() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'source' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearSource() {
      source = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    /**
      * Gets the value of the 'sourceType' field.
      * @return The value.
      */
    public java.lang.String getSourceType() {
      return sourceType;
    }


    /**
      * Sets the value of the 'sourceType' field.
      * @param value The value of 'sourceType'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setSourceType(java.lang.String value) {
      validate(fields()[7], value);
      this.sourceType = value;
      fieldSetFlags()[7] = true;
      return this;
    }

    /**
      * Checks whether the 'sourceType' field has been set.
      * @return True if the 'sourceType' field has been set, false otherwise.
      */
    public boolean hasSourceType() {
      return fieldSetFlags()[7];
    }


    /**
      * Clears the value of the 'sourceType' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearSourceType() {
      sourceType = null;
      fieldSetFlags()[7] = false;
      return this;
    }

    /**
      * Gets the value of the 'sourceId' field.
      * @return The value.
      */
    public java.lang.String getSourceId() {
      return sourceId;
    }


    /**
      * Sets the value of the 'sourceId' field.
      * @param value The value of 'sourceId'.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder setSourceId(java.lang.String value) {
      validate(fields()[8], value);
      this.sourceId = value;
      fieldSetFlags()[8] = true;
      return this;
    }

    /**
      * Checks whether the 'sourceId' field has been set.
      * @return True if the 'sourceId' field has been set, false otherwise.
      */
    public boolean hasSourceId() {
      return fieldSetFlags()[8];
    }


    /**
      * Clears the value of the 'sourceId' field.
      * @return This builder.
      */
    public com.mycompany.kafka.model.canonical.Account.Builder clearSourceId() {
      sourceId = null;
      fieldSetFlags()[8] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Account build() {
      try {
        Account record = new Account();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.String) defaultValue(fields()[0]);
        record.accountNumber = fieldSetFlags()[1] ? this.accountNumber : (java.lang.String) defaultValue(fields()[1]);
        record.accountType = fieldSetFlags()[2] ? this.accountType : (java.lang.String) defaultValue(fields()[2]);
        record.contacts = fieldSetFlags()[3] ? this.contacts : (java.util.List<com.mycompany.kafka.model.canonical.Contact>) defaultValue(fields()[3]);
        record.created = fieldSetFlags()[4] ? this.created : (java.time.Instant) defaultValue(fields()[4]);
        record.updated = fieldSetFlags()[5] ? this.updated : (java.time.Instant) defaultValue(fields()[5]);
        record.source = fieldSetFlags()[6] ? this.source : (java.lang.String) defaultValue(fields()[6]);
        record.sourceType = fieldSetFlags()[7] ? this.sourceType : (java.lang.String) defaultValue(fields()[7]);
        record.sourceId = fieldSetFlags()[8] ? this.sourceId : (java.lang.String) defaultValue(fields()[8]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<Account>
    WRITER$ = (org.apache.avro.io.DatumWriter<Account>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<Account>
    READER$ = (org.apache.avro.io.DatumReader<Account>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










