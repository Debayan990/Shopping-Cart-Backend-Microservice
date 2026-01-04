package com.cts.service;

import com.cts.dtos.OrderRequest;
import com.cts.dtos.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderResponseDto placeOrder(String username, OrderRequest orderRequest);
    List<OrderResponseDto> getMyOrders(String username);
    OrderResponseDto getOrderById(Long id);
}