package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.StreamsLifecycle;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.ShutdownEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ApplicationHooks implements ApplicationEventListener<ShutdownEvent> {

    private static final Logger log = LoggerFactory.getLogger(ApplicationHooks.class);

    @Inject
    private StreamsLifecycle streamsLifecycle;

    @Override
    public void onApplicationEvent(ShutdownEvent event) {
        log.info("Application shutdown event called");
        streamsLifecycle.stop();
    }

    @Override
    public boolean supports(ShutdownEvent event) {
        return ApplicationEventListener.super.supports(event);
    }
}
