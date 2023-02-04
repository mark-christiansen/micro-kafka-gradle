package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.StreamsLifecycle;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.util.Optional;

public class Application {

    public static void main(String[] args) {
        try (ApplicationContext ctx = Micronaut.run(Application.class, args)) {
            Optional<StreamsLifecycle> bean = ctx.findBean(StreamsLifecycle.class);
            if (bean.isPresent()) {
                bean.get().start();
            }
        }
        System.exit(0);
    }
}