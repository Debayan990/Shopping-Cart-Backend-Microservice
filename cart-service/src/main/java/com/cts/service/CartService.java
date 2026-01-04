package com.cts.service;

import com.cts.dtos.CartDto;
import com.cts.dtos.CartItemInput;

public interface CartService {
    CartDto addItemToCart(String username, CartItemInput cartItemInput);
    CartDto getCartByUsername(String username);
    CartDto removeItemFromCart(String username, Long itemId);
    String clearCart(String username);
}