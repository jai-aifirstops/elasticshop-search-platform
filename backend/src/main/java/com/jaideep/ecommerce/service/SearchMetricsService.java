package com.jaideep.ecommerce.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public class SearchMetricsService {

    private final Counter advancedRequests;
    private final Counter fuzzyRequests;
    private final Counter autocompleteRequests;
    private final Counter facetRequests;

    private final Timer advancedDuration;
    private final Timer fuzzyDuration;
    private final Timer autocompleteDuration;
    private final Timer facetDuration;

    public SearchMetricsService(
            MeterRegistry meterRegistry
    ) {

        advancedRequests =
                Counter.builder(
                                "elasticshop.search.requests"
                        )
                        .tag(
                                "type",
                                "advanced"
                        )
                        .register(
                                meterRegistry
                        );

        fuzzyRequests =
                Counter.builder(
                                "elasticshop.search.requests"
                        )
                        .tag(
                                "type",
                                "fuzzy"
                        )
                        .register(
                                meterRegistry
                        );

        autocompleteRequests =
                Counter.builder(
                                "elasticshop.search.requests"
                        )
                        .tag(
                                "type",
                                "autocomplete"
                        )
                        .register(
                                meterRegistry
                        );

        facetRequests =
                Counter.builder(
                                "elasticshop.search.requests"
                        )
                        .tag(
                                "type",
                                "facets"
                        )
                        .register(
                                meterRegistry
                        );

        advancedDuration =
                Timer.builder(
                                "elasticshop.search.duration"
                        )
                        .tag(
                                "type",
                                "advanced"
                        )
                        .register(
                                meterRegistry
                        );

        fuzzyDuration =
                Timer.builder(
                                "elasticshop.search.duration"
                        )
                        .tag(
                                "type",
                                "fuzzy"
                        )
                        .register(
                                meterRegistry
                        );

        autocompleteDuration =
                Timer.builder(
                                "elasticshop.search.duration"
                        )
                        .tag(
                                "type",
                                "autocomplete"
                        )
                        .register(
                                meterRegistry
                        );

        facetDuration =
                Timer.builder(
                                "elasticshop.search.duration"
                        )
                        .tag(
                                "type",
                                "facets"
                        )
                        .register(
                                meterRegistry
                        );
    }

    public <T> T advanced(
            Supplier<T> operation
    ) {

        advancedRequests.increment();

        return advancedDuration.record(
                operation
        );
    }

    public <T> T fuzzy(
            Supplier<T> operation
    ) {

        fuzzyRequests.increment();

        return fuzzyDuration.record(
                operation
        );
    }

    public <T> T autocomplete(
            Supplier<T> operation
    ) {

        autocompleteRequests.increment();

        return autocompleteDuration.record(
                operation
        );
    }

    public <T> T facets(
            Supplier<T> operation
    ) {

        facetRequests.increment();

        return facetDuration.record(
                operation
        );
    }
}