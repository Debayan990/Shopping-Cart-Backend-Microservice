package com.cts.service;

import com.cts.dtos.CartDto;
import com.cts.dtos.CartItemInput;
import com.cts.dtos.InventoryDto;
import com.cts.dtos.ItemDto;
import com.cts.entities.Cart;
import com.cts.entities.CartItem;
import com.cts.exception.BadRequestException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceImplTest {

    // @MockitoBean to add mocks to the Spring ApplicationContext
    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private ItemPullService itemPullService;

    @MockitoBean
    private InventoryStockPullService inventoryStockPullService;

    @MockitoBean
    private ModelMapper modelMapper;

    // @Autowired to inject the real service (which will use the mocks above)
    @Autowired
    private CartService cartService;

    private Cart cart;
    private CartItem cartItem;
    private CartDto cartDto;
    private CartItemInput cartItemInput;
    private ItemDto itemDto;
    private InventoryDto inventoryDto;

    @BeforeEach
    void init() {
        // Setup Cart and Items
        cart = new Cart();
        cart.setId(1L);
        cart.setUsername("user");
        cart.setTotalPrice(BigDecimal.valueOf(100.0));
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setItemId(101L);
        cartItem.setItemName("Wireless Mouse");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(50.0));
        cartItem.setSubTotal(BigDecimal.valueOf(100.0));
        cartItem.setCart(cart);

        cart.getItems().add(cartItem);

        // Setup DTOs
        cartDto = new CartDto(1L, "user", BigDecimal.valueOf(100.0), new ArrayList<>());
        cartItemInput = new CartItemInput();
        cartItemInput.setItemId(101L);
        cartItemInput.setQuantity(1);

        // Setup External Data Mocks
        itemDto = new ItemDto(101L, "Wireless Mouse", "Desc", "Electronics", BigDecimal.valueOf(50.0), LocalDateTime.now());

        inventoryDto = new InventoryDto(1L, 101L, 10, "Warehouse A");
    }

    @Test
    void addItemToCart_NewCart() {
        when(cartRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        when(itemPullService.getItemDetails(101L)).thenReturn(itemDto);
        when(inventoryStockPullService.getInventoryStock(101L)).thenReturn(inventoryDto);

        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(modelMapper.map(any(Cart.class), eq(CartDto.class))).thenReturn(cartDto);

        CartDto result = cartService.addItemToCart("newuser", cartItemInput);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
        verify(itemPullService).getItemDetails(101L);
    }

    @Test
    void addItemToCart_ExistingCart_UpdateQuantity() {
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));
        when(itemPullService.getItemDetails(101L)).thenReturn(itemDto);
        when(inventoryStockPullService.getInventoryStock(101L)).thenReturn(inventoryDto);

        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(modelMapper.map(any(Cart.class), eq(CartDto.class))).thenReturn(cartDto);

        CartDto result = cartService.addItemToCart("user", cartItemInput);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItemToCart_OutOfStock() {
        inventoryDto.setQuantity(0); // Set stock to 0

        when(itemPullService.getItemDetails(101L)).thenReturn(itemDto);
        when(inventoryStockPullService.getInventoryStock(101L)).thenReturn(inventoryDto);

        assertThrows(BadRequestException.class, () -> cartService.addItemToCart("user", cartItemInput));

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addItemToCart_ExceedsStock() {
        // Cart already has 2, Requesting 1 more. Set stock to 2. Total needed 3 > 2.
        inventoryDto.setQuantity(2);

        when(itemPullService.getItemDetails(101L)).thenReturn(itemDto);
        when(inventoryStockPullService.getInventoryStock(101L)).thenReturn(inventoryDto);
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));

        assertThrows(BadRequestException.class, () -> cartService.addItemToCart("user", cartItemInput));
    }

    @Test
    void getCartByUsername() {
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));
        when(modelMapper.map(cart, CartDto.class)).thenReturn(cartDto);

        CartDto result = cartService.getCartByUsername("user");

        assertNotNull(result);
        assertEquals("user", result.getUsername());
        verify(cartRepository).findByUsername("user");
    }

    @Test
    void getCartByUsername_NotFound() {
        when(cartRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getCartByUsername("unknown"));
    }

    @Test
    void removeItemFromCart() {
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(modelMapper.map(any(Cart.class), eq(CartDto.class))).thenReturn(cartDto);

        CartDto result = cartService.removeItemFromCart("user", 101L);

        assertNotNull(result);
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_ItemNotFound() {
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));

        // Try to remove item ID 999 which doesn't exist in setup
        assertThrows(ResourceNotFoundException.class, () -> cartService.removeItemFromCart("user", 999L));

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void clearCart() {
        when(cartRepository.findByUsername("user")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.clearCart("user");

        assertEquals("Cart cleared successfully", result);
        verify(cartRepository).save(cart);
        assertTrue(cart.getItems().isEmpty());
    }
}