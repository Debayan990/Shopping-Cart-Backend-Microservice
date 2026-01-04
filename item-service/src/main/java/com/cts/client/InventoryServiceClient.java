package com.cts.client;

import com.cts.dtos.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    @PostMapping("/api/inventory/internal")
    InventoryDto addInventoryInternal(@RequestBody InventoryDto inventoryDto);

    @DeleteMapping("/api/inventory/item/{itemId}")
    void deleteInventoryByItemId(@PathVariable("itemId") Long itemId);
}