package com.jaideep.ecommerce.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class OutboxService {
    private final OutboxEventRepository repository;
    private final String productTopic;

    public OutboxService(
        OutboxEventRepository repository,
        @Value("${app.kafka.product-topic}") String productTopic
    ) {
        this.repository = repository;
        this.productTopic = productTopic;
    }

    public String enqueueProductUpsert(Long productId) {
        String eventId = UUID.randomUUID().toString();
        String payload = eventId + "|UPSERT|" + productId;

        repository.save(new OutboxEvent(
            eventId,
            "Product",
            productId,
            "UPSERT",
            productTopic,
            payload,
            Instant.now()
        ));

        return eventId;
    }

    public long pendingCount() { return repository.countByPublishedAtIsNull(); }
    public long publishedCount() { return repository.countByPublishedAtIsNotNull(); }
    public long totalCount() { return repository.count(); }
}
