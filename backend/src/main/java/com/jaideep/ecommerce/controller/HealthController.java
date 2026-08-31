package com.jaideep.ecommerce.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final ElasticsearchClient client;

    public HealthController(
            ElasticsearchClient client
    ) {
        this.client = client;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health()
            throws IOException {

        InfoResponse info = client.info();

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "backend",
                "UP"
        );

        result.put(
                "elasticsearch",
                "UP"
        );

        result.put(
                "cluster",
                info.clusterName()
        );

        result.put(
                "elasticsearchVersion",
                info.version().number()
        );

        return result;
    }
}
