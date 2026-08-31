package com.jaideep.ecommerce.database;

import com.jaideep.ecommerce.model.Product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CatalogDatabaseService {

    private final ProductRepository repository;

    public CatalogDatabaseService(
            ProductRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public int seedIfEmpty() {

        if (repository.count() > 0) {
            return 0;
        }

        List<ProductEntity> products =
                seedProducts();

        repository.saveAll(products);

        return products.size();
    }

    public long count() {
        return repository.count();
    }

    public List<ProductEntity> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Product updatePrice(
            Long id,
            Double price
    ) {

        if (
                price == null ||
                price < 0
        ) {
            throw new IllegalArgumentException(
                    "Price must be zero or greater"
            );
        }

        ProductEntity entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new java.util.NoSuchElementException(
                                                "Product not found: " + id
                                        )
                        );

        entity.updatePrice(price);

        repository.save(entity);

        return entity.toProduct();
    }

    @Transactional
    public Product updateStock(
            Long id,
            Boolean inStock
    ) {

        if (inStock == null) {
            throw new IllegalArgumentException(
                    "inStock must be provided"
            );
        }

        ProductEntity entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new java.util.NoSuchElementException(
                                                "Product not found: " + id
                                        )
                        );

        entity.updateStock(inStock);

        repository.save(entity);

        return entity.toProduct();
    }

    public ProductEntity findEntityById(
            Long id
    ) {

        return repository
                .findById(id)
                .orElseThrow(
                        () ->
                                new java.util.NoSuchElementException(
                                        "Product not found: " + id
                                )
                );
    }
    private List<ProductEntity> seedProducts() {

        LocalDate date =
                LocalDate.of(
                        2026,
                        8,
                        31
                );

        return List.of(

                new ProductEntity(
                        1L,
                        "P1001",
                        "Apple MacBook Air M4",
                        "Lightweight Apple laptop powered by the M4 chip for productivity and everyday computing.",
                        "Apple",
                        "Laptops",
                        999.99,
                        4.8f,
                        true,
                        "apple,macbook,laptop",
                        date
                ),

                new ProductEntity(
                        2L,
                        "P1002",
                        "Apple MacBook Pro M4",
                        "High-performance Apple MacBook Pro designed for software development and professional workloads.",
                        "Apple",
                        "Laptops",
                        1599.99,
                        4.9f,
                        true,
                        "apple,macbook,pro,laptop",
                        date
                ),

                new ProductEntity(
                        3L,
                        "P1003",
                        "Dell XPS 13 Laptop",
                        "Compact premium Dell laptop designed for professional and everyday use.",
                        "Dell",
                        "Laptops",
                        1199.99,
                        4.6f,
                        true,
                        "dell,xps,laptop",
                        date
                ),

                new ProductEntity(
                        4L,
                        "P1004",
                        "Apple iPhone 17 Pro",
                        "Premium Apple smartphone with a professional camera system and high-performance processor.",
                        "Apple",
                        "Smartphones",
                        1099.99,
                        4.8f,
                        true,
                        "apple,iphone,smartphone",
                        date
                ),

                new ProductEntity(
                        5L,
                        "P1005",
                        "Samsung Galaxy Book5 Pro",
                        "Premium Samsung Galaxy laptop for productivity, entertainment and mobile professionals.",
                        "Samsung",
                        "Laptops",
                        1399.99,
                        4.5f,
                        true,
                        "samsung,galaxy,laptop",
                        date
                ),

                new ProductEntity(
                        6L,
                        "P1006",
                        "Logitech MX Master 3S",
                        "Wireless ergonomic productivity mouse designed for precise control and professional workflows.",
                        "Logitech",
                        "Accessories",
                        99.99,
                        4.7f,
                        true,
                        "logitech,mouse,wireless,accessory",
                        date
                ),

                new ProductEntity(
                        7L,
                        "P1007",
                        "Lenovo ThinkPad X1 Carbon",
                        "Lightweight business laptop designed for productivity, development and travel.",
                        "Lenovo",
                        "Laptops",
                        1499.99,
                        4.7f,
                        true,
                        "lenovo,thinkpad,business,laptop",
                        date
                ),

                new ProductEntity(
                        8L,
                        "P1008",
                        "HP Spectre x360 14",
                        "Premium convertible laptop with touch display and flexible two-in-one design.",
                        "HP",
                        "Laptops",
                        1299.99,
                        4.6f,
                        true,
                        "hp,spectre,convertible,laptop",
                        date
                ),

                new ProductEntity(
                        9L,
                        "P1009",
                        "ASUS Zenbook 14 OLED",
                        "Compact OLED laptop designed for productivity and high-quality visual work.",
                        "ASUS",
                        "Laptops",
                        1099.99,
                        4.5f,
                        true,
                        "asus,zenbook,oled,laptop",
                        date
                ),

                new ProductEntity(
                        10L,
                        "P1010",
                        "Microsoft Surface Laptop 7",
                        "Thin productivity laptop with premium build quality and long battery life.",
                        "Microsoft",
                        "Laptops",
                        1199.99,
                        4.6f,
                        true,
                        "microsoft,surface,laptop",
                        date
                ),

                new ProductEntity(
                        11L,
                        "P1011",
                        "Acer Swift Go 14",
                        "Portable everyday laptop with a compact design for school, work and travel.",
                        "Acer",
                        "Laptops",
                        899.99,
                        4.4f,
                        true,
                        "acer,swift,laptop",
                        date
                ),

                new ProductEntity(
                        12L,
                        "P1012",
                        "ASUS ROG Strix 16",
                        "High-performance gaming laptop with powerful graphics and fast display.",
                        "ASUS",
                        "Laptops",
                        1899.99,
                        4.7f,
                        true,
                        "asus,rog,gaming,laptop",
                        date
                ),

                new ProductEntity(
                        13L,
                        "P1013",
                        "Google Pixel 10 Pro",
                        "Premium Android smartphone focused on camera quality and intelligent software.",
                        "Google",
                        "Smartphones",
                        999.99,
                        4.7f,
                        true,
                        "google,pixel,android,smartphone",
                        date
                ),

                new ProductEntity(
                        14L,
                        "P1014",
                        "Samsung Galaxy S26 Ultra",
                        "Premium Samsung smartphone designed for photography, productivity and performance.",
                        "Samsung",
                        "Smartphones",
                        1299.99,
                        4.8f,
                        false,
                        "samsung,galaxy,android,smartphone",
                        date
                ),

                new ProductEntity(
                        15L,
                        "P1015",
                        "Google Pixel 10",
                        "Android smartphone with a clean software experience and advanced camera features.",
                        "Google",
                        "Smartphones",
                        799.99,
                        4.6f,
                        true,
                        "google,pixel,android,smartphone",
                        date
                ),

                new ProductEntity(
                        16L,
                        "P1016",
                        "Apple iPad Air M3",
                        "Portable Apple tablet built for entertainment, study, creativity and productivity.",
                        "Apple",
                        "Tablets",
                        699.99,
                        4.7f,
                        true,
                        "apple,ipad,tablet",
                        date
                ),

                new ProductEntity(
                        17L,
                        "P1017",
                        "Samsung Galaxy Tab S10 Plus",
                        "Large Android tablet designed for media, multitasking and productivity.",
                        "Samsung",
                        "Tablets",
                        999.99,
                        4.6f,
                        true,
                        "samsung,galaxy,tablet",
                        date
                ),

                new ProductEntity(
                        18L,
                        "P1018",
                        "Microsoft Surface Pro",
                        "Portable two-in-one Windows tablet designed for mobile professional workflows.",
                        "Microsoft",
                        "Tablets",
                        1099.99,
                        4.5f,
                        true,
                        "microsoft,surface,tablet",
                        date
                ),

                new ProductEntity(
                        19L,
                        "P1019",
                        "Dell UltraSharp 27 Monitor",
                        "Professional 27-inch monitor focused on productivity and accurate visual work.",
                        "Dell",
                        "Monitors",
                        599.99,
                        4.6f,
                        true,
                        "dell,ultrasharp,monitor",
                        date
                ),

                new ProductEntity(
                        20L,
                        "P1020",
                        "LG UltraGear 27 OLED",
                        "Fast OLED gaming monitor designed for smooth motion and high contrast.",
                        "LG",
                        "Monitors",
                        899.99,
                        4.7f,
                        true,
                        "lg,ultragear,oled,monitor",
                        date
                ),

                new ProductEntity(
                        21L,
                        "P1021",
                        "Samsung Odyssey G8 Monitor",
                        "Premium gaming monitor with a large high-refresh display.",
                        "Samsung",
                        "Monitors",
                        999.99,
                        4.5f,
                        false,
                        "samsung,odyssey,gaming,monitor",
                        date
                ),

                new ProductEntity(
                        22L,
                        "P1022",
                        "Sony WH-1000XM6",
                        "Wireless over-ear headphones with active noise cancellation.",
                        "Sony",
                        "Headphones",
                        449.99,
                        4.8f,
                        true,
                        "sony,headphones,wireless,noise-cancelling",
                        date
                ),

                new ProductEntity(
                        23L,
                        "P1023",
                        "Bose QuietComfort Ultra",
                        "Premium wireless headphones designed for comfort and noise cancellation.",
                        "Bose",
                        "Headphones",
                        429.99,
                        4.7f,
                        true,
                        "bose,headphones,wireless,noise-cancelling",
                        date
                ),

                new ProductEntity(
                        24L,
                        "P1024",
                        "Apple AirPods Pro",
                        "Compact wireless earbuds with active noise cancellation and Apple ecosystem integration.",
                        "Apple",
                        "Headphones",
                        249.99,
                        4.7f,
                        true,
                        "apple,airpods,earbuds,wireless",
                        date
                ),

                new ProductEntity(
                        25L,
                        "P1025",
                        "Logitech G Pro X Superlight 2",
                        "Lightweight wireless gaming mouse designed for fast and precise control.",
                        "Logitech",
                        "Accessories",
                        159.99,
                        4.7f,
                        true,
                        "logitech,mouse,gaming,wireless",
                        date
                ),

                new ProductEntity(
                        26L,
                        "P1026",
                        "Keychron Q1 Max",
                        "Premium wireless mechanical keyboard designed for productivity and customization.",
                        "Keychron",
                        "Accessories",
                        219.99,
                        4.6f,
                        true,
                        "keychron,keyboard,mechanical,wireless",
                        date
                ),

                new ProductEntity(
                        27L,
                        "P1027",
                        "Anker 737 Power Bank",
                        "High-capacity portable charger for laptops, phones and mobile devices.",
                        "Anker",
                        "Accessories",
                        149.99,
                        4.6f,
                        false,
                        "anker,power-bank,charger,accessory",
                        date
                ),

                new ProductEntity(
                        28L,
                        "P1028",
                        "Samsung T9 Portable SSD",
                        "Fast portable solid-state drive for backups, development files and media.",
                        "Samsung",
                        "Accessories",
                        199.99,
                        4.7f,
                        true,
                        "samsung,ssd,storage,portable",
                        date
                ),

                new ProductEntity(
                        29L,
                        "P1029",
                        "SanDisk Extreme Portable SSD",
                        "Compact portable solid-state drive for fast file storage and transfers.",
                        "SanDisk",
                        "Accessories",
                        149.99,
                        4.5f,
                        true,
                        "sandisk,ssd,storage,portable",
                        date
                ),

                new ProductEntity(
                        30L,
                        "P1030",
                        "Logitech MX Keys S",
                        "Wireless productivity keyboard designed for comfortable multi-device workflows.",
                        "Logitech",
                        "Accessories",
                        119.99,
                        4.7f,
                        true,
                        "logitech,keyboard,wireless,productivity",
                        date
                )
        );
    }
}