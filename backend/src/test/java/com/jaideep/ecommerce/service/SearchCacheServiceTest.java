package com.jaideep.ecommerce.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchCacheServiceTest {

    private final SearchCacheService cacheService =
            new SearchCacheService(
                    null,
                    null
            );

    @Test
    void sameSearchProducesSameCacheKey() {

        String first =
                cacheService
                        .advancedSearchKey(
                                "macbook",
                                "Apple",
                                "Laptops",
                                null,
                                2000.0,
                                4.5,
                                true,
                                0,
                                10,
                                "relevance"
                        );

        String second =
                cacheService
                        .advancedSearchKey(
                                "macbook",
                                "Apple",
                                "Laptops",
                                null,
                                2000.0,
                                4.5,
                                true,
                                0,
                                10,
                                "relevance"
                        );

        assertEquals(
                first,
                second
        );
    }

    @Test
    void differentPageProducesDifferentCacheKey() {

        String first =
                cacheService
                        .advancedSearchKey(
                                "laptop",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                0,
                                10,
                                "relevance"
                        );

        String second =
                cacheService
                        .advancedSearchKey(
                                "laptop",
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                1,
                                10,
                                "relevance"
                        );

        assertNotEquals(
                first,
                second
        );

        assertTrue(
                first.startsWith(
                        "search:advanced:"
                )
        );
    }
}