package com.cts.controllers;

import com.cts.dtos.InventoryDto;
import com.cts.dtos.SuccessResponse;
import com.cts.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<InventoryDto> addInventory(@Valid @RequestBody InventoryDto inventoryDto) {
        return new ResponseEntity<>(inventoryService.addInventory(inventoryDto), HttpStatus.CREATED);
    }

    @PostMapping("/internal")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<InventoryDto> addInventoryInternal(@Valid @RequestBody InventoryDto inventoryDto) {
        return new ResponseEntity<>(inventoryService.addInventoryInternal(inventoryDto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getInventoryById(id));
    }


    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<InventoryDto> getInventoryByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(inventoryService.getInventoryByItemId(itemId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<List<InventoryDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<InventoryDto> updateInventory(@PathVariable Long id, @Valid @RequestBody InventoryDto inventoryDto) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventoryDto));
    }

    @PutMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<InventoryDto> updateInventoryByItemId(@PathVariable Long itemId, @Valid @RequestBody InventoryDto inventoryDto) {
        return ResponseEntity.ok(inventoryService.updateInventoryByItemId(itemId, inventoryDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<SuccessResponse> deleteInventory(@PathVariable Long id) {
        SuccessResponse response = new SuccessResponse(inventoryService.deleteInventory(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/item/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<SuccessResponse> deleteInventoryByItemId(@PathVariable Long itemId) {
        SuccessResponse response = new SuccessResponse(inventoryService.deleteInventoryByItemId(itemId));
        return ResponseEntity.ok(response);
    }
}
