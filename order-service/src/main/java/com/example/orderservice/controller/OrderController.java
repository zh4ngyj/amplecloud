package com.example.orderservice.controller;

import com.example.orderservice.model.ProductSummary;
import com.example.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order operations backed by product service")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/catalog")
    @Operation(summary = "List catalog via Feign client")
    public List<ProductSummary> catalog() {
        return orderService.availableProducts();
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Mock order creation for a product")
    public Map<String, Object> order(@PathVariable String productId) {
        ProductSummary product = orderService.findProduct(productId);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("orderId", "order-" + productId);
        response.put("product", product);
        response.put("status", "CREATED");
        return response;
    }
}
