package com.mycompany.kafka.streams.metrics;

import com.mycompany.kafka.streams.KafkaStreamConfiguration;
import io.micrometer.core.instrument.MeterRegistry;
import io.micronaut.configuration.kafka.metrics.AbstractKafkaMetrics;
import io.micronaut.configuration.kafka.streams.metrics.KafkaStreamsMetricsReporter;
import io.micronaut.configuration.metrics.annotation.RequiresMetrics;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;

import java.util.Optional;

import static io.micronaut.configuration.metrics.micrometer.MeterRegistryFactory.MICRONAUT_METRICS_BINDERS;

@RequiresMetrics
@Context
@Requires(property = MICRONAUT_METRICS_BINDERS + ".kafka.streams.enabled", value = "true", defaultValue = "true")
public class KafkaStreamsMetrics extends AbstractKafkaMetrics<KafkaStreamConfiguration> implements BeanCreatedEventListener<KafkaStreamConfiguration> {

    private final BeanLocator beanLocator;

    public KafkaStreamsMetrics(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Override
    public KafkaStreamConfiguration onCreated(BeanCreatedEvent<KafkaStreamConfiguration> event) {
        Optional<MeterRegistry> optionalMeterRegistry = beanLocator.findBean(MeterRegistry.class);
        if (optionalMeterRegistry.isPresent()) {
            return addKafkaMetrics(event, KafkaStreamsMetricsReporter.class.getName(), optionalMeterRegistry.get());
        } else {
            return addKafkaMetrics(event, KafkaStreamsMetricsReporter.class.getName());
        }
    }
}