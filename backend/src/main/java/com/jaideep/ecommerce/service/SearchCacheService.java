package com.jaideep.ecommerce.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SearchCacheService {

    private static final Duration SEARCH_TTL =
            Duration.ofSeconds(60);

    private static final Duration FACET_TTL =
            Duration.ofMinutes(5);

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    private final AtomicLong hits =
            new AtomicLong();

    private final AtomicLong misses =
            new AtomicLong();

    public SearchCacheService(
            StringRedisTemplate redis,
            JsonMapper jsonMapper
    ) {
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    @SuppressWarnings("unchecked")
    public Optional<Map<String, Object>> get(
            String key
    ) {

        String json =
                redis
                        .opsForValue()
                        .get(key);

        if (json == null) {

            misses.incrementAndGet();

            return Optional.empty();
        }

        try {

            Map<String, Object> value =
                    jsonMapper.readValue(
                            json,
                            Map.class
                    );

            hits.incrementAndGet();

            return Optional.of(value);

        }
        catch (Exception exception) {

            redis.delete(key);

            misses.incrementAndGet();

            return Optional.empty();
        }
    }

    public void putSearch(
            String key,
            Map<String, Object> value
    ) {

        put(
                key,
                value,
                SEARCH_TTL
        );
    }

    public void putFacets(
            String key,
            Map<String, Object> value
    ) {

        put(
                key,
                value,
                FACET_TTL
        );
    }

    private void put(
            String key,
            Map<String, Object> value,
            Duration ttl
    ) {

        try {

            String json =
                    jsonMapper
                            .writeValueAsString(
                                    value
                            );

            redis
                    .opsForValue()
                    .set(
                            key,
                            json,
                            ttl
                    );

        }
        catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to write Redis cache",
                    exception
            );
        }
    }

    public String advancedSearchKey(
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

        String raw =
                String.valueOf(q)
                        + "|"
                        + String.valueOf(brand)
                        + "|"
                        + String.valueOf(category)
                        + "|"
                        + String.valueOf(minPrice)
                        + "|"
                        + String.valueOf(maxPrice)
                        + "|"
                        + String.valueOf(minRating)
                        + "|"
                        + String.valueOf(inStock)
                        + "|"
                        + page
                        + "|"
                        + size
                        + "|"
                        + String.valueOf(sort);

        return "search:advanced:"
                + sha256(raw);
    }

    public String facetsKey() {
        return "search:facets:v1";
    }

    private String sha256(
            String value
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] bytes =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(bytes);

        }
        catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to create cache key",
                    exception
            );
        }
    }

    public void clearSearchCache() {

        Set<String> keys =
                redis.keys(
                        "search:*"
                );

        if (
                keys != null &&
                !keys.isEmpty()
        ) {

            redis.delete(keys);
        }
    }

    public void clearAndReset() {

        clearSearchCache();

        hits.set(0);
        misses.set(0);
    }

    public long keyCount() {

        Set<String> keys =
                redis.keys(
                        "search:*"
                );

        if (keys == null) {
            return 0;
        }

        return keys.size();
    }

    public Map<String, Object> stats() {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "redis",
                "UP"
        );

        result.put(
                "cacheHits",
                hits.get()
        );

        result.put(
                "cacheMisses",
                misses.get()
        );

        result.put(
                "cachedKeys",
                keyCount()
        );

        result.put(
                "searchTtlSeconds",
                SEARCH_TTL.toSeconds()
        );

        result.put(
                "facetTtlSeconds",
                FACET_TTL.toSeconds()
        );

        return result;
    }
}