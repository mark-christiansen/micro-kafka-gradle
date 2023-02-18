package com.mycompany.kafka.streams;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;

import java.util.Map;

public class RocksDBConfig implements RocksDBConfigSetter {
    @Override
    public void setConfig(String storeName, Options options, Map<String, Object> configs) {
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        options.setBlobCompressionType(CompressionType.LZ4_COMPRESSION);
    }

    @Override
    public void close(String storeName, Options options) {
    }
}
