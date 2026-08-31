package com.jaideep.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.jaideep.ecommerce.dto.ProductSearchHit;
import com.jaideep.ecommerce.dto.ProductSearchResponse;
import com.jaideep.ecommerce.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ElasticsearchClient client;

    @Value("${elasticsearch.index}")
    private String index;

    public ProductService(ElasticsearchClient client) {
        this.client = client;
    }

    public ProductSearchResponse search(
            String searchText,
            int page,
            int size
    ) throws IOException {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int from = safePage * safeSize;

        SearchResponse<Product> response = client.search(search -> {

            search
                    .index(index)
                    .from(from)
                    .size(safeSize);

            if (searchText == null || searchText.isBlank()) {

                search.query(query ->
                        query.matchAll(matchAll -> matchAll)
                );

            } else {

                search.query(query ->
                        query.multiMatch(multiMatch ->
                                multiMatch
                                        .query(searchText)
                                        .fields(
                                                "name^3",
                                                "description^2",
                                                "brand^2",
                                                "category"
                                        )
                        )
                );
            }

            return search;

        }, Product.class);

        List<ProductSearchHit> products =
                response
                        .hits()
                        .hits()
                        .stream()
                        .filter(hit -> hit.source() != null)
                        .map(this::convertHit)
                        .toList();

        long total =
                response.hits().total() == null
                        ? products.size()
                        : response.hits().total().value();

        return new ProductSearchResponse(
                total,
                safePage,
                safeSize,
                products
        );
    }

    private ProductSearchHit convertHit(Hit<Product> hit) {

        return new ProductSearchHit(
                hit.id(),
                hit.score(),
                hit.source()
        );
    }

    public Optional<Product> getById(String id)
            throws IOException {

        GetResponse<Product> response =
                client.get(
                        request ->
                                request
                                        .index(index)
                                        .id(id),
                        Product.class
                );

        if (!response.found()) {
            return Optional.empty();
        }

        return Optional.ofNullable(response.source());
    }

    public Product save(
            String id,
            Product product
    ) throws IOException {

        client.index(request ->
                request
                        .index(index)
                        .id(id)
                        .document(product)
        );

        client.indices().refresh(
                request -> request.index(index)
        );

        return product;
    }

    public boolean delete(String id)
            throws IOException {

        GetResponse<Product> existing =
                client.get(
                        request ->
                                request
                                        .index(index)
                                        .id(id),
                        Product.class
                );

        if (!existing.found()) {
            return false;
        }

        client.delete(request ->
                request
                        .index(index)
                        .id(id)
        );

        client.indices().refresh(
                request -> request.index(index)
        );

        return true;
    }
}
