package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.service.SearchCacheService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*")
public class CacheController {

    private final SearchCacheService cacheService;

    public CacheController(
            SearchCacheService cacheService
    ) {
        this.cacheService =
                cacheService;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {

        return cacheService.stats();
    }

    @DeleteMapping
    public Map<String, Object> clear() {

        cacheService.clearAndReset();

        return cacheService.stats();
    }
}