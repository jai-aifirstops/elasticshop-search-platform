package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.outbox.OutboxService;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/outbox")
@CrossOrigin(origins = "*")
public class OutboxController {
    private final OutboxService outboxService;

    public OutboxController(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("total", outboxService.totalCount());
        response.put("pending", outboxService.pendingCount());
        response.put("published", outboxService.publishedCount());
        return response;
    }
}
