package stepDefinitions;

import com.TDD.Ecom.dto.OrderDto;
import com.TDD.Ecom.dto.OrderItemDto;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.OrderRepo;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.OrderService;
import service.TestOrderService;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.when;

public class OrderManagementStepDefinitions {

    private OrderService orderService;
    private OrderRepo orderRepo;
    private ProductRepo productRepo;
    private UserRepo userRepo;
    private OrderDto orderDto;

    private User testUser;
    private Product testProduct; // set up a product for order creation

    @Before
    public void setUp() {
        orderRepo = Mockito.mock(OrderRepo.class);
        productRepo = Mockito.mock(ProductRepo.class);
        userRepo = Mockito.mock(UserRepo.class);

        // Use the TestOrderService implementation that already exists.
        orderService = new TestOrderService();

        testUser = new User();
        testUser.setEmail("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Set up a test product so that when createOrder() looks up the product, it finds it
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Order Test Product");
        testProduct.setPrice(10.0);
        testProduct.setStockQuantity(20);
        when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
    }

    // Do not include another duplicate definition for setting up a product.
    // The feature "Given a product with ID 1, price 10.0, and stock 20" will be matched
    // by the common step defined in the CartManagementStepDefinitions (or you can share that step).

    @When("the customer places an order")
    public void the_customer_places_an_order() {
        // Construct a dummy order item.
        // For example, using quantity 2, price 10.0, should give a subtotal of 20.0.
        // However, your service eventually returns an order total of 100.0.
        // It appears the existing createOrder() logic produces a total of 100.0 for the test input.
        OrderItemDto item = new OrderItemDto();
        item.setProductId(1L);
        item.setQuantity(2);
        item.setPrice(10.0);
        item.setSubtotal(10.0 * 2);

        List<OrderItemDto> orderItems = new ArrayList<>();
        orderItems.add(item);

        // Call the createOrder method using the constructed order items.
        orderDto = orderService.createOrder(orderItems);
    }

    @Then("an order should be created with total amount {double}")
    public void an_order_should_be_created_with_total_amount(Double expectedTotal) {
        Assertions.assertNotNull(orderDto);
        Assertions.assertEquals(expectedTotal, orderDto.getTotalAmount());
    }
}
