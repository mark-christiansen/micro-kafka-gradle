package com.mycompany.kafka.streams;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

public class StateStoreMaterializer<K, V> {

    private final boolean inMemoryStateStores;

    public StateStoreMaterializer(boolean inMemoryStateStores) {
        this.inMemoryStateStores = inMemoryStateStores;
    }

    public Materialized<K, V, KeyValueStore<Bytes, byte[]>> getStateStore(String storeName, Serde<K> keySerde, Serde<V> valueSerde) {
        KeyValueBytesStoreSupplier supplier = inMemoryStateStores ? Stores.inMemoryKeyValueStore(storeName) :
                Stores.persistentKeyValueStore(storeName);
        return Materialized.<K, V>as(supplier).withKeySerde(keySerde).withValueSerde(valueSerde);
    }
}