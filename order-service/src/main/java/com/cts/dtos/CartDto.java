package com.cts.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDto {
    private BigDecimal totalPrice;
    private List<CartItemDto> items;
}