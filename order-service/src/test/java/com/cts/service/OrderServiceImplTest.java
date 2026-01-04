package com.cts.service;

import com.cts.dtos.*;
import com.cts.entities.Order;
import com.cts.entities.OrderItem;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderServiceImplTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private CartPullService cartPullService;

    @MockitoBean
    private InventoryPullPushService inventoryPullPushService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Order order;
    private OrderResponseDto orderResponseDto;
    private CartDto cartDto;
    private CartItemDto cartItemDto;
    private InventoryDto inventoryDto;

    @BeforeEach
    void init() {
        // Setup Request
        orderRequest = new OrderRequest();
        orderRequest.setShippingAddress("123 Test St");

        // Setup Cart Data (Using Setters as per Order-Service definition)
        cartItemDto = new CartItemDto();
        cartItemDto.setItemId(101L);
        cartItemDto.setItemName("Wireless Mouse");
        cartItemDto.setQuantity(2);
        cartItemDto.setPrice(BigDecimal.valueOf(50.0));

        List<CartItemDto> items = new ArrayList<>();
        items.add(cartItemDto);

        cartDto = new CartDto();
        cartDto.setTotalPrice(BigDecimal.valueOf(100.0));
        cartDto.setItems(items);

        // Setup Inventory Data (Using Setters)
        inventoryDto = new InventoryDto();
        inventoryDto.setId(1L);
        inventoryDto.setItemId(101L);
        inventoryDto.setQuantity(10);
        inventoryDto.setWarehouseLocation("Warehouse A");

        // Setup Order Entity
        order = new Order();
        order.setId(1L);
        order.setUsername("user");
        order.setTotalPrice(BigDecimal.valueOf(100.0));
        order.setStatus("PLACED");
        order.setShippingAddress("123 Test St");
        order.setOrderDate(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        // Setup Response DTO
        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setUsername("user");
        orderResponseDto.setStatus("PLACED");
        orderResponseDto.setTotalPrice(BigDecimal.valueOf(100.0));
    }

    @Test
    void placeOrder_Success() {
        // Mock Cart Fetch
        when(cartPullService.getCart("user")).thenReturn(cartDto);

        // Mock Inventory Check
        when(inventoryPullPushService.getInventory(101L)).thenReturn(inventoryDto);
        // Mock Inventory Update
        when(inventoryPullPushService.updateInventory(eq(101L), any(InventoryDto.class))).thenReturn(inventoryDto);

        // Mock Order Save
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(any(Order.class), eq(OrderResponseDto.class))).thenReturn(orderResponseDto);

        // Mock Cart Clear (FIX: Use doNothing for void methods)
        doNothing().when(cartPullService).clearCart("user");

        OrderResponseDto result = orderService.placeOrder("user", orderRequest);

        assertNotNull(result);
        assertEquals("PLACED", result.getStatus());
        assertEquals(BigDecimal.valueOf(100.0), result.getTotalPrice());

        verify(cartPullService).getCart("user");
        verify(inventoryPullPushService).getInventory(101L);
        verify(inventoryPullPushService).updateInventory(eq(101L), any(InventoryDto.class));
        verify(orderRepository).save(any(Order.class));
        verify(cartPullService).clearCart("user");
    }

    @Test
    void placeOrder_EmptyCart() {
        CartDto emptyCart = new CartDto();
        emptyCart.setTotalPrice(BigDecimal.ZERO);
        emptyCart.setItems(new ArrayList<>());

        when(cartPullService.getCart("user")).thenReturn(emptyCart);

        assertThrows(BadRequestException.class, () -> orderService.placeOrder("user", orderRequest));

        verify(orderRepository, never()).save(any(Order.class));
        verify(inventoryPullPushService, never()).updateInventory(anyLong(), any(InventoryDto.class));
    }

    @Test
    void placeOrder_OutOfStock() {
        // Set inventory quantity less than requested (requested 2, available 1)
        inventoryDto.setQuantity(1);

        when(cartPullService.getCart("user")).thenReturn(cartDto);
        when(inventoryPullPushService.getInventory(101L)).thenReturn(inventoryDto);

        assertThrows(BadRequestException.class, () -> orderService.placeOrder("user", orderRequest));

        verify(inventoryPullPushService, never()).updateInventory(anyLong(), any(InventoryDto.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getMyOrders() {
        when(orderRepository.findByUsername("user")).thenReturn(List.of(order));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDto.class))).thenReturn(orderResponseDto);

        List<OrderResponseDto> results = orderService.getMyOrders("user");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("user", results.get(0).getUsername());
        verify(orderRepository).findByUsername("user");
    }

    @Test
    void getOrderById() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(modelMapper.map(any(Order.class), eq(OrderResponseDto.class))).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(99L));
        verify(orderRepository).findById(99L);
    }
}