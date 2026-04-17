package com.TDD.Ecom.service;

import com.TDD.Ecom.dto.CartDto;
import com.TDD.Ecom.dto.CartItemDto;
import com.TDD.Ecom.dto.OrderItemDto;
import com.TDD.Ecom.model.Cart;
import com.TDD.Ecom.model.CartItem;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.CartRepo;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepo cartRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderService orderService;

    @Autowired
    public CartService(CartRepo cartRepo, ProductRepo productRepo, UserRepo userRepo, OrderService orderService) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.orderService = orderService;
    }

    private static OrderItemDto apply(CartItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setProductId(item.getProduct().getId());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepo.findByUser(user)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setTotalAmount(0.0);
                return cartRepo.save(newCart);
            });
    }

    @Transactional
    public Cart addToCart(Long productId, int quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setSubtotal(item.getPrice() * item.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPrice(product.getPrice());
            newItem.setSubtotal(product.getPrice() * quantity);
            cart.getItems().add(newItem);
        }

        cart.updateTotalAmount();
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart updateCartItem(Long productId, int quantity) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(quantity);
            item.setSubtotal(item.getPrice() * quantity);
        }

        cart.setTotalAmount(cart.getItems().stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum());

        return cartRepo.save(cart);
    }

    @Transactional
    public Cart removeFromCart(Long productId) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));

        cart.setTotalAmount(cart.getItems().stream()
            .mapToDouble(CartItem::getSubtotal)
            .sum());

        return cartRepo.save(cart);
    }

    @Transactional
    public Cart clearCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cart.setTotalAmount(0.0);
        return cartRepo.save(cart);
    }

    public Cart getCart() {
        User user = getCurrentUser();
        return getOrCreateCart(user);
    }

    @Transactional
    public void checkout() {
        Cart cart = getCart();
        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItemDto> orderItems = cart.getItems().stream()
            .map(CartService::apply)
            .collect(Collectors.toList());

        orderService.createOrder(orderItems);
        clearCart();
    }

    public CartDto getCartDto() {
        return mapToDto(getCart());
    }

    @Transactional
    public CartDto addToCartDto(Long productId, int quantity) {
        return mapToDto(addToCart(productId, quantity));
    }

    @Transactional
    public CartDto updateCartItemDto(Long productId, int quantity) {
        return mapToDto(updateCartItem(productId, quantity));
    }

    @Transactional
    public CartDto removeFromCartDto(Long productId) {
        return mapToDto(removeFromCart(productId));
    }

    @Transactional
    public CartDto clearCartDto() {
        return mapToDto(clearCart());
    }

    private CartDto mapToDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setTotalAmount(cart.getTotalAmount());
        dto.setItems(cart.getItems().stream()
            .map(this::mapToCartItemDto)
            .collect(Collectors.toList()));
        return dto;
    }

    private CartItemDto mapToCartItemDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
} 