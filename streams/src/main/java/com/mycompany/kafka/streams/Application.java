package com.mycompany.kafka.streams;

import com.mycompany.kafka.common.streams.StreamsLifecycle;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "com/mycompany/kafka/streams", description = "Test Kafka Streams Application",
        mixinStandardHelpOptions = true)
public class Application implements Runnable {

    @Inject
    private StreamsLifecycle streamsLifecycle;
    @Inject
    private ApplicationContext applicationContext;

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        ApplicationContext applicationContext = Micronaut.build(args)
                .mainClass(Application.class)
                .start();
        PicocliRunner.run(Application.class, applicationContext, args);
    }

    public void run() {
        streamsLifecycle.start();
    }
}