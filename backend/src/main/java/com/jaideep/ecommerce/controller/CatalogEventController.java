package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.kafka.ProductEventConsumer;
import com.jaideep.ecommerce.service.ProductCommandResult;
import com.jaideep.ecommerce.service.ProductCommandService;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog/events")
@CrossOrigin(origins = "*")
public class CatalogEventController {
    private final ProductCommandService commandService;
    private final ProductEventConsumer consumer;

    public CatalogEventController(
        ProductCommandService commandService,
        ProductEventConsumer consumer
    ) {
        this.commandService = commandService;
        this.consumer = consumer;
    }

    @PutMapping("/{id}/price")
    public Map<String, Object> updatePrice(
        @PathVariable Long id,
        @RequestParam Double price
    ) {
        ProductCommandResult command = commandService.updatePrice(id, price);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "OUTBOX_STORED");
        response.put("eventId", command.eventId());
        response.put("product", command.product());
        return response;
    }

    @PutMapping("/{id}/stock")
    public Map<String, Object> updateStock(
        @PathVariable Long id,
        @RequestParam Boolean inStock
    ) {
        ProductCommandResult command = commandService.updateStock(id, inStock);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "OUTBOX_STORED");
        response.put("eventId", command.eventId());
        response.put("product", command.product());
        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("topic", "product-events");
        response.put("consumerGroup", "product-search-indexer");
        response.put("processedEvents", consumer.processedEvents());
        response.put("duplicatesIgnored", consumer.duplicatesIgnored());
        response.put("lastEvent", consumer.lastEvent());
        return response;
    }
}
