package com.example.productservice.service;

import com.example.productservice.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class ProductCatalog {

    private static final Logger log = LoggerFactory.getLogger(ProductCatalog.class);

    private final List<Product> products = Arrays.asList(
            new Product("p-100", "Spring Cloud Gateway", new BigDecimal("99.90")),
            new Product("p-200", "Spring Boot Pro", new BigDecimal("149.00")),
            new Product("p-300", "Netflix OSS Toolkit", new BigDecimal("199.00"))
    );

    public List<Product> findAll() {
        log.info("Retrieving all products");
        return products;
    }

    public Optional<Product> findById(String id) {
        log.info("Searching for product with id={}", id);
        return products.stream().filter(product -> product.getId().equals(id)).findFirst();
    }
}
