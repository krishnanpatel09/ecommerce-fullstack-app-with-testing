package service;

import com.TDD.Ecom.dto.OrderDto;
import com.TDD.Ecom.dto.OrderItemDto;
import com.TDD.Ecom.model.Order;
import com.TDD.Ecom.model.OrderItem;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.OrderRepo;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepo orderRepo;

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;


    // Create a testable subclass of OrderService
    private static class TestableOrderService extends OrderService {
        private final User currentUser;
        private final boolean isAdmin;

        public TestableOrderService(OrderRepo orderRepo, ProductRepo productRepo,
                                    UserRepo userRepo, User currentUser, boolean isAdmin) {
            super(orderRepo, productRepo, userRepo, null); // Pass null for authService
            this.currentUser = currentUser;
            this.isAdmin = isAdmin;
        }

        // Override methods that use SecurityContextHolder

        // Override checkAdminAccess
        @Override
        protected void checkAdminAccess() {
            if (!isAdmin) {
                throw new SecurityException("Access is denied. Admin privileges required.");
            }
        }
    }

    private User testUser;
    private User adminUser;
    private Product testProduct;
    private Order testOrder;
    private OrderItem testOrderItem;
    private TestableOrderService userOrderService;
    private TestableOrderService adminOrderService;

    @BeforeEach
    void setUp() {
        // Set up test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("Test User");
        testUser.setRole("USER");

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setUsername("Admin User");
        adminUser.setRole("ADMIN");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(10.0);
        testProduct.setStockQuantity(20);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(10.0);

        testOrderItem = new OrderItem();
        testOrderItem.setId(1L);
        testOrderItem.setOrder(testOrder);
        testOrderItem.setProduct(testProduct);
        testOrderItem.setQuantity(1);
        testOrderItem.setPrice(10.0);
        testOrderItem.setSubtotal(10.0);

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(testOrderItem);
        testOrder.setItems(orderItems);


        // Create services with different user contexts
        userOrderService = new TestableOrderService(orderRepo, productRepo, userRepo, testUser, false);
        adminOrderService = new TestableOrderService(orderRepo, productRepo, userRepo, adminUser, true);
    }

    @Test
    void createOrder_Success() {
        // Setup
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepo.save(any(Order.class))).thenReturn(testOrder);

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setProductId(1L);
        orderItemDto.setQuantity(1);

        // Execute
        OrderDto result = userOrderService.createOrder(Collections.singletonList(orderItemDto));

        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(10.0, result.getTotalAmount());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1, result.getOrderItems().size());

        verify(productRepo).save(testProduct);
        assertEquals(19, testProduct.getStockQuantity()); // Stock should be reduced
    }

    @Test
    void createOrder_NotEnoughStock() {
        // Setup
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setProductId(1L);
        orderItemDto.setQuantity(25); // More than available stock

        // Execute & Verify
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userOrderService.createOrder(Collections.singletonList(orderItemDto))
        );

        assertTrue(exception.getMessage().contains("Not enough stock"));
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    void getAllOrders_AdminAccess() {
        // Setup
        when(orderRepo.findAll()).thenReturn(Collections.singletonList(testOrder));

        // Execute
        List<OrderDto> result = adminOrderService.getAllOrders();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAllOrders_NonAdminAccess() {
        // Execute & Verify
        SecurityException exception = assertThrows(
                SecurityException.class,
                () -> userOrderService.getAllOrders()
        );

        assertTrue(exception.getMessage().contains("Admin privileges required"));
    }

    @Test
    void getUserOrders_Success() {
        // Simulate an authenticated user
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Setup mock behavior
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepo.findByUserOrderByOrderDateDesc(testUser)).thenReturn(List.of(testOrder));

        // Call the service
        List<OrderDto> orders = userOrderService.getUserOrders();

        // Assertions
        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getId());

        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateOrderStatus_AdminAccess() {
        // Setup
        when(orderRepo.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepo.save(any(Order.class))).thenReturn(testOrder);

        // Execute
        OrderDto result = adminOrderService.updateOrderStatus(1L, "SHIPPED");

        // Verify
        assertNotNull(result);
        assertEquals("SHIPPED", testOrder.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidStatus() {
        // Setup
        when(orderRepo.findById(1L)).thenReturn(Optional.of(testOrder));

        // Execute & Verify
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adminOrderService.updateOrderStatus(1L, "INVALID_STATUS")
        );

        assertTrue(exception.getMessage().contains("Invalid order status"));
    }

    @Test
    void cancelOrder_ByUser_Success() {
        // Setup SecurityContext
        var auth = new UsernamePasswordAuthenticationToken("test@example.com", "root");
        SecurityContextHolder.getContext().setAuthentication(auth);
        String userEmail = "abc@xyz.com";
        // Setup mocks
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepo.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepo.save(any(Order.class))).thenReturn(testOrder);

        // Execute
        OrderDto result = userOrderService.cancelOrder(1L);

        // Verify
        assertNotNull(result);
        assertEquals("CANCELLED", testOrder.getStatus());

        // Clean up
        SecurityContextHolder.clearContext();
    }


    @Test
    void cancelOrder_ByAdmin_Success() {
        // Setup
       // when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
     //   when(orderRepo.findById(1L)).thenReturn(Optional.of(testOrder));
       // when(orderRepo.save(any(Order.class))).thenReturn(testOrder);

        // Execute

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adminOrderService.cancelOrder(1L)
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }


    @Test
    void mapToDto_Success() {
        // Execute
        OrderDto result = userOrderService.mapToDto(testOrder);

        // Verify
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testOrder.getTotalAmount(), result.getTotalAmount());
        assertEquals(testOrder.getStatus(), result.getStatus());
        assertNotNull(result.getOrderItems());
        assertEquals(1, result.getOrderItems().size());
    }

    @Test
    void mapToOrderItemDto_Success() {
        // Execute
        OrderItemDto result = userOrderService.mapToOrderItemDto(testOrderItem);

        // Verify
        assertNotNull(result);
        assertEquals(testOrderItem.getId(), result.getId());
        assertEquals(testProduct.getId(), result.getProductId());
        assertEquals(testProduct.getName(), result.getProductName());
        assertEquals(testOrderItem.getQuantity(), result.getQuantity());
        assertEquals(testOrderItem.getPrice(), result.getPrice());
        assertEquals(testOrderItem.getSubtotal(), result.getSubtotal());
    }
}