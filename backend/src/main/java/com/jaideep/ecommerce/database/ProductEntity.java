package com.jaideep.ecommerce.database;

import com.jaideep.ecommerce.model.Product;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(
                        name = "idx_products_brand",
                        columnList = "brand"
                ),
                @Index(
                        name = "idx_products_category",
                        columnList = "category"
                ),
                @Index(
                        name = "idx_products_price",
                        columnList = "price"
                )
        }
)
public class ProductEntity {

    @Id
    private Long id;

    @Column(
            name = "product_id",
            nullable = false,
            unique = true,
            length = 50
    )
    private String productId;

    @Column(
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            nullable = false,
            length = 100
    )
    private String brand;

    @Column(
            nullable = false,
            length = 100
    )
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Float rating;

    @Column(
            name = "in_stock",
            nullable = false
    )
    private Boolean inStock;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDate createdAt;

    protected ProductEntity() {
    }

    public ProductEntity(
            Long id,
            String productId,
            String name,
            String description,
            String brand,
            String category,
            Double price,
            Float rating,
            Boolean inStock,
            String tags,
            LocalDate createdAt
    ) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.inStock = inStock;
        this.tags = tags;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public Double getPrice() {
        return price;
    }

    public Float getRating() {
        return rating;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public String getTags() {
        return tags;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public Product toProduct() {

        List<String> tagList;

        if (tags == null || tags.isBlank()) {

            tagList = List.of();

        } else {

            tagList =
                    Arrays.stream(
                                    tags.split(",")
                            )
                            .map(String::trim)
                            .filter(
                                    value ->
                                            !value.isBlank()
                            )
                            .toList();
        }

        return new Product(
                productId,
                name,
                description,
                brand,
                category,
                price,
                rating,
                inStock,
                tagList,
                createdAt.toString()
        );
    }
}