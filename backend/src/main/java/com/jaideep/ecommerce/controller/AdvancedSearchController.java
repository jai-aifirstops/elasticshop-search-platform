package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.service.AdvancedSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class AdvancedSearchController {

    private final AdvancedSearchService searchService;

    public AdvancedSearchController(
            AdvancedSearchService searchService
    ) {
        this.searchService = searchService;
    }

    @GetMapping("/advanced")
    public Map<String, Object> advancedSearch(

            @RequestParam(required = false)
            String q,

            @RequestParam(required = false)
            String brand,

            @RequestParam(required = false)
            String category,

            @RequestParam(required = false)
            Double minPrice,

            @RequestParam(required = false)
            Double maxPrice,

            @RequestParam(required = false)
            Double minRating,

            @RequestParam(required = false)
            Boolean inStock,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "relevance")
            String sort
    ) {

        return searchService.advancedSearch(
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
        );
    }

    @GetMapping("/fuzzy")
    public Map<String, Object> fuzzy(

            @RequestParam
            String q,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return searchService.fuzzySearch(
                q,
                size
        );
    }

    @GetMapping("/autocomplete")
    public Map<String, Object> autocomplete(

            @RequestParam
            String q,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return searchService.autocomplete(
                q,
                size
        );
    }

    @GetMapping("/facets")
    public Map<String, Object> facets() {

        return searchService.facets();
    }
}