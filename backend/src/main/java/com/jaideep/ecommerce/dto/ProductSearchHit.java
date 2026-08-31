package com.jaideep.ecommerce.dto;

import com.jaideep.ecommerce.model.Product;

public record ProductSearchHit(

        String id,

        Double score,

        Product product
) {
}
