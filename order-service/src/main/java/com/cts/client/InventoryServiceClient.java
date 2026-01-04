package com.cts.client;

import com.cts.dtos.InventoryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryServiceClient {

    @GetMapping("/api/inventory/item/{itemId}")
    InventoryDto getInventoryByItemId(@PathVariable("itemId") Long itemId);

    @PutMapping("/api/inventory/item/{itemId}")
    InventoryDto updateInventory(@PathVariable("itemId") Long itemId, @RequestBody InventoryDto inventoryDto);
}