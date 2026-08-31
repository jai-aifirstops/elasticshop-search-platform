package com.jaideep.ecommerce.controller;

import com.jaideep.ecommerce.dto.ProductSearchResponse;
import com.jaideep.ecommerce.model.Product;
import com.jaideep.ecommerce.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    public ProductController(
            ProductService productService
    ) {
        this.productService = productService;
    }

    @GetMapping
    public ProductSearchResponse getProducts(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) throws IOException {

        return productService.search(
                null,
                page,
                size
        );
    }

    @GetMapping("/search")
    public ProductSearchResponse searchProducts(

            @RequestParam
            String q,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size

    ) throws IOException {

        return productService.search(
                q,
                page,
                size
        );
    }

    @GetMapping("/{id}")
    public Product getProduct(
            @PathVariable String id
    ) throws IOException {

        return productService
                .getById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found"
                        )
                );
    }

    @PutMapping("/{id}")
    public Product saveProduct(

            @PathVariable
            String id,

            @RequestBody
            Product product

    ) throws IOException {

        return productService.save(
                id,
                product
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @PathVariable String id
    ) throws IOException {

        boolean deleted =
                productService.delete(id);

        if (!deleted) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found"
            );
        }
    }
}
