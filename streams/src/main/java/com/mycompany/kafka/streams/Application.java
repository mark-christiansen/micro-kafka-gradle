package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.StreamsLifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        try (ApplicationContext ctx = Micronaut.run(Application.class, args)) {

            Optional<StreamsLifecycle> bean = ctx.findBean(StreamsLifecycle.class);
            if (bean.isPresent()) {

                StreamsLifecycle lifecycle = bean.get();
                lifecycle.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Application shutdown event called");
                    lifecycle.stop();
                }));
            }
        }
    }
}