package com.jaideep.ecommerce.service;

import com.jaideep.ecommerce.model.Product;

public record ProductCommandResult(Product product, String eventId) {
}
