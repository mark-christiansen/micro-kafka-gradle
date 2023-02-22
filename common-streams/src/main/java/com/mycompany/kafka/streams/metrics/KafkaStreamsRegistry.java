package com.mycompany.kafka.streams.metrics;

import com.mycompany.kafka.streams.metrics.KafkaStreamConfiguration;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Singleton;
import org.apache.kafka.streams.Topology;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

@Singleton
public class KafkaStreamsRegistry {

    private final boolean stateStoreCleanup;
    private final Set<KafkaStreamConfiguration> registeredStreams;

    public KafkaStreamsRegistry(@Property(name = "application.state.store.cleanup") boolean stateStoreCleanup) {
        this.stateStoreCleanup = stateStoreCleanup;
        this.registeredStreams = new HashSet<>();
    }

    public KafkaStreamConfiguration register(Topology topology, Properties streamProps) {
        KafkaStreamConfiguration registeredStream = new KafkaStreamConfiguration(topology, streamProps, stateStoreCleanup);
        registeredStreams.add(registeredStream);
        return registeredStream;
    }

    public Set<KafkaStreamConfiguration> getStreams() {
        return registeredStreams;
    }
}
