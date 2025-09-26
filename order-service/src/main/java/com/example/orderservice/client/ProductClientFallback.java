package com.example.orderservice.client;

import com.example.orderservice.model.ProductSummary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public List<ProductSummary> findAll() {
        return Collections.singletonList(
                new ProductSummary("N/A", "Product service unavailable", BigDecimal.ZERO)
        );
    }

    @Override
    public ProductSummary findById(String id) {
        return new ProductSummary(id, "Product service unavailable", BigDecimal.ZERO);
    }
}
