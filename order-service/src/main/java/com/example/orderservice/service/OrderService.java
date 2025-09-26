package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.model.ProductSummary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final ProductClient productClient;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public List<ProductSummary> availableProducts() {
        return productClient.findAll();
    }

    public ProductSummary findProduct(String id) {
        return productClient.findById(id);
    }
}
