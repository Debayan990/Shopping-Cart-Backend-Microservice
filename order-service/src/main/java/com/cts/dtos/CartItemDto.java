package com.cts.dtos;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long itemId;
    private String itemName;
    private int quantity;
    private BigDecimal price;
}