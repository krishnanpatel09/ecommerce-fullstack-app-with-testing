package com.TDD.Ecom.service;

import com.TDD.Ecom.dto.*;
import com.TDD.Ecom.model.*;
import com.TDD.Ecom.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private  OrderRepo orderRepo;
    private  ProductRepo productRepo;
    private  UserRepo userRepo;
    private  AuthService authService;

    @Autowired
    public OrderService(OrderRepo orderRepo, ProductRepo productRepo, UserRepo userRepo, AuthService authService) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.authService = authService;
    }

    protected void checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        String userEmail = authentication.getName();
        if (!authService.isAdmin(userEmail)) {
            throw new SecurityException("Access is denied. Admin privileges required.");
        }
    }

    @Transactional
    public OrderDto createOrder(List<OrderItemDto> orderItems) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> items = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemDto itemDto : orderItems) {
            Product product = productRepo.findById(itemDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            if (product.getStockQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice() * itemDto.getQuantity());

            items.add(orderItem);
            totalAmount += orderItem.getSubtotal();

            // Update product stock
            product.setStockQuantity(product.getStockQuantity() - itemDto.getQuantity());
            productRepo.save(product);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepo.save(order);
        return mapToDto(savedOrder);
    }

    public List<OrderDto> getAllOrders() {
        checkAdminAccess();
        return orderRepo.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public List<OrderDto> getUserOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderRepo.findByUserOrderByOrderDateDesc(user).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String newStatus) {
        checkAdminAccess();
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid order status");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepo.save(order);
        return mapToDto(updatedOrder);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Only allow cancellation if user is admin or the order belongs to the user
        if (!authService.isAdmin(userEmail) && !Long.valueOf(order.getUser().getId()).equals(Long.valueOf(user.getId()))) {
            throw new SecurityException("Access denied. You can only cancel your own orders.");
        }

        if (!order.getStatus().equals("PENDING")) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }

        order.setStatus("CANCELLED");
        Order updatedOrder = orderRepo.save(order);
        return mapToDto(updatedOrder);
    }

    private boolean isValidStatus(String status) {
        return status.equals("PENDING") ||
               status.equals("PROCESSING") ||
               status.equals("SHIPPED") ||
               status.equals("DELIVERED") ||
               status.equals("CANCELLED");
    }

    public OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setUserId(Long.valueOf(order.getUser().getId()));
        dto.setOrderItems(order.getItems().stream()
            .map(this::mapToOrderItemDto)
            .collect(Collectors.toList()));
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setOrderDate(order.getOrderDate());
        dto.setLastUpdated(order.getLastUpdated());
        return dto;
    }

    public OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setSubtotal(orderItem.getSubtotal());
        return dto;
    }
}