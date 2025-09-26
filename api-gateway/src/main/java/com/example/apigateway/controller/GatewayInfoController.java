package com.example.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
@Tag(name = "Gateway", description = "Gateway helper endpoints")
public class GatewayInfoController {

    private final DiscoveryClient discoveryClient;

    public GatewayInfoController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/services")
    @Operation(summary = "List registered services")
    public Map<String, Object> services() {
        Map<String, Object> payload = new HashMap<String, Object>();
        List<String> services = discoveryClient.getServices();
        payload.put("registeredServices", services);
        return payload;
    }
}
