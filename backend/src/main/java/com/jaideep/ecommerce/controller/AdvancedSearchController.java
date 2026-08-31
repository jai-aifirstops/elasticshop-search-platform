package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.service.AdvancedSearchService;
import com.jaideep.ecommerce.service.SearchMetricsService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
@Validated
public class AdvancedSearchController {

    private final AdvancedSearchService searchService;
    private final SearchMetricsService metrics;

    public AdvancedSearchController(
            AdvancedSearchService searchService,
            SearchMetricsService metrics
    ) {

        this.searchService =
                searchService;

        this.metrics =
                metrics;
    }

    @GetMapping("/advanced")
    public Map<String, Object> advancedSearch(

            @RequestParam(required = false)
            @Size(max = 200)
            String q,

            @RequestParam(required = false)
            @Size(max = 100)
            String brand,

            @RequestParam(required = false)
            @Size(max = 100)
            String category,

            @RequestParam(required = false)
            @DecimalMin("0.0")
            Double minPrice,

            @RequestParam(required = false)
            @DecimalMin("0.0")
            Double maxPrice,

            @RequestParam(required = false)
            @DecimalMin("0.0")
            Double minRating,

            @RequestParam(required = false)
            Boolean inStock,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(100)
            int size,

            @RequestParam(defaultValue = "relevance")
            @Pattern(
                    regexp = "relevance|price_asc|price_desc|rating_desc|name_asc"
            )
            String sort
    ) {

        return metrics.advanced(
                () ->
                        searchService.advancedSearch(
                                q,
                                brand,
                                category,
                                minPrice,
                                maxPrice,
                                minRating,
                                inStock,
                                page,
                                size,
                                sort
                        )
        );
    }

    @GetMapping("/fuzzy")
    public Map<String, Object> fuzzy(

            @RequestParam
            @Size(
                    min = 1,
                    max = 200
            )
            String q,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(50)
            int size
    ) {

        return metrics.fuzzy(
                () ->
                        searchService.fuzzySearch(
                                q,
                                size
                        )
        );
    }

    @GetMapping("/autocomplete")
    public Map<String, Object> autocomplete(

            @RequestParam
            @Size(
                    min = 1,
                    max = 100
            )
            String q,

            @RequestParam(defaultValue = "10")
            @Min(1)
            @Max(20)
            int size
    ) {

        return metrics.autocomplete(
                () ->
                        searchService.autocomplete(
                                q,
                                size
                        )
        );
    }

    @GetMapping("/facets")
    public Map<String, Object> facets() {

        return metrics.facets(
                searchService::facets
        );
    }
}