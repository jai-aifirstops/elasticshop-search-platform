package com.jaideep.ecommerce.service;

import com.jaideep.ecommerce.database.ProductEntity;
import com.jaideep.ecommerce.database.ProductRepository;
import com.jaideep.ecommerce.outbox.OutboxService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class ProductCommandService {
    private final ProductRepository repository;
    private final OutboxService outboxService;

    public ProductCommandService(
        ProductRepository repository,
        OutboxService outboxService
    ) {
        this.repository = repository;
        this.outboxService = outboxService;
    }

    @Transactional
    public ProductCommandResult updatePrice(Long id, Double price) {
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Price must be zero or greater");
        }

        ProductEntity entity = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));

        entity.updatePrice(price);
        repository.save(entity);

        String eventId = outboxService.enqueueProductUpsert(id);
        return new ProductCommandResult(entity.toProduct(), eventId);
    }

    @Transactional
    public ProductCommandResult updateStock(Long id, Boolean inStock) {
        if (inStock == null) {
            throw new IllegalArgumentException("inStock must be provided");
        }

        ProductEntity entity = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));

        entity.updateStock(inStock);
        repository.save(entity);

        String eventId = outboxService.enqueueProductUpsert(id);
        return new ProductCommandResult(entity.toProduct(), eventId);
    }
}
