package com.mycompany.kafka.streams.health;

import com.mycompany.kafka.streams.metrics.KafkaStreamsRegistry;
import com.mycompany.kafka.streams.metrics.KafkaStreamConfiguration;
import io.micronaut.configuration.kafka.config.AbstractKafkaConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.aggregator.HealthAggregator;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import jakarta.inject.Singleton;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TaskMetadata;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Requires(classes = HealthIndicator.class)
@Requires(property = KafkaStreamsHealth.ENABLED_PROPERTY, value = "true", defaultValue = "true")
public class KafkaStreamsHealth implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsHealth.class);
    public static final String ENABLED_PROPERTY = AbstractKafkaConfiguration.PREFIX + ".health.streams.enabled";
    private static final String NAME = "kafkaStreams";
    private static final String METADATA_PARTITIONS = "partitions";

    private final KafkaStreamsRegistry kafkaStreamsRegistry;
    private final HealthAggregator<?> healthAggregator;

    public KafkaStreamsHealth(final KafkaStreamsRegistry kafkaStreamsRegistry,
                              final HealthAggregator<?> healthAggregator) {
        this.kafkaStreamsRegistry = kafkaStreamsRegistry;
        this.healthAggregator = healthAggregator;
    }

    @Override
    public Publisher<HealthResult> getResult() {

        Set<KafkaStreamConfiguration> streams = this.kafkaStreamsRegistry.getStreams();
        Flux<HealthResult> kafkaStreamHealth = Flux.fromIterable(streams)
                .flatMap(s -> Flux.just(s)
                        .filter(p -> s.getStream().state().isRunningOrRebalancing())
                        .map(p -> HealthResult.builder(s.getApplicationId(), HealthStatus.UP)
                                .details(getUpDetails(s.getStream())))
                        .switchIfEmpty(Flux.create(emitter -> {
                            if (s.getStream().state().isRunningOrRebalancing()) {
                                HealthResult.Builder upResult = HealthResult.builder(s.getApplicationId(), HealthStatus.UP)
                                        .details(getUpDetails(s.getStream()));
                                emitter.next(upResult);
                            } else {
                                HealthResult.Builder downResult = HealthResult.builder(s.getApplicationId(), HealthStatus.DOWN)
                                        .details(getDownDetails(s.getStream().state(), s.getApplicationId()));
                                emitter.next(downResult);
                            }
                            emitter.complete();
                        }))
                        .onErrorResume(e -> Flux.just(HealthResult.builder(s.getApplicationId(), HealthStatus.DOWN)
                                .details(getDownDetails(e.getMessage(), s.getStream().state(), s.getApplicationId(), e)))))
                .map(HealthResult.Builder::build);
        return healthAggregator.aggregate(NAME, kafkaStreamHealth);
    }

    @Override
    public int getOrder() {
        return HealthIndicator.super.getOrder();
    }

    private Map<String, Object> getUpDetails(KafkaStreams kafkaStreams) {
        final Map<String, Object> streamDetails = new HashMap<>();

        if (kafkaStreams.state().isRunningOrRebalancing()) {
            for (org.apache.kafka.streams.ThreadMetadata metadata : kafkaStreams.metadataForLocalThreads()) {
                final Map<String, Object> threadDetails = new HashMap<>();
                threadDetails.put("threadName", metadata.threadName());
                threadDetails.put("threadState", metadata.threadState());
                threadDetails.put("adminClientId", metadata.adminClientId());
                threadDetails.put("consumerClientId", metadata.consumerClientId());
                threadDetails.put("restoreConsumerClientId", metadata.restoreConsumerClientId());
                threadDetails.put("producerClientIds", metadata.producerClientIds());
                threadDetails.put("activeTasks", taskDetails(metadata.activeTasks()));
                threadDetails.put("standbyTasks", taskDetails(metadata.standbyTasks()));
                streamDetails.put(metadata.threadName(), threadDetails);
            }
        } else {
            streamDetails.put("error", "The processor is down");
        }
        return streamDetails;
    }

    private Map<String, Object> taskDetails(Set<TaskMetadata> taskMetadataSet) {
        final Map<String, Object> details = new HashMap<>();
        for (TaskMetadata taskMetadata : taskMetadataSet) {
            details.put("taskId", taskMetadata.taskId());
            if (details.containsKey(METADATA_PARTITIONS)) {
                @SuppressWarnings("unchecked")
                List<String> partitionsInfo = (List<String>) details.get(METADATA_PARTITIONS);
                partitionsInfo.addAll(addPartitionsInfo(taskMetadata));
            } else {
                details.put(METADATA_PARTITIONS, addPartitionsInfo(taskMetadata));
            }
        }
        return details;
    }

    private List<String> addPartitionsInfo(TaskMetadata metadata) {
        // don't use stream().toList() as it returns an immutable collection
        return metadata.topicPartitions().stream()
                .map(p -> "partition=" + p.partition() + ", topic=" + p.topic())
                .collect(Collectors.toList());
    }

    private Map<String, String> getDownDetails(KafkaStreams.State state, String streamId) {
        return getDownDetails("Processor appears to be down", state, streamId, null);
    }

    private Map<String, String> getDownDetails(String message, KafkaStreams.State state, String streamId, Throwable e) {
        if (e != null) {
            LOG.debug("Reporting Kafka health DOWN. Kafka stream [{}] in state [{}] is DOWN. Reason: {}", streamId, state, message);
            LOG.debug(e.getMessage(), e);
        } else {
            LOG.debug("Reporting Kafka health DOWN. Kafka stream [{}] in state [{}] is DOWN. Reason: {}", streamId, state, message);
        }
        final Map<String, String> details = new HashMap<>();
        details.put("threadState", state.name());
        details.put("error", message);
        return details;
    }
}
