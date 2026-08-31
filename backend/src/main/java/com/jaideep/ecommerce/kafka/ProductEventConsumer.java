package com.jaideep.ecommerce.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.jaideep.ecommerce.database.CatalogDatabaseService;
import com.jaideep.ecommerce.database.ProductEntity;
import com.jaideep.ecommerce.outbox.ProcessedEvent;
import com.jaideep.ecommerce.outbox.ProcessedEventRepository;
import com.jaideep.ecommerce.service.SearchCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProductEventConsumer {
    private final ElasticsearchClient client;
    private final CatalogDatabaseService databaseService;
    private final SearchCacheService cacheService;
    private final ProcessedEventRepository processedEventRepository;

    private final AtomicLong processedEvents = new AtomicLong();
    private final AtomicLong duplicatesIgnored = new AtomicLong();
    private final AtomicReference<String> lastEvent = new AtomicReference<>("NONE");

    @Value("${elasticsearch.index}")
    private String index;

    public ProductEventConsumer(
        ElasticsearchClient client,
        CatalogDatabaseService databaseService,
        SearchCacheService cacheService,
        ProcessedEventRepository processedEventRepository
    ) {
        this.client = client;
        this.databaseService = databaseService;
        this.cacheService = cacheService;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${app.kafka.product-topic}")
    public void consume(String payload) throws IOException {
        String[] parts = payload.split("\\|", 3);

        String eventId = null;
        String operation;
        Long documentId;

        if (parts.length == 3) {
            eventId = parts[0];
            operation = parts[1];
            documentId = Long.valueOf(parts[2]);
        }
        else if (parts.length == 2) {
            operation = parts[0];
            documentId = Long.valueOf(parts[1]);
        }
        else {
            throw new IllegalArgumentException("Invalid product event: " + payload);
        }

        if (eventId != null && processedEventRepository.existsById(eventId)) {
            duplicatesIgnored.incrementAndGet();
            System.out.println("Duplicate Kafka event ignored: " + eventId);
            return;
        }

        if (operation.equalsIgnoreCase("UPSERT")) {
            ProductEntity entity = databaseService.findEntityById(documentId);

            client.index(request ->
                request
                    .index(index)
                    .id(documentId.toString())
                    .document(entity.toProduct())
            );
        }
        else if (operation.equalsIgnoreCase("DELETE")) {
            client.delete(request ->
                request
                    .index(index)
                    .id(documentId.toString())
            );
        }
        else {
            throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        client.indices().refresh(request -> request.index(index));

        try {
            cacheService.clearSearchCache();
        }
        catch (Exception exception) {
            System.err.println(
                "Redis cache invalidation failed: " + exception.getMessage()
            );
        }

        if (eventId != null) {
            processedEventRepository.save(
                new ProcessedEvent(eventId, Instant.now())
            );
        }

        lastEvent.set(payload);
        processedEvents.incrementAndGet();
        System.out.println("Kafka event processed: " + payload);
    }

    public long processedEvents() { return processedEvents.get(); }
    public long duplicatesIgnored() { return duplicatesIgnored.get(); }
    public String lastEvent() { return lastEvent.get(); }
}
