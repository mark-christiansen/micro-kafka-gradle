package com.mycompany.kafka.producer;

import com.mycompany.kafka.producer.data.*;
import com.mycompany.kafka.schemas.SchemaLoader;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import jakarta.inject.Singleton;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.TopicExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

@Singleton
public class DataProducer {

    private static final Logger log = LoggerFactory.getLogger(DataProducer.class);

    private final AdminClient adminClient;
    private final Producer<Long, GenericRecord> producer;
    private final SchemaLoader schemaLoader;
    private final String accountTopic;
    private final String accountContactTopic;
    private final String contactTopic;
    private final String contactAddressTopic;
    private final String contactRoleTopic;
    private final String countryTopic;
    private final String stateTopic;
    private final String canonicalAccountTopic;
    private final String canonicalAccountContactTopic;
    private final String canonicalContactTopic;
    private final String canonicalContactAddressTopic;
    private final int partitions;
    private final long accounts;
    private final int batchSize;
    private final long frequencyMs;
    private final ExecutorService executorService;

    public DataProducer(AdminClient adminClient,
                        Producer<Long, GenericRecord> producer,
                        SchemaLoader schemaLoader,
                        Environment environment) {

        this.adminClient = adminClient;
        this.producer = producer;
        this.schemaLoader = schemaLoader;

        Map<String, Object> appConfig = environment.getProperties("application");
        this.accountTopic = (String) appConfig.get("account.topic");
        this.accountContactTopic = (String) appConfig.get("account.contact.topic");
        this.contactTopic = (String) appConfig.get("contact.topic");
        this.contactAddressTopic = (String) appConfig.get("contact.address.topic");
        this.contactRoleTopic = (String) appConfig.get("contact.role.topic");
        this.countryTopic = (String) appConfig.get("country.topic");
        this.stateTopic = (String) appConfig.get("state.topic");
        this.canonicalAccountTopic = (String) appConfig.get("canonical.account.topic");
        this.canonicalAccountContactTopic = (String) appConfig.get("canonical.account.contact.topic");
        this.canonicalContactTopic = (String) appConfig.get("canonical.contact.topic");
        this.canonicalContactAddressTopic = (String) appConfig.get("canonical.contact.address.topic");
        this.partitions = Integer.parseInt((String) appConfig.get("partitions"));
        this.accounts = Long.parseLong((String) appConfig.get("accounts"));
        this.batchSize = Integer.parseInt((String) appConfig.get("batch.size"));
        this.frequencyMs = Long.parseLong((String) appConfig.get("frequency.ms"));
        this.executorService = new ThreadPoolExecutor(1, Integer.parseInt((String) appConfig.get("threads")),
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void start() throws InterruptedException {

        int contactRoleCount = 10;
        int countryCount = 100;
        int stateCount = 50;

        long contactCount = accounts * 3;
        long addressCount = contactCount * 2;

        // create topics
        log.info("Creating topics...");
        createTopic(accountTopic, partitions);
        createTopic(accountContactTopic, partitions);
        createTopic(contactTopic, partitions);
        createTopic(contactAddressTopic, partitions);
        createTopic(canonicalAccountTopic, partitions);
        createTopic(canonicalAccountContactTopic, partitions);
        createTopic(canonicalContactTopic, partitions);
        createTopic(canonicalContactAddressTopic, partitions);
        createTopic(contactRoleTopic, 1);
        createTopic(countryTopic, 1);
        createTopic(stateTopic, 1);

        List<Callable<Boolean>> producers = new ArrayList<>();
        log.info("");
        log.info("Producing {} contact roles to {}", contactRoleCount, contactRoleTopic);
        log.info("Producing {} countries to {}", countryCount, countryTopic);
        log.info("Producing {} states to {}", stateCount, stateTopic);
        log.info("");

        // produce the global topic data first and wait until finished
        producers.add(new ContactRoleProducer(producer, schemaLoader, contactRoleTopic, contactRoleCount,
                contactRoleCount, 0));
        producers.add(new CountryProducer(producer, schemaLoader, countryTopic, countryCount, countryCount, 0));
        producers.add(new StateProducer(producer, schemaLoader, stateTopic, stateCount, stateCount, 0));
        List<Future<Boolean>> results = this.executorService.invokeAll(producers);
        log.info("Finished producing contact roles, countries and states");

        // produce the remaining data to the single heterogeneous source topic
        producers.clear();
        log.info("");
        log.info("Producing {} accounts to {}", accounts, accountTopic);
        log.info("Producing {} contacts to {}", contactCount, contactTopic);
        log.info("Producing {} addresses to {}", addressCount, contactAddressTopic);
        log.info("");

        producers.add(new AccountProducer(producer, schemaLoader, accountTopic, accounts, batchSize, frequencyMs));
        producers.add(new ContactProducer(producer, schemaLoader, contactTopic, contactCount, batchSize, frequencyMs));
        producers.add(new ContactAddressProducer(producer, schemaLoader, contactAddressTopic, addressCount, batchSize, frequencyMs,
                contactCount, stateCount, countryCount));
        producers.add(new AccountContactProducer(producer, schemaLoader, accountContactTopic, contactCount, batchSize, frequencyMs,
                accounts, contactCount, contactRoleCount));
        results = this.executorService.invokeAll(producers);
        log.info("Finished producing accounts, contacts and addresses");

        log.info("");
        log.info("Produced {} contact roles to {}", contactRoleCount, contactRoleTopic);
        log.info("Produced {} countries to {}", countryCount, countryTopic);
        log.info("Produced {} states to {}", stateCount, stateTopic);
        log.info("Produced {} accounts to {}", accounts, accountTopic);
        log.info("Produced {} contacts to {}", contactCount, contactTopic);
        log.info("Produced {} addresses to {}", addressCount, contactAddressTopic);
        log.info("");

    }

    private void createTopic(String topicName, int partitions) {

        final NewTopic newTopic = new NewTopic(topicName, Optional.of(partitions), Optional.empty());
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            log.info("Created topic {} with {} partitions", topicName, partitions);
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            } else {
                log.info("Topic {} already exists", topicName);
            }
        }
    }
}
