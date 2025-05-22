package com.example.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "billing-service", url = "${billing-service.url}")
public interface BillingClient {

    @PostMapping("/api/billing/create/{userId}")
    void createWallet(@PathVariable("userId") UUID userId);
}
