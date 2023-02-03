package com.mycompany.kafka.consumer;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ShutdownEvent;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApplicationHooks implements ApplicationEventListener<ShutdownEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationHooks.class);

    private final GenericRecordConsumer consumer;

    public ApplicationHooks(GenericRecordConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onApplicationEvent(ShutdownEvent event) {
        log.info("Application shutdown event called");
        consumer.stop();
    }

    @Override
    public boolean supports(ShutdownEvent event) {
        return ApplicationEventListener.super.supports(event);
    }
}