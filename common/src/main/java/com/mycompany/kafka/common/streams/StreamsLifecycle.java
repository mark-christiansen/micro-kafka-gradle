package com.mycompany.kafka.common.streams;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Named;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import static java.lang.String.format;

public class StreamsLifecycle {

    private static final Logger log = LoggerFactory.getLogger(StreamsLifecycle.class);
    private static final String STATE_STORE_CLEANUP = "state.store.cleanup";
    private final String applicationId;
    private final Boolean stateStoreCleanup;
    private final Topology topology;
    private final KafkaStreams streams;
    private final ApplicationContext applicationContext;

    public StreamsLifecycle(Topology topology,
                            Map<String, Object> applicationProperties,
                            Properties streamsProperties,
                            @Named ApplicationContext applicationContext) {
        this.topology = topology;
        this.applicationId = streamsProperties.getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
        this.stateStoreCleanup = Boolean.parseBoolean((String) applicationProperties.get(STATE_STORE_CLEANUP));
        streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        streamsProperties.put(StreamsConfig.CLIENT_ID_CONFIG, applicationId);
        this.applicationContext = applicationContext;
        this.streams = new KafkaStreams(topology, streamsProperties);
    }

    public void start() {

        final TopologyDescription description = topology.describe();
        log.info("=======================================================================================");
        log.info("Topology: {}", description);
        log.info("=======================================================================================");

        log.info("Starting Stream {}", applicationId);
        if (streams != null) {

            streams.setUncaughtExceptionHandler(e -> {
                log.error(format("Stopping the application %s due to unhandled exception", applicationId), e);
                applicationContext.stop();
                return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
            });

            streams.setStateListener((newState, oldState) -> {
                if (newState == KafkaStreams.State.ERROR) {
                    throw new RuntimeException("Kafka Streams went into an ERROR state");
                }
            });

            if (stateStoreCleanup) {
                streams.cleanUp();
            }

            streams.start();
        }
    }

    public void stop() {
        log.warn("Closing Kafka Streams application {}", applicationId);
        if (streams != null) {
            streams.close(Duration.ofSeconds(5));
            log.info("Closed Kafka Streams application {}", applicationId);
        }
    }

    public Boolean isHealthy() {
        return streams.state().isRunningOrRebalancing();
    }

    public String topologyDescription() {
        return topology.describe().toString();
    }
}