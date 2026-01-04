package com.cts.controller;

import com.cts.dtos.CartDto;
import com.cts.dtos.CartItemInput;
import com.cts.dtos.SuccessDto;
import com.cts.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<CartDto> addToCart(Authentication authentication, @Valid @RequestBody CartItemInput cartItemInput) {
        // Getting username directly from the Security Context (JWT)
        String username = authentication.getName();
        return ResponseEntity.ok(cartService.addItemToCart(username, cartItemInput));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<CartDto> getCart(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(cartService.getCartByUsername(username));
    }

    @DeleteMapping("/remove/{itemId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<CartDto> removeItem(Authentication authentication, @PathVariable Long itemId) {
        String username = authentication.getName();
        return ResponseEntity.ok(cartService.removeItemFromCart(username, itemId));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SYSTEM')")
    public ResponseEntity<SuccessDto> clearCart(Authentication authentication) {
        String username = authentication.getName();
        SuccessDto result = new SuccessDto(cartService.clearCart(username));
        return ResponseEntity.ok(result);
    }
}