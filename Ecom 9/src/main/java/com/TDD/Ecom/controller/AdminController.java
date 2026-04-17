package com.TDD.Ecom.controller;

import com.TDD.Ecom.dto.OrderDto;
import com.TDD.Ecom.dto.ProductDto;
import com.TDD.Ecom.service.OrderService;
import com.TDD.Ecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public AdminController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }

    // Order Management Endpoints
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<OrderDto> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (SecurityException e) {
            logger.error("Security error in getAllOrders: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Unauthorized access");
            return ResponseEntity.status(403).body(response);
        } catch (Exception e) {
            logger.error("Error in getAllOrders: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to fetch orders: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Status is required"));
            }
            OrderDto updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
            return ResponseEntity.ok(updatedOrder);
        } catch (SecurityException e) {
            logger.error("Security error in updateOrderStatus: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (Exception e) {
            logger.error("Error in updateOrderStatus: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to update order status: " + e.getMessage()));
        }
    }

    // Product Management Endpoints
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            List<ProductDto> products = productService.listProducts();
            return ResponseEntity.ok(products);
        } catch (SecurityException e) {
            logger.error("Security error in getAllProducts: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (Exception e) {
            logger.error("Error in getAllProducts: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to fetch products: " + e.getMessage()));
        }
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody ProductDto productDto) {
        try {
            if (productDto.getStockQuantity() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Stock quantity is required"));
            }
            ProductDto newProduct = productService.addProduct(productDto);
            return ResponseEntity.ok(newProduct);
        } catch (SecurityException e) {
            logger.error("Security error in addProduct: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error in addProduct: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error in addProduct: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to add product: " + e.getMessage()));
        }
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductDto productDto) {
        try {
            if (productDto.getStockQuantity() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Stock quantity is required"));
            }
            ProductDto updatedProduct = productService.updateProduct(productId, productDto);
            return ResponseEntity.ok(updatedProduct);
        } catch (SecurityException e) {
            logger.error("Security error in updateProduct: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (IllegalArgumentException e) {
            logger.error("Validation error in updateProduct: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error in updateProduct: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to update product: " + e.getMessage()));
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        try {
            if (productService.deleteProduct(productId)) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            logger.error("Security error in deleteProduct: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (Exception e) {
            logger.error("Error in deleteProduct: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to delete product: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        try {
            return ResponseEntity.ok(Map.of(
                "totalOrders", orderService.getAllOrders().size(),
                "pendingOrders", orderService.getAllOrders().stream()
                    .filter(order -> "PENDING".equals(order.getStatus()))
                    .count()
            ));
        } catch (SecurityException e) {
            logger.error("Security error in getDashboardStats: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("message", "Unauthorized access"));
        } catch (Exception e) {
            logger.error("Error in getDashboardStats: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to fetch dashboard stats: " + e.getMessage()));
        }
    }
} 