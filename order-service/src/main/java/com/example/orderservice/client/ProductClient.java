package com.example.orderservice.client;

import com.example.orderservice.model.ProductSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products")
    List<ProductSummary> findAll();

    @GetMapping("/products/{id}")
    ProductSummary findById(@PathVariable("id") String id);
}
