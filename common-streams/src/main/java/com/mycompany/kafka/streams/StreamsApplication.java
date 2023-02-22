package com.mycompany.kafka.streams;

import com.mycompany.kafka.streams.metrics.KafkaStreamConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class StreamsApplication {

    private static final Logger log = LoggerFactory.getLogger(StreamsApplication.class);

    public static void main(String[] args) {

        ApplicationContext ctx = Micronaut.run(StreamsApplication.class, args);
        Optional<KafkaStreamConfiguration> bean;
        do {

            bean = ctx.findBean(KafkaStreamConfiguration.class);
            if (bean.isPresent()) {
                KafkaStreamConfiguration stream = bean.get();
                stream.create(ctx);
                stream.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Application shutdown event called");
                    stream.stop();
                }));
            } else {
                log.warn("Kafka Stream configuration not registered, waiting and trying again");
            }

        } while (bean.isEmpty());
    }
}
