package com.jaideep.ecommerce.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublisher {
    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
        OutboxEventRepository repository,
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(
        fixedDelayString = "${app.outbox.poll-delay-ms:1000}",
        initialDelayString = "${app.outbox.initial-delay-ms:2000}"
    )
    public void publishPending() {
        List<OutboxEvent> events =
            repository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate
                    .send(event.getTopic(), event.getAggregateId().toString(), event.getPayload())
                    .get(10, TimeUnit.SECONDS);

                event.markPublished();
                repository.save(event);
                System.out.println("Outbox event published: " + event.getId());
            }
            catch (Exception exception) {
                event.markAttemptFailed();
                repository.save(event);
                System.err.println(
                    "Outbox publication failed: " + event.getId()
                    + " attempt=" + event.getAttempts()
                    + " reason=" + exception.getMessage()
                );
            }
        }
    }
}
