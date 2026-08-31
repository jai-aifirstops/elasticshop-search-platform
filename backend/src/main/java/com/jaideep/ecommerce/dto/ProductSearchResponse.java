package com.jaideep.ecommerce.dto;

import java.util.List;

public record ProductSearchResponse(

        long total,

        int page,

        int size,

        List<ProductSearchHit> results
) {
}
