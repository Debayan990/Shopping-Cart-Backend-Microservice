package com.cts.service;

import com.cts.client.CartServiceClient;
import com.cts.dtos.CartDto;
import com.cts.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartPullService {

    private final CartServiceClient cartServiceClient;

    @CircuitBreaker(name = "cart-service", fallbackMethod = "getCartFallback")
    @Retry(name = "cart-service")
    public CartDto getCart(String username) {
        log.info("Fetching cart for user: {}", username);
        return cartServiceClient.getCart();
    }

    @CircuitBreaker(name = "cart-service", fallbackMethod = "clearCartFallback")
    @Retry(name = "cart-service")
    public void clearCart(String username) {
        log.info("Clearing cart for user: {}", username);
        cartServiceClient.clearCart();
    }


    public CartDto getCartFallback(String username, Throwable th) {
        log.error("Failed to fetch cart for user: {}. Error: {}", username, th.getMessage());
        throw new ServiceUnavailableException("Cart Service is currently unavailable. Please try again later.");
    }

    public void clearCartFallback(String username, Throwable th) {
        log.error("Failed to clear cart for user: {}. Error: {}", username, th.getMessage());
        // We not want to fail the whole order if clearing fails, but for safety, we alert the user or log it as a warning.
        log.warn("Order placed, but failed to clear cart automatically due to service error.");
    }
}