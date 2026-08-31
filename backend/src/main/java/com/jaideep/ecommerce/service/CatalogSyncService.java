package com.jaideep.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.jaideep.ecommerce.database.ProductEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CatalogSyncService {

    private final ElasticsearchClient client;

    @Value("${elasticsearch.index}")
    private String index;

    public CatalogSyncService(
            ElasticsearchClient client
    ) {
        this.client = client;
    }

    public int syncAll(
            List<ProductEntity> products
    ) throws IOException {

        for (ProductEntity entity : products) {

            client.index(
                    request ->
                            request
                                    .index(index)
                                    .id(
                                            entity
                                                    .getId()
                                                    .toString()
                                    )
                                    .document(
                                            entity.toProduct()
                                    )
            );
        }

        client.indices().refresh(
                request ->
                        request.index(index)
        );

        return products.size();
    }
}