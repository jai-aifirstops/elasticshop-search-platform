package com.jaideep.ecommerce.kafka;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.jaideep.ecommerce.database.CatalogDatabaseService;
import com.jaideep.ecommerce.database.ProductEntity;
import com.jaideep.ecommerce.service.SearchCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ProductEventConsumer {

    private final ElasticsearchClient client;

    private final CatalogDatabaseService databaseService;

    private final SearchCacheService cacheService;

    private final AtomicLong processedEvents =
            new AtomicLong();

    private final AtomicReference<String> lastEvent =
            new AtomicReference<>(
                    "NONE"
            );

    @Value("${elasticsearch.index}")
    private String index;

    public ProductEventConsumer(
            ElasticsearchClient client,
            CatalogDatabaseService databaseService,
            SearchCacheService cacheService
    ) {

        this.client =
                client;

        this.databaseService =
                databaseService;

        this.cacheService =
                cacheService;
    }

    @KafkaListener(
            topics = "${app.kafka.product-topic}"
    )
    public void consume(
            String payload
    ) throws IOException {

        String[] parts =
                payload.split(
                        "\\|",
                        2
                );

        if (parts.length != 2) {

            throw new IllegalArgumentException(
                    "Invalid product event: "
                            + payload
            );
        }

        String operation =
                parts[0];

        Long documentId =
                Long.valueOf(
                        parts[1]
                );

        if (
                operation.equalsIgnoreCase(
                        "UPSERT"
                )
        ) {

            ProductEntity entity =
                    databaseService
                            .findEntityById(
                                    documentId
                            );

            client.index(
                    request ->
                            request
                                    .index(index)
                                    .id(
                                            documentId
                                                    .toString()
                                    )
                                    .document(
                                            entity.toProduct()
                                    )
            );

        }
        else if (
                operation.equalsIgnoreCase(
                        "DELETE"
                )
        ) {

            client.delete(
                    request ->
                            request
                                    .index(index)
                                    .id(
                                            documentId
                                                    .toString()
                                    )
            );

        }
        else {

            throw new IllegalArgumentException(
                    "Unsupported operation: "
                            + operation
            );
        }

        client.indices().refresh(
                request ->
                        request.index(index)
        );

        /*
         * Product data changed.
         * Cached search/facet results may now be stale.
         */
        try {

            cacheService
                    .clearSearchCache();

        }
        catch (Exception exception) {

            System.err.println(
                    "Redis cache invalidation failed: "
                            + exception.getMessage()
            );
        }

        lastEvent.set(payload);

        processedEvents.incrementAndGet();

        System.out.println(
                "Kafka event processed: "
                        + payload
        );
    }

    public long processedEvents() {

        return processedEvents.get();
    }

    public String lastEvent() {

        return lastEvent.get();
    }
}