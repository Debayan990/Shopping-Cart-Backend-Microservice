package com.cts.controller;

import com.cts.dtos.OrderRequest;
import com.cts.dtos.OrderResponseDto;
import com.cts.security.JwtTokenProvider;
import com.cts.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    // FIX: Mock JwtTokenProvider to handle Authentication injection
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequest orderRequest;
    private OrderResponseDto orderResponseDto;

    @BeforeEach
    void init() {
        orderRequest = new OrderRequest();
        orderRequest.setShippingAddress("123 Test St");

        orderResponseDto = new OrderResponseDto();
        orderResponseDto.setId(1L);
        orderResponseDto.setUsername("user");
        orderResponseDto.setTotalPrice(BigDecimal.valueOf(100.0));
        orderResponseDto.setStatus("PLACED");

        // Stub JWT validation to allow the Security Filter to create Authentication
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("user");
        when(jwtTokenProvider.getRoles(anyString())).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));
    }

    @Test
    void placeOrder() throws Exception {
        when(orderService.placeOrder(eq("user"), any(OrderRequest.class))).thenReturn(orderResponseDto);

        mockMvc.perform(post("/api/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLACED"))
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void placeOrder_InvalidRequest() throws Exception {
        OrderRequest invalidRequest = new OrderRequest(); // Missing shipping address

        mockMvc.perform(post("/api/orders/place")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyOrders() throws Exception {
        when(orderService.getMyOrders("user")).thenReturn(List.of(orderResponseDto));

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].username").value("user"));
    }

    @Test
    void getOrderById() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(orderResponseDto);

        mockMvc.perform(get("/api/orders/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}