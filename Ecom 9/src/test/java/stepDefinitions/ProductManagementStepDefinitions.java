package stepDefinitions;

import com.TDD.Ecom.dto.ProductDto;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.ProductService;
import com.TDD.Ecom.service.AuthService;

import io.cucumber.java.en.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductManagementStepDefinitions {

    private ProductRepo productRepo;
    private UserRepo userRepo;
    private ProductService productService;
    private TestAuthService authService;

    private ProductDto resultDto;
    private Exception caughtException;

    private static class TestAuthService extends AuthService {
        private boolean isAdmin = true;

        public TestAuthService(UserRepo userRepo) {
            super(userRepo, null, null);
        }

        public void setAdminUser(boolean isAdmin) {
            this.isAdmin = isAdmin;
        }

        @Override
        public boolean isAdmin(String email) {
            return isAdmin;
        }
    }

    @Given("an authenticated admin user")
    public void an_authenticated_admin_user() {
        productRepo = mock(ProductRepo.class);
        userRepo = mock(UserRepo.class);
        authService = new TestAuthService(userRepo);
        authService.setAdminUser(true);
        productService = new ProductService(productRepo, authService);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@example.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @When("they add a new product with name {string} and price {double}")
    public void they_add_a_new_product_with_name_and_price(String name, double price) {
        ProductDto dto = new ProductDto(null, name, "Desc", price, 10, "img.png");
        Product saved = new Product(1L, name, "Desc", "img.png", price, 10);

        when(productRepo.save(any(Product.class))).thenReturn(saved);
        resultDto = productService.addProduct(dto);
    }

    @Then("the product should be added successfully with name {string}")
    public void the_product_should_be_added_successfully_with_name(String expectedName) {
        assertNotNull(resultDto);
        assertEquals(expectedName, resultDto.getName());
    }

    @When("they update the product with ID {int} with name {string} and price {double}")
    public void they_update_the_product_with_ID_with_name_and_price(Integer id, String name, double price) {
        Product existing = new Product(Long.valueOf(id), "Old", "OldDesc", "old.png", 5.99, 5);
        ProductDto dto = new ProductDto(Long.valueOf(id), name, "NewDesc", price, 20, "new.png");

        when(productRepo.findById(Long.valueOf(id))).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Product.class))).thenReturn(existing);

        resultDto = productService.updateProduct(Long.valueOf(id), dto);
    }

    @Then("the product should be updated with name {string}")
    public void the_product_should_be_updated_with_name(String expectedName) {
        assertNotNull(resultDto);
        assertEquals(expectedName, resultDto.getName());
    }

    @When("they get product by ID {int}")
    public void they_get_product_by_ID(Integer id) {
        when(productRepo.findById(Long.valueOf(id))).thenReturn(Optional.empty());

        try {
            productService.getProductById(Long.valueOf(id));
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("a {string} exception should be thrown")
    public void a_exception_should_be_thrown(String expectedMessagePart) {
        assertNotNull(caughtException);
        assertTrue(caughtException.getMessage().contains(expectedMessagePart));
    }

    @When("they list all products")
    public void they_list_all_products() {
        List<Product> list = Arrays.asList(
                new Product(1L, "P1", "D1", "img1", 1.0, 1),
                new Product(2L, "P2", "D2", "img2", 2.0, 2)
        );

        when(productRepo.findAll()).thenReturn(list);
        List<ProductDto> results = productService.listProducts();
        assertEquals(2, results.size());
    }

    @Then("the product list should contain {int} products")
    public void the_product_list_should_contain_products(Integer count) {
        List<ProductDto> results = productService.listProducts();
        assertEquals(count, results.size());
    }

    @When("they delete the product with ID {int}")
    public void they_delete_the_product_with_ID(Integer id) {
        when(productRepo.existsById(Long.valueOf(id))).thenReturn(true);
        doNothing().when(productRepo).deleteById(Long.valueOf(id));

        boolean deleted = productService.deleteProduct(Long.valueOf(id));
        assertTrue(deleted);
    }

    @Given("an unauthenticated user")
    public void an_unauthenticated_user() {
        // Clear any authentication
        SecurityContextHolder.clearContext();

        productRepo = mock(ProductRepo.class);
        userRepo = mock(UserRepo.class);
        authService = new TestAuthService(userRepo);
        authService.setAdminUser(true);
        productService = new ProductService(productRepo, authService);
    }

    @When("they attempt to delete a product with ID {int}")
    public void they_attempt_to_delete_a_product_with_ID(Integer id) {
        try {
            productService.deleteProduct(Long.valueOf(id));
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @Then("a SecurityException with message {string} should be thrown")
    public void a_SecurityException_with_message_should_be_thrown(String expectedMessage) {
        assertNotNull(caughtException);
        assertEquals(expectedMessage, caughtException.getMessage());
    }

    @Given("an authenticated non-admin user")
    public void an_authenticated_non_admin_user() {
        productRepo = mock(ProductRepo.class);
        userRepo = mock(UserRepo.class);
        authService = new TestAuthService(userRepo);
        authService.setAdminUser(false);  // Simulate non-admin user
        productService = new ProductService(productRepo, authService);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @When("they try to update non-existing product with ID {int}")
    public void they_try_to_update_non_existing_product_with_ID(Integer id) {
        ProductDto dto = new ProductDto(Long.valueOf(id), "Name", "Desc", 10.0, 5, "img.png");
        when(productRepo.findById(Long.valueOf(id))).thenReturn(Optional.empty());

        try {
            productService.updateProduct(Long.valueOf(id), dto);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("they try to update product with duplicate name")
    public void they_try_to_update_product_with_duplicate_name() {
        Long productId = 1L;
        Product existing = new Product(productId, "Old", "OldDesc", "img.png", 10.0, 5);
        ProductDto dto = new ProductDto(productId, "New", "NewDesc", 12.0, 6, "img2");

        when(productRepo.findById(productId)).thenReturn(Optional.of(existing));
        when(productRepo.save(any(Product.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        try {
            productService.updateProduct(productId, dto);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("they try to update product with empty name")
    public void they_try_to_update_product_with_empty_name() {
        Long productId = 1L;
        Product existing = new Product(productId, "Old", "Desc", "img", 10.0, 5);
        ProductDto dto = new ProductDto(productId, "", "Desc", 10.0, 5, "img");

        when(productRepo.findById(productId)).thenReturn(Optional.of(existing));

        try {
            productService.updateProduct(productId, dto);
        } catch (Exception e) {
            caughtException = e;
        }
    }

    @When("they try to delete a non-existing product with ID {int}")
    public void they_try_to_delete_a_non_existing_product_with_ID(Integer id) {
        when(productRepo.existsById(Long.valueOf(id))).thenReturn(false);
        boolean deleted = productService.deleteProduct(Long.valueOf(id));
        assertFalse(deleted);
    }

    @Then("the product should be deleted successfully")
    public void the_product_should_be_deleted_successfully() {
        // Already validated in the When step with assertTrue()
    }

    @Then("the product should not be deleted")
    public void the_product_should_not_be_deleted() {
        // Already validated in the When step with assertFalse()
    }

}
