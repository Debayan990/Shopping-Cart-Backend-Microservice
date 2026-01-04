package com.cts.service;

import com.cts.client.InventoryServiceClient;
import com.cts.dtos.InventoryDto;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryPullPushService {

    private final InventoryServiceClient inventoryServiceClient;

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "getInventoryFallback")
    @Retry(name = "inventory-service")
    public InventoryDto getInventory(Long itemId) {
        return inventoryServiceClient.getInventoryByItemId(itemId);
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "updateInventoryFallback")
    @Retry(name = "inventory-service")
    public InventoryDto updateInventory(Long itemId, InventoryDto inventoryDto) {
        return inventoryServiceClient.updateInventory(itemId, inventoryDto);
    }


    public InventoryDto getInventoryFallback(Long itemId, Throwable th) {
        log.error("Error fetching inventory for itemId: {}", itemId, th);
        if (th instanceof FeignException.NotFound) {
            throw new ResourceNotFoundException("Inventory Item", "id", itemId);
        }
        throw new ServiceUnavailableException("Inventory Service is unavailable.");
    }

    public InventoryDto updateInventoryFallback(Long itemId, InventoryDto dto, Throwable th) {
        log.error("Error updating inventory for itemId: {}", itemId, th);
        throw new ServiceUnavailableException("Unable to update inventory. Order cannot be processed.");
    }
}