package com.cts.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemInput {
    @NotNull(message = "Item ID is required")
    private Long itemId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}