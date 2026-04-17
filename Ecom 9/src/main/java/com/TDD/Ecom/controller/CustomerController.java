package com.TDD.Ecom.controller;

import com.TDD.Ecom.dto.*;
import com.TDD.Ecom.service.CartService;
import com.TDD.Ecom.service.OrderService;
import com.TDD.Ecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*")
public class CustomerController {
    private final CartService cartService;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public CustomerController(CartService cartService, OrderService orderService, ProductService productService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.productService = productService;
    }

    // Product Browsing Endpoints
    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(productService.listProducts());
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long productId) {
        ProductDto product = productService.getProductById(productId);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.notFound().build();
    }

    // Shopping Cart Endpoints
    @GetMapping("/cart")
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(cartService.getCartDto());
    }

    @PostMapping("/cart/items")
    public ResponseEntity<CartDto> addToCart(
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCartDto(request.getProductId(), request.getQuantity()));
    }

    @PutMapping("/cart/items/{productId}")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable Long productId,
            @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItemDto(productId, request.getQuantity()));
    }

    @DeleteMapping("/cart/items/{productId}")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeFromCartDto(productId));
    }

    @DeleteMapping("/cart")
    public ResponseEntity<CartDto> clearCart() {
        return ResponseEntity.ok(cartService.clearCartDto());
    }

    // Order Management Endpoints
    @PostMapping("/orders")
    public ResponseEntity<Void> checkout() {
        cartService.checkout();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        return ResponseEntity.ok(orderService.getUserOrders());
    }

    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}

class CartItemRequest {
    private Long productId;
    private int quantity;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
} 