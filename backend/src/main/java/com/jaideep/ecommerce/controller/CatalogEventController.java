package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.database.CatalogDatabaseService;
import com.jaideep.ecommerce.kafka.ProductEventConsumer;
import com.jaideep.ecommerce.kafka.ProductEventProducer;
import com.jaideep.ecommerce.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/catalog/events")
@CrossOrigin(origins = "*")
public class CatalogEventController {

    private final CatalogDatabaseService databaseService;
    private final ProductEventProducer producer;
    private final ProductEventConsumer consumer;

    public CatalogEventController(
            CatalogDatabaseService databaseService,
            ProductEventProducer producer,
            ProductEventConsumer consumer
    ) {
        this.databaseService =
                databaseService;

        this.producer =
                producer;

        this.consumer =
                consumer;
    }

    @PutMapping("/{id}/price")
    public Map<String, Object> updatePrice(

            @PathVariable
            Long id,

            @RequestParam
            Double price
    ) {

        try {

            Product product =
                    databaseService
                            .updatePrice(
                                    id,
                                    price
                            );

            /*
             * updatePrice() returns only after the
             * PostgreSQL transaction has completed.
             * We publish the Kafka event afterwards.
             */

            producer.publishUpsert(id);

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "status",
                    "EVENT_PUBLISHED"
            );

            result.put(
                    "event",
                    "UPSERT|" + id
            );

            result.put(
                    "product",
                    product
            );

            return result;

        }
        catch (NoSuchElementException exception) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );
        }
    }

    @PutMapping("/{id}/stock")
    public Map<String, Object> updateStock(

            @PathVariable
            Long id,

            @RequestParam
            Boolean inStock
    ) {

        try {

            Product product =
                    databaseService
                            .updateStock(
                                    id,
                                    inStock
                            );

            producer.publishUpsert(id);

            Map<String, Object> result =
                    new LinkedHashMap<>();

            result.put(
                    "status",
                    "EVENT_PUBLISHED"
            );

            result.put(
                    "event",
                    "UPSERT|" + id
            );

            result.put(
                    "product",
                    product
            );

            return result;

        }
        catch (NoSuchElementException exception) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    exception.getMessage()
            );
        }
    }

    @GetMapping("/status")
    public Map<String, Object> status() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "topic",
                "product-events"
        );

        result.put(
                "consumerGroup",
                "product-search-indexer"
        );

        result.put(
                "processedEvents",
                consumer.processedEvents()
        );

        result.put(
                "lastEvent",
                consumer.lastEvent()
        );

        return result;
    }
}