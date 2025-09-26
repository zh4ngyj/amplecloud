package com.example.productservice.controller;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductCatalog productCatalog;

    public ProductController(ProductCatalog productCatalog) {
        this.productCatalog = productCatalog;
    }

    @GetMapping
    @Operation(summary = "List all products")
    public List<Product> findAll() {
        return productCatalog.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find product by id")
    public ResponseEntity<Product> findById(@PathVariable String id) {
        return productCatalog.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
