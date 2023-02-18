package com.mycompany.kafka.streams;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        ApplicationContext ctx = Micronaut.run(Application.class, args);
        Optional<KafkaStreamConfiguration> bean = ctx.findBean(KafkaStreamConfiguration.class);
        if (bean.isPresent()) {

            KafkaStreamConfiguration stream = bean.get();
            stream.create(ctx);
            stream.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Application shutdown event called");
                stream.stop();
            }));
        }
    }
}