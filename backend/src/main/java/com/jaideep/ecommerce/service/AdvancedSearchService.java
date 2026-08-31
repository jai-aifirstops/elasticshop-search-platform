package com.jaideep.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdvancedSearchService {

    private final RestClient restClient;
    private final String index;

    public AdvancedSearchService(
            @Value("${elasticsearch.url}") String elasticsearchUrl,
            @Value("${elasticsearch.index}") String index
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(elasticsearchUrl)
                .build();

        this.index = index;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> advancedSearch(
            String q,
            String brand,
            String category,
            Double minPrice,
            Double maxPrice,
            Double minRating,
            Boolean inStock,
            int page,
            int size,
            String sort
    ) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        List<Object> must = new ArrayList<>();
        List<Object> filters = new ArrayList<>();

        if (q != null && !q.isBlank()) {

            Map<String, Object> multiMatch = new LinkedHashMap<>();

            multiMatch.put("query", q);

            multiMatch.put(
                    "fields",
                    List.of(
                            "name^3",
                            "description^2",
                            "brand^2",
                            "category"
                    )
            );

            must.add(
                    Map.of(
                            "multi_match",
                            multiMatch
                    )
            );
        }

        if (brand != null && !brand.isBlank()) {

            filters.add(
                    Map.of(
                            "term",
                            Map.of(
                                    "brand.keyword",
                                    brand
                            )
                    )
            );
        }

        if (category != null && !category.isBlank()) {

            filters.add(
                    Map.of(
                            "term",
                            Map.of(
                                    "category.keyword",
                                    category
                            )
                    )
            );
        }

        if (minPrice != null || maxPrice != null) {

            Map<String, Object> priceRange = new LinkedHashMap<>();

            if (minPrice != null) {
                priceRange.put("gte", minPrice);
            }

            if (maxPrice != null) {
                priceRange.put("lte", maxPrice);
            }

            filters.add(
                    Map.of(
                            "range",
                            Map.of(
                                    "price",
                                    priceRange
                            )
                    )
            );
        }

        if (minRating != null) {

            filters.add(
                    Map.of(
                            "range",
                            Map.of(
                                    "rating",
                                    Map.of(
                                            "gte",
                                            minRating
                                    )
                            )
                    )
            );
        }

        if (inStock != null) {

            filters.add(
                    Map.of(
                            "term",
                            Map.of(
                                    "inStock",
                                    inStock
                            )
                    )
            );
        }

        Map<String, Object> bool = new LinkedHashMap<>();

        if (!must.isEmpty()) {
            bool.put("must", must);
        }

        if (!filters.isEmpty()) {
            bool.put("filter", filters);
        }

        Map<String, Object> query = new LinkedHashMap<>();

        if (bool.isEmpty()) {

            query.put(
                    "match_all",
                    Map.of()
            );

        } else {

            query.put(
                    "bool",
                    bool
            );
        }

        Map<String, Object> request = new LinkedHashMap<>();

        request.put(
                "from",
                safePage * safeSize
        );

        request.put(
                "size",
                safeSize
        );

        request.put(
                "query",
                query
        );

        request.put(
                "highlight",
                Map.of(
                        "fields",
                        Map.of(
                                "name",
                                Map.of(),
                                "description",
                                Map.of()
                        )
                )
        );

        List<Object> sorts = buildSort(sort);

        if (!sorts.isEmpty()) {
            request.put("sort", sorts);
        }

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/" + index + "/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class);

        return response == null
                ? Map.of()
                : response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fuzzySearch(
            String q,
            int size
    ) {

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        50
                );

        Map<String, Object> multiMatch = new LinkedHashMap<>();

        multiMatch.put("query", q);

        multiMatch.put(
                "fields",
                List.of(
                        "name^3",
                        "description",
                        "brand"
                )
        );

        multiMatch.put(
                "fuzziness",
                "AUTO"
        );

        Map<String, Object> request = new LinkedHashMap<>();

        request.put("size", safeSize);

        request.put(
                "query",
                Map.of(
                        "multi_match",
                        multiMatch
                )
        );

        request.put(
                "highlight",
                Map.of(
                        "fields",
                        Map.of(
                                "name",
                                Map.of()
                        )
                )
        );

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/" + index + "/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class);

        return response == null
                ? Map.of()
                : response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> autocomplete(
            String q,
            int size
    ) {

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        20
                );

        Map<String, Object> request = new LinkedHashMap<>();

        request.put("size", safeSize);

        request.put(
                "query",
                Map.of(
                        "match_phrase_prefix",
                        Map.of(
                                "name",
                                Map.of(
                                        "query",
                                        q
                                )
                        )
                )
        );

        request.put(
                "_source",
                List.of(
                        "productId",
                        "name",
                        "brand",
                        "category",
                        "price",
                        "rating",
                        "inStock"
                )
        );

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/" + index + "/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class);

        return response == null
                ? Map.of()
                : response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> facets() {

        Map<String, Object> aggregations =
                new LinkedHashMap<>();

        aggregations.put(
                "brands",
                Map.of(
                        "terms",
                        Map.of(
                                "field",
                                "brand.keyword",
                                "size",
                                50
                        )
                )
        );

        aggregations.put(
                "categories",
                Map.of(
                        "terms",
                        Map.of(
                                "field",
                                "category.keyword",
                                "size",
                                50
                        )
                )
        );

        aggregations.put(
                "price_stats",
                Map.of(
                        "stats",
                        Map.of(
                                "field",
                                "price"
                        )
                )
        );

        aggregations.put(
                "average_rating",
                Map.of(
                        "avg",
                        Map.of(
                                "field",
                                "rating"
                        )
                )
        );

        Map<String, Object> request = new LinkedHashMap<>();

        request.put("size", 0);
        request.put("aggs", aggregations);

        Map<String, Object> response =
                restClient
                        .post()
                        .uri("/" + index + "/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .body(Map.class);

        return response == null
                ? Map.of()
                : response;
    }

    private List<Object> buildSort(
            String sort
    ) {

        if (
                sort == null ||
                sort.isBlank() ||
                sort.equalsIgnoreCase("relevance")
        ) {
            return List.of();
        }

        return switch (sort.toLowerCase()) {

            case "price_asc" ->
                    List.of(
                            Map.of(
                                    "price",
                                    Map.of(
                                            "order",
                                            "asc"
                                    )
                            )
                    );

            case "price_desc" ->
                    List.of(
                            Map.of(
                                    "price",
                                    Map.of(
                                            "order",
                                            "desc"
                                    )
                            )
                    );

            case "rating_desc" ->
                    List.of(
                            Map.of(
                                    "rating",
                                    Map.of(
                                            "order",
                                            "desc"
                                    )
                            )
                    );

            case "name_asc" ->
                    List.of(
                            Map.of(
                                    "name.sort",
                                    Map.of(
                                            "order",
                                            "asc"
                                    )
                            )
                    );

            default -> List.of();
        };
    }
}