package com.cts.controller;

import com.cts.dtos.CartDto;
import com.cts.dtos.CartItemInput;
import com.cts.dtos.SuccessDto;
import com.cts.security.JwtTokenProvider;
import com.cts.service.CartService;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    // Mock JwtTokenProvider to handle Authentication injection
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private CartDto cartDto;
    private CartItemInput cartItemInput;

    @BeforeEach
    void init() {
        cartDto = new CartDto(1L, "user", BigDecimal.valueOf(100.0), new ArrayList<>());

        cartItemInput = new CartItemInput();
        cartItemInput.setItemId(101L);
        cartItemInput.setQuantity(2);

        // Stub JWT validation to allow the Security Filter to create Authentication
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getUsername(anyString())).thenReturn("user");
        when(jwtTokenProvider.getRoles(anyString())).thenReturn(List.of("ROLE_USER"));
    }

    @Test
    void addToCart() throws Exception {
        when(cartService.addItemToCart(eq("user"), any(CartItemInput.class))).thenReturn(cartDto);

        mockMvc.perform(post("/api/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemInput))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void getCart() throws Exception {
        when(cartService.getCartByUsername("user")).thenReturn(cartDto);

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void removeItem() throws Exception {
        when(cartService.removeItemFromCart("user", 101L)).thenReturn(cartDto);

        mockMvc.perform(delete("/api/cart/remove/{itemId}", 101L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"));
    }

    @Test
    void clearCart() throws Exception {
        SuccessDto successDto = new SuccessDto("Cart cleared successfully");
        when(cartService.clearCart("user")).thenReturn(successDto.getMessage());

        mockMvc.perform(delete("/api/cart/clear")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));
    }
}