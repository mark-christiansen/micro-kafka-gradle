package com.mycompany.kafka.consumer;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

import java.util.Optional;

public class Application {

    public static void main(String[] args) {
        try (ApplicationContext ctx = Micronaut.run(Application.class, args)) {
            Optional<GenericRecordConsumer> bean = ctx.findBean(GenericRecordConsumer.class);
            bean.ifPresent(AbstractGenericRecordConsumer::start);
        }
        System.exit(0);
    }
}