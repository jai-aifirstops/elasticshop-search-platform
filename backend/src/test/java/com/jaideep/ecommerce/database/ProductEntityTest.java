package com.jaideep.ecommerce.database;

import com.jaideep.ecommerce.model.Product;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductEntityTest {

    @Test
    void convertsEntityToSearchProduct() {

        ProductEntity entity =
                new ProductEntity(
                        99L,
                        "TEST-99",
                        "Test Laptop",
                        "Test description",
                        "Test Brand",
                        "Laptops",
                        999.99,
                        4.8f,
                        true,
                        "test,laptop,java",
                        LocalDate.of(
                                2026,
                                8,
                                31
                        )
                );

        Product product =
                entity.toProduct();

        assertEquals(
                "TEST-99",
                product.productId()
        );

        assertEquals(
                "Test Laptop",
                product.name()
        );

        assertEquals(
                3,
                product.tags().size()
        );

        assertTrue(
                product.inStock()
        );
    }

    @Test
    void updateMethodsChangeEntityState() {

        ProductEntity entity =
                new ProductEntity(
                        100L,
                        "TEST-100",
                        "Test Product",
                        "Description",
                        "Brand",
                        "Accessories",
                        100.0,
                        4.0f,
                        true,
                        "test",
                        LocalDate.of(
                                2026,
                                8,
                                31
                        )
                );

        entity.updatePrice(
                125.50
        );

        entity.updateStock(
                false
        );

        assertEquals(
                125.50,
                entity.getPrice()
        );

        assertFalse(
                entity.getInStock()
        );
    }
}