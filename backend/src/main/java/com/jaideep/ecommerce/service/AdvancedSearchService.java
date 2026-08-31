package com.jaideep.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class AdvancedSearchService {
    private final RestClient restClient;
    private final String index;
    private final SearchCacheService cacheService;

    public AdvancedSearchService(
        @Value("${elasticsearch.url}") String elasticsearchUrl,
        @Value("${elasticsearch.index}") String index,
        SearchCacheService cacheService
    ) {
        this.restClient = RestClient.builder().baseUrl(elasticsearchUrl).build();
        this.index = index;
        this.cacheService = cacheService;
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

        String cacheKey = cacheService.advancedSearchKey(
            q, brand, category, minPrice, maxPrice, minRating,
            inStock, safePage, safeSize, sort
        );

        Optional<Map<String, Object>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        List<Object> must = new ArrayList<>();
        List<Object> should = new ArrayList<>();
        List<Object> filters = new ArrayList<>();

        boolean hasTextQuery = q != null && !q.isBlank();

        if (hasTextQuery) {
            Map<String, Object> multiMatch = new LinkedHashMap<>();
            multiMatch.put("query", q);
            multiMatch.put(
                "fields",
                List.of("name^4", "description^1.5", "brand^2.5", "category^1.5")
            );
            must.add(Map.of("multi_match", multiMatch));

            should.add(
                Map.of(
                    "term",
                    Map.of(
                        "name.sort",
                        Map.of(
                            "value", q.toLowerCase(Locale.ROOT),
                            "boost", 12.0
                        )
                    )
                )
            );

            should.add(
                Map.of(
                    "match_phrase",
                    Map.of(
                        "name",
                        Map.of("query", q, "boost", 6.0)
                    )
                )
            );

            should.add(
                Map.of(
                    "match_phrase",
                    Map.of(
                        "description",
                        Map.of("query", q, "boost", 1.5)
                    )
                )
            );
        }

        if (brand != null && !brand.isBlank()) {
            filters.add(
                Map.of("term", Map.of("brand.keyword", brand))
            );
        }

        if (category != null && !category.isBlank()) {
            filters.add(
                Map.of("term", Map.of("category.keyword", category))
            );
        }

        if (minPrice != null || maxPrice != null) {
            Map<String, Object> range = new LinkedHashMap<>();
            if (minPrice != null) range.put("gte", minPrice);
            if (maxPrice != null) range.put("lte", maxPrice);
            filters.add(Map.of("range", Map.of("price", range)));
        }

        if (minRating != null) {
            filters.add(
                Map.of(
                    "range",
                    Map.of("rating", Map.of("gte", minRating))
                )
            );
        }

        if (inStock != null) {
            filters.add(
                Map.of("term", Map.of("inStock", inStock))
            );
        }

        Map<String, Object> bool = new LinkedHashMap<>();
        if (!must.isEmpty()) bool.put("must", must);
        if (!should.isEmpty()) bool.put("should", should);
        if (!filters.isEmpty()) bool.put("filter", filters);

        Map<String, Object> baseQuery =
            bool.isEmpty()
                ? Map.of("match_all", Map.of())
                : Map.of("bool", bool);

        Map<String, Object> finalQuery;

        if (hasTextQuery) {
            Map<String, Object> fieldValueFactor = new LinkedHashMap<>();
            fieldValueFactor.put("field", "rating");
            fieldValueFactor.put("factor", 0.25);
            fieldValueFactor.put("modifier", "sqrt");
            fieldValueFactor.put("missing", 0);

            Map<String, Object> functionScore = new LinkedHashMap<>();
            functionScore.put("query", baseQuery);
            functionScore.put(
                "functions",
                List.of(Map.of("field_value_factor", fieldValueFactor))
            );
            functionScore.put("score_mode", "sum");
            functionScore.put("boost_mode", "sum");

            finalQuery = Map.of("function_score", functionScore);
        }
        else {
            finalQuery = baseQuery;
        }

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("from", safePage * safeSize);
        request.put("size", safeSize);
        request.put("query", finalQuery);
        request.put(
            "highlight",
            Map.of(
                "fields",
                Map.of("name", Map.of(), "description", Map.of())
            )
        );

        List<Object> sorting = buildSort(sort);
        if (!sorting.isEmpty()) {
            request.put("sort", sorting);
        }

        Map<String, Object> response =
            restClient
                .post()
                .uri("/" + index + "/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        Map<String, Object> result = response == null ? Map.of() : response;
        cacheService.putSearch(cacheKey, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fuzzySearch(String q, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);

        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", q);
        multiMatch.put("fields", List.of("name^4", "description", "brand^2"));
        multiMatch.put("fuzziness", "AUTO");

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("size", safeSize);
        request.put("query", Map.of("multi_match", multiMatch));
        request.put(
            "highlight",
            Map.of("fields", Map.of("name", Map.of()))
        );

        Map<String, Object> response =
            restClient
                .post()
                .uri("/" + index + "/_search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        return response == null ? Map.of() : response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> autocomplete(String q, int size) {
        int safeSize = Math.min(Math.max(size, 1), 20);

        Map<String, Object> multiMatch = new LinkedHashMap<>();
        multiMatch.put("query", q);
        multiMatch.put("type", "bool_prefix");
        multiMatch.put(
            "fields",
            List.of("nameSearch", "nameSearch._2gram", "nameSearch._3gram")
        );

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("size", safeSize);
        request.put("query", Map.of("multi_match", multiMatch));
        request.put(
            "_source",
            List.of(
                "productId", "name", "brand", "category",
                "price", "rating", "inStock"
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

        return response == null ? Map.of() : response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> facets() {
        String cacheKey = cacheService.facetsKey();

        Optional<Map<String, Object>> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        Map<String, Object> aggregations = new LinkedHashMap<>();
        aggregations.put(
            "brands",
            Map.of("terms", Map.of("field", "brand.keyword", "size", 50))
        );
        aggregations.put(
            "categories",
            Map.of("terms", Map.of("field", "category.keyword", "size", 50))
        );
        aggregations.put(
            "price_stats",
            Map.of("stats", Map.of("field", "price"))
        );
        aggregations.put(
            "average_rating",
            Map.of("avg", Map.of("field", "rating"))
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

        Map<String, Object> result = response == null ? Map.of() : response;
        cacheService.putFacets(cacheKey, result);
        return result;
    }

    private List<Object> buildSort(String sort) {
        if (
            sort == null ||
            sort.isBlank() ||
            sort.equalsIgnoreCase("relevance")
        ) {
            return List.of();
        }

        return switch (sort.toLowerCase()) {
            case "price_asc" ->
                List.of(Map.of("price", Map.of("order", "asc")));
            case "price_desc" ->
                List.of(Map.of("price", Map.of("order", "desc")));
            case "rating_desc" ->
                List.of(Map.of("rating", Map.of("order", "desc")));
            case "name_asc" ->
                List.of(Map.of("name.sort", Map.of("order", "asc")));
            default ->
                List.of();
        };
    }
}
