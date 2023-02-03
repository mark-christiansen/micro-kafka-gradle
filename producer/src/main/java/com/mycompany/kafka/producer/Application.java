package com.mycompany.kafka.producer;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.util.Optional;

public class Application {

    public static void main(String[] args) throws Exception {
        try (ApplicationContext ctx = Micronaut.run(Application.class, args)) {
            Optional<GenericRecordProducer> bean = ctx.findBean(GenericRecordProducer.class);
            if (bean.isPresent()) {
                bean.get().start();
            }
        }
        System.exit(0);
    }
}