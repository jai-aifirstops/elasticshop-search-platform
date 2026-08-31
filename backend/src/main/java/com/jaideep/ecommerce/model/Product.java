package com.jaideep.ecommerce.model;

import java.util.List;

public record Product(

        String productId,

        String name,

        String description,

        String brand,

        String category,

        Double price,

        Float rating,

        Boolean inStock,

        List<String> tags,

        String createdAt
) {
}
