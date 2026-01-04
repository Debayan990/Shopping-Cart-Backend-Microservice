package com.cts.service;

import com.cts.dtos.*;
import com.cts.entities.Order;
import com.cts.entities.OrderItem;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartPullService cartPullService;           // Use Proxy
    private final InventoryPullPushService inventoryPullPushService; // Use Proxy
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderResponseDto placeOrder(String username, OrderRequest orderRequest) {

        // Fetch Cart
        CartDto cartDto = cartPullService.getCart(username);

        if (cartDto == null || cartDto.getItems() == null || cartDto.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot place order.");
        }

        // Validate Stock and Reduce Inventory
        for (CartItemDto item : cartDto.getItems()) {
            // Check Stock
            InventoryDto inventory = inventoryPullPushService.getInventory(item.getItemId());

            if (inventory.getQuantity() < item.getQuantity()) {
                throw new BadRequestException("Item '" + item.getItemName() + "' is out of stock.");
            }

            // Deduct quantity
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());

            // Update Inventory
            inventoryPullPushService.updateInventory(item.getItemId(), inventory);
        }

        // Create Order Entity
        Order order = new Order();
        order.setUsername(username);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setTotalPrice(cartDto.getTotalPrice());

        // Map Items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItemDto cartItem : cartDto.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(cartItem.getItemId());
            orderItem.setItemName(cartItem.getItemName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        // Save Order
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with ID: {}", savedOrder.getId());

        // Clear Cart
        cartPullService.clearCart(username);

        return modelMapper.map(savedOrder, OrderResponseDto.class);
    }

    @Override
    public List<OrderResponseDto> getMyOrders(String username) {
        return orderRepository.findByUsername(username).stream()
                .map(order -> modelMapper.map(order, OrderResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return modelMapper.map(order, OrderResponseDto.class);
    }
}