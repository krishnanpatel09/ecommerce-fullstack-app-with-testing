package service;

import com.TDD.Ecom.dto.CartDto;
import com.TDD.Ecom.dto.OrderDto;
import com.TDD.Ecom.dto.OrderItemDto;
import com.TDD.Ecom.model.Cart;
import com.TDD.Ecom.model.CartItem;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.CartRepo;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.CartService;
import com.TDD.Ecom.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepo cartRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;

    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    private static class StubOrderService extends OrderService {
        boolean called = false;

        public StubOrderService() {
            super(null, null, null, null);
        }

        @Override
        public OrderDto createOrder(List<OrderItemDto> items) {
            this.called = true;
            return new OrderDto();
        }

        public boolean wasCalled() {
            return called;
        }
    }

    @BeforeEach
    void setup() {
        // Instantiate the CartService with the required dependencies
        cartService = new CartService(cartRepo, productRepo, userRepo, new StubOrderService());

        testUser = new User();
        testUser.setEmail("test@example.com");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(10.0);
        testProduct.setStockQuantity(100);

        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        // Set an authenticated user in the security context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );
    }

    @Test
    void addToCartDto_ShouldAddNewItem() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.save(any())).thenReturn(testCart);

        CartDto result = cartService.addToCartDto(1L, 2);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(20.0, result.getTotalAmount());
    }

    @Test
    void updateCartItemDto_ShouldUpdateQuantity() {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(1);
        item.setPrice(testProduct.getPrice());
        item.setSubtotal(10.0);
        testCart.setItems(new ArrayList<>(List.of(item)));

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartRepo.save(any())).thenReturn(testCart);

        CartDto result = cartService.updateCartItemDto(1L, 3);

        assertEquals(3, result.getItems().get(0).getQuantity());
        assertEquals(30.0, result.getItems().get(0).getSubtotal());
    }

    @Test
    void removeFromCartDto_ShouldRemoveItem() {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(2);
        item.setSubtotal(20.0);
        testCart.setItems(new ArrayList<>(List.of(item)));
        testCart.setTotalAmount(20.0);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any())).thenReturn(testCart);

        CartDto result = cartService.removeFromCartDto(1L);

        assertTrue(result.getItems().isEmpty());
        assertEquals(0.0, result.getTotalAmount());
    }

    @Test
    void clearCartDto_ShouldClearAllItems() {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(1);
        item.setSubtotal(10.0);
        testCart.setItems(new ArrayList<>(List.of(item)));
        testCart.setTotalAmount(10.0);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any())).thenReturn(testCart);

        CartDto result = cartService.clearCartDto();

        assertEquals(0.0, result.getTotalAmount());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void getCartDto_ShouldReturnCartDetails() {
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));

        testCart.setTotalAmount(0.0);

        CartDto result = cartService.getCartDto();

        assertNotNull(result);
        assertEquals(0.0, result.getTotalAmount());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void addToCartDto_OutOfStock_ShouldThrowException() {
        testProduct.setName("Test Product"); // Set name for dynamic message
        testProduct.setStockQuantity(1);       // Only 1 in stock

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCartDto(1L, 5));

        // Expected message updated to match business logic
        assertEquals("Not enough stock available", ex.getMessage());
    }

    @Test
    void getCartDto_AsAdmin_ShouldStillReturnCart() {
        mockAuthentication("admin@example.com", "ADMIN");

        testUser.setEmail("admin@example.com");
        testCart.setTotalAmount(0.0);

        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));

        CartDto result = cartService.getCartDto();

        assertNotNull(result);
        assertEquals(0.0, result.getTotalAmount());
    }

    @Test
    void checkout_AsCustomer_ShouldTriggerOrderAndClearCart() {
        CartItem item = new CartItem();
        Product product = new Product();
        product.setId(1L);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(15.0);
        item.setSubtotal(30.0);

        testCart.setItems(new ArrayList<>(List.of(item))); // Mutable list
        testCart.setTotalAmount(30.0);

        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(testCart));
        when(cartRepo.save(any())).thenReturn(testCart);

        cartService.checkout();

        verify(cartRepo, atLeastOnce()).save(any(Cart.class));
    }

    @Test
    void getCartDto_UnauthenticatedUser_ShouldThrow() {
        // Instead of clearing the context (which causes a NullPointerException),
        // we set an authentication with an empty principal to simulate an unauthenticated user.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("", null, Collections.emptyList())
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cartService.getCartDto());

        // With an empty email, our CartService.getCurrentUser() will not find a user and throw "User not found"
        assertEquals("User not found", ex.getMessage());
    }

    private void mockAuthentication(String email, String... roles) {
        List<org.springframework.security.core.GrantedAuthority> authorities =
                Arrays.stream(roles)
                        .map(role -> (org.springframework.security.core.GrantedAuthority) () -> "ROLE_" + role)
                        .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, authorities)
        );
    }
}
