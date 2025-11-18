package com.example.orderservice.service;

import com.example.orderservice.client.ProductClient;
import com.example.orderservice.model.ProductSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final ProductClient productClient;

    public OrderService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public List<ProductSummary> availableProducts() {
        log.info("Retrieving available products from product service");
        return productClient.findAll();
    }

    public ProductSummary findProduct(String id) {
        log.info("Finding product with id={} from product service", id);
        return productClient.findById(id);
    }
}
