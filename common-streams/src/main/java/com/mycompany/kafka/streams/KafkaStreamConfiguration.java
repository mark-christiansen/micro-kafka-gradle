package com.mycompany.kafka.streams;

import io.micronaut.configuration.kafka.config.AbstractKafkaConfiguration;
import io.micronaut.context.ApplicationContext;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Properties;

import static java.lang.String.format;

public class KafkaStreamConfiguration extends AbstractKafkaConfiguration {

    private static final Logger log = LoggerFactory.getLogger(KafkaStreamConfiguration.class);

    private KafkaStreams stream;
    private final Topology topology;
    private final String applicationId;
    private final boolean stateStoreCleanup;

    public KafkaStreamConfiguration(Topology topology, Properties config, boolean stateStoreCleanup) {
        super(config);
        this.topology = topology;
        this.applicationId = config.getProperty(StreamsConfig.APPLICATION_ID_CONFIG);
        this.stateStoreCleanup = stateStoreCleanup;
    }

    public Topology getTopology() {
        return topology;
    }

    public KafkaStreams getStream() {
        return stream;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void create(ApplicationContext applicationContext) {

        final TopologyDescription description = topology.describe();
        log.info("");
        log.info("=======================================================================================");
        log.info("Topology: {}", applicationId);
        log.info("=======================================================================================");
        log.info("{}", description);
        log.info("");

        this.stream = new KafkaStreams(topology, getConfig());
        stream.setUncaughtExceptionHandler(e -> {
            log.error(format("Stopping the application %s due to unhandled exception", applicationId), e);
            applicationContext.stop();
            return StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
        });

        stream.setStateListener((newState, oldState) -> {
            if (newState == KafkaStreams.State.ERROR) {
                throw new RuntimeException("Kafka Streams went into an ERROR state");
            }
        });

        if (stateStoreCleanup) {
            stream.cleanUp();
        }
    }

    public void start() {
        log.info("Starting Kafka Streams application {}", applicationId);
        stream.start();
        log.info("Started Kafka Streams application {}", applicationId);
    }

    public void stop() {
        log.info("Closing Kafka Streams application {}", applicationId);
        stream.close(Duration.ofSeconds(5));
        log.info("Closed Kafka Streams application {}", applicationId);
    }
}
