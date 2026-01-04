package com.cts.dtos;

import lombok.Data;
@Data
public class InventoryDto {
    private Long id;
    private Long itemId;
    private Integer quantity;
    private String warehouseLocation;
}