package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.database.CatalogDatabaseService;
import com.jaideep.ecommerce.model.Product;
import com.jaideep.ecommerce.service.CatalogSyncService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
@CrossOrigin(origins = "*")
public class DatabaseController {

    private final CatalogDatabaseService databaseService;
    private final CatalogSyncService syncService;

    public DatabaseController(
            CatalogDatabaseService databaseService,
            CatalogSyncService syncService
    ) {
        this.databaseService =
                databaseService;

        this.syncService =
                syncService;
    }

    @GetMapping("/count")
    public Map<String, Long> count() {

        return Map.of(
                "postgresProducts",
                databaseService.count()
        );
    }

    @GetMapping("/products")
    public List<Product> products() {

        return databaseService
                .findAll()
                .stream()
                .map(
                        entity ->
                                entity.toProduct()
                )
                .toList();
    }

    @PostMapping("/sync")
    public Map<String, Object> sync()
            throws IOException {

        int synchronizedProducts =
                syncService.syncAll(
                        databaseService.findAll()
                );

        return Map.of(
                "status",
                "SYNCED",
                "products",
                synchronizedProducts
        );
    }
}