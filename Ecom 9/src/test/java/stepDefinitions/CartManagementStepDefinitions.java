package stepDefinitions;

import com.TDD.Ecom.dto.CartDto;
import com.TDD.Ecom.model.Cart;
import com.TDD.Ecom.model.CartItem;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.CartRepo;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.CartService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CartManagementStepDefinitions {

    private CartService cartService;
    private CartRepo cartRepo;
    private ProductRepo productRepo;
    private UserRepo userRepo;
    private OrderService orderService;

    private User testUser;
    private Product testProduct;
    private CartDto cartDto;
    private Exception exception;

    @Before
    public void setUp() {
        cartRepo = Mockito.mock(CartRepo.class);
        productRepo = Mockito.mock(ProductRepo.class);
        userRepo = Mockito.mock(UserRepo.class);

        orderService = new TestOrderService();
        cartService = new CartService(cartRepo, productRepo, userRepo, orderService);

        testUser = new User();
        testUser.setEmail("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@example.com", null, Collections.emptyList())
        );
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    }

    @Given("a product with ID {long}, price {double}, and stock {int}")
    public void a_product_with_details(Long id, Double price, Integer stock) {
        testProduct = new Product();
        testProduct.setId(id);
        testProduct.setName("Test Product");
        testProduct.setPrice(price);
        testProduct.setStockQuantity(stock);
        when(productRepo.findById(id)).thenReturn(Optional.of(testProduct));
    }

    @When("the customer adds {int} of product with ID {long} to the cart")
    public void the_customer_adds_product_to_cart(Integer quantity, Long productId) {
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(0.0);
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);
        cartDto = cartService.addToCartDto(productId, quantity);
    }

    @Then("the cart should contain the product and total amount should be {double}")
    public void the_cart_should_contain_product_and_total(Double expectedTotal) {
        Assertions.assertNotNull(cartDto);
        Assertions.assertEquals(expectedTotal, cartDto.getTotalAmount());
        Assertions.assertFalse(cartDto.getItems().isEmpty());
    }

    @When("the customer tries to add {int} of product with ID {long} exceeding stock")
    public void the_customer_tries_to_add_exceeding_stock(Integer quantity, Long productId) {
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        try {
            cartService.addToCartDto(productId, quantity);
        } catch (Exception e) {
            exception = e;
        }
    }

    @Then("an error should be thrown with message {string}")
    public void an_error_should_be_thrown(String message) {
        Assertions.assertNotNull(exception);
        Assertions.assertEquals(message, exception.getMessage());
    }

    @And("the customer updates the quantity of product with ID {long} to {int}")
    public void the_customer_updates_the_quantity(Long productId, Integer quantity) {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(1);
        item.setPrice(testProduct.getPrice());
        item.setSubtotal(testProduct.getPrice());

        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>(List.of(item)));
        cart.setTotalAmount(testProduct.getPrice());
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);
        cartDto = cartService.updateCartItemDto(productId, quantity);
    }

    @Then("the cart should reflect updated subtotal of {double}")
    public void the_cart_should_reflect_updated_subtotal(Double expectedSubtotal) {
        Assertions.assertNotNull(cartDto);
        Assertions.assertFalse(cartDto.getItems().isEmpty());
        Assertions.assertEquals(expectedSubtotal, cartDto.getItems().get(0).getSubtotal());
    }

    @And("the customer removes the product with ID {long} from the cart")
    public void the_customer_removes_the_product_from_cart(Long productId) {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(1);
        item.setSubtotal(testProduct.getPrice());

        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>(List.of(item)));
        cart.setTotalAmount(testProduct.getPrice());
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);
        cartDto = cartService.removeFromCartDto(productId);
    }

    @Then("the cart should be empty with total amount {double}")
    public void the_cart_should_be_empty(Double expectedTotal) {
        Assertions.assertNotNull(cartDto);
        Assertions.assertTrue(cartDto.getItems().isEmpty());
        Assertions.assertEquals(expectedTotal, cartDto.getTotalAmount());
    }

    @And("the customer clears the cart")
    public void the_customer_clears_the_cart() {
        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>());
        cart.setTotalAmount(0.0);
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);
        cartDto = cartService.clearCartDto();
    }

    @And("the customer proceeds to checkout")
    public void the_customer_proceeds_to_checkout() {
        CartItem item = new CartItem();
        item.setProduct(testProduct);
        item.setQuantity(2);
        item.setPrice(testProduct.getPrice());
        item.setSubtotal(testProduct.getPrice() * 2);

        Cart cart = new Cart();
        cart.setUser(testUser);
        cart.setItems(new ArrayList<>(List.of(item)));
        cart.setTotalAmount(item.getSubtotal());
        when(cartRepo.findByUser(testUser)).thenReturn(Optional.of(cart));
        when(cartRepo.save(any())).thenReturn(cart);
        cartService.checkout();
        cartDto = cartService.getCartDto();
    }
}
