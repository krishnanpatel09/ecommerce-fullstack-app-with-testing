package com.TDD.Ecom.controller;

import com.TDD.Ecom.dto.CartDto;
import com.TDD.Ecom.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CartController {
    private static final Logger logger = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        try {
            return ResponseEntity.ok(cartService.getCartDto());
        } catch (Exception e) {
            logger.error("Failed to get cart: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to get cart: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> requestBody) {
        try {
            logger.info("Received request to add product {} to cart with body: {}", productId, requestBody);
            
            // Log authentication information
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("Authentication: principal={}, authorities={}", 
                auth.getName(), 
                auth.getAuthorities());
            
            if (!requestBody.containsKey("quantity")) {
                logger.warn("Quantity not provided in request body");
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Quantity is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            int quantity = requestBody.get("quantity");
            logger.debug("Adding product {} to cart with quantity {}", productId, quantity);
            
            CartDto cartDto = cartService.addToCartDto(productId, quantity);
            logger.info("Successfully added product {} to cart", productId);
            return ResponseEntity.ok(cartDto);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request to add to cart: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Failed to add item to cart: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add item to cart: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> requestBody) {
        try {
            int quantity = requestBody.getOrDefault("quantity", 1);
            return ResponseEntity.ok(cartService.updateCartItemDto(productId, quantity));
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update cart item: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        try {
            return ResponseEntity.ok(cartService.removeFromCartDto(productId));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to remove item from cart: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> clearCart() {
        try {
            return ResponseEntity.ok(cartService.clearCartDto());
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to clear cart: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        try {
            cartService.checkout();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to checkout: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 