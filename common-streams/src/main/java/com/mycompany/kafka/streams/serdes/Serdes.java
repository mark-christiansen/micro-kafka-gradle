package com.mycompany.kafka.streams.serdes;

import com.mycompany.kafka.model.canonical.Contact;
import com.mycompany.kafka.model.canonical.ContactAddress;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;

import java.util.HashMap;
import java.util.Map;

@SuppressFBWarnings(value="EI_EXPOSE_REP", justification="Cannot clone SchemaRegistryClient")
public class Serdes {
    private static final String SCHEMA_REGISTRY_AUTH = "schema.registry.auth";
    private final Map<String, Object> kafkaProps;
    private final SchemaRegistryClient client;

    public Serdes(SchemaRegistryClient client,
                  Map<String, Object> kafkaProps) {
        this.client = client;
        this.kafkaProps = new HashMap<>(kafkaProps);
    }

    public Serde<GenericRecord> createGenericSerde(boolean key) {
        GenericAvroSerde serde = new GenericAvroSerde(client);
        serde.configure(getSerdeConfig(), key);
        return serde;
    }

    public SpecificAvroSerde<Contact> createContactSerde() {
        SpecificAvroSerde<Contact> serde = new SpecificAvroSerde<>(client);
        serde.configure(getSerdeConfig(), false);
        return serde;
    }

    public SpecificAvroSerde<ContactAddress> createContactAddressSerde() {
        SpecificAvroSerde<ContactAddress> serde = new SpecificAvroSerde<>(client);
        serde.configure(getSerdeConfig(), false);
        return serde;
    }

    private Map<String, Object> getSerdeConfig() {

        final Map<String, Object> serdeConfig = new HashMap<>();
        serdeConfig.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaProps.get(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG));
        // these settings are necessary to support heterogeneous topics
        serdeConfig.put(KafkaAvroDeserializerConfig.KEY_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class);
        serdeConfig.put(KafkaAvroDeserializerConfig.VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class);

        boolean auth = Boolean.parseBoolean((String) kafkaProps.get(SCHEMA_REGISTRY_AUTH));
        if (auth) {
            serdeConfig.put(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE,
                    kafkaProps.get(AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE));
            serdeConfig.put(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG,
                    kafkaProps.get(AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG));
        }
        serdeConfig.put(KafkaAvroDeserializerConfig.AUTO_REGISTER_SCHEMAS, kafkaProps.get("auto.register.schemas"));
        return serdeConfig;
    }
}
