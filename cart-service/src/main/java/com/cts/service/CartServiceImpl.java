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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ItemPullService itemPullService;
    private final InventoryStockPullService inventoryStockPullService;
    private final ModelMapper modelMapper;

    @Override
    public CartDto addItemToCart(String username, CartItemInput cartItemInput) {
        Long itemId = cartItemInput.getItemId();
        int requestedQuantity = cartItemInput.getQuantity();

        // Fetch Item Details
        ItemDto itemDto = itemPullService.getItemDetails(itemId);

        // Validate Stock from Inventory
        InventoryDto inventoryDto = inventoryStockPullService.getInventoryStock(itemId);

        // Check availability for the NEW request
        if (inventoryDto.getQuantity() < requestedQuantity) {
            throw new BadRequestException("Out of Stock! Available quantity: " + inventoryDto.getQuantity());
        }

        // Get or Create Cart
        Cart cart = cartRepository.findByUsername(username)
                .orElse(new Cart());

        if (cart.getUsername() == null) {
            cart.setUsername(username);
            cart.setItems(new ArrayList<>());
            cart.setTotalPrice(BigDecimal.ZERO);
        }

        // Add or Update Item in Cart
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newTotalQuantity = existingItem.getQuantity() + requestedQuantity;

            // Check if total quantity exceeds stock
            if (inventoryDto.getQuantity() < newTotalQuantity) {
                throw new BadRequestException("Cannot add more. Would exceed stock. Available: " + inventoryDto.getQuantity());
            }

            existingItem.setQuantity(newTotalQuantity);
            existingItem.setPrice(itemDto.getPrice());
            existingItem.setSubTotal(itemDto.getPrice().multiply(BigDecimal.valueOf(newTotalQuantity)));
        } else {
            CartItem newItem = new CartItem();
            newItem.setItemId(itemId);
            newItem.setItemName(itemDto.getName());
            newItem.setQuantity(requestedQuantity);
            newItem.setPrice(itemDto.getPrice());
            newItem.setSubTotal(itemDto.getPrice().multiply(BigDecimal.valueOf(requestedQuantity)));
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }

        // Recalculate Total Cart Price
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);

        Cart savedCart = cartRepository.save(cart);
        return modelMapper.map(savedCart, CartDto.class);
    }

    @Override
    public CartDto getCartByUsername(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));
        return modelMapper.map(cart, CartDto.class);
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(String username, Long itemId) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));

        boolean removed = cart.getItems().removeIf(item -> item.getItemId().equals(itemId));

        if (!removed) {
            throw new ResourceNotFoundException("Item in Cart", "id", itemId);
        }

        // Recalculate Total
        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);

        return modelMapper.map(cartRepository.save(cart), CartDto.class);
    }

    @Override
    @Transactional
    public String clearCart(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "username", username));

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        return "Cart cleared successfully";
    }
}