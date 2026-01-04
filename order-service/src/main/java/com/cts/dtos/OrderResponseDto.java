package com.cts.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponseDto {
    private Long id;
    private String username;
    private BigDecimal totalPrice;
    private String status;
    private String shippingAddress;
    private LocalDateTime orderDate;
    private List<OrderItemDto> items;
}