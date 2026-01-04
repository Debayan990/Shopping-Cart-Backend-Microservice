package com.cts.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private LocalDateTime createdAt;
}