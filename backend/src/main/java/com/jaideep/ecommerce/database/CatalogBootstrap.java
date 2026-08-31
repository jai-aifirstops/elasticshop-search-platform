package com.jaideep.ecommerce.database;

import com.jaideep.ecommerce.service.CatalogSyncService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogBootstrap
        implements ApplicationRunner {

    private final CatalogDatabaseService databaseService;
    private final CatalogSyncService syncService;

    public CatalogBootstrap(
            CatalogDatabaseService databaseService,
            CatalogSyncService syncService
    ) {
        this.databaseService =
                databaseService;

        this.syncService =
                syncService;
    }

    @Override
    public void run(
            ApplicationArguments args
    ) throws Exception {

        int seeded =
                databaseService.seedIfEmpty();

        int synced =
                syncService.syncAll(
                        databaseService.findAll()
                );

        System.out.println(
                "PostgreSQL products seeded: "
                        + seeded
        );

        System.out.println(
                "Products synchronized to Elasticsearch: "
                        + synced
        );
    }
}