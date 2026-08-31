package com.jaideep.ecommerce.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.url}")
    private String elasticsearchUrl;

    @Bean(destroyMethod = "close")
    public ElasticsearchClient elasticsearchClient() {

        return ElasticsearchClient.of(builder ->
                builder.host(elasticsearchUrl)
        );
    }
}
