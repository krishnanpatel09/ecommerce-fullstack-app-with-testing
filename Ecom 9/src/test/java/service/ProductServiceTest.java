package service;

import com.TDD.Ecom.dto.*;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.AuthService;
import com.TDD.Ecom.service.ProductService;
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
public class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private UserRepo userRepo;

    private TestAuthService authService;
    private ProductService productService;

    private ProductDto productDto;
    private Product product;

    private static class TestAuthService extends AuthService {
        private boolean isAdminUser = true;

        public TestAuthService(UserRepo userRepo) {
            super(userRepo, null, null);
        }

        @Override
        public boolean isAdmin(String email) {
            return isAdminUser;
        }

        public void setAdminUser(boolean isAdmin) {
            this.isAdminUser = isAdmin;
        }

        @Override
        public Map<String, String> signup(SignupDto signupDto) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User Account Created successfully");
            response.put("role", isAdminUser ? "ADMIN" : "USER");
            return response;
        }

        @Override
        public Map<String, String> login(LoginDto loginDto) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("role", isAdminUser ? "ADMIN" : "USER");
            return response;
        }
    }

    @BeforeEach
    void setUp() {
        // Setup test data
        productDto = new ProductDto();
        productDto.setName("Test Product");
        productDto.setDescription("Test Description");
        productDto.setPrice(99.99);
        productDto.setStockQuantity(10);

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(99.99);
        product.setStockQuantity(10);

        // Set up authentication
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@example.com", null, List.of(() -> "ROLE_ADMIN"))
        );

        // Setup AuthService with mocked UserRepo
        authService = new TestAuthService(userRepo);
        productService = new ProductService(productRepo, authService);
    }

    @Test
    void addProduct_ShouldReturnSavedProductDto() {
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.addProduct(productDto);

        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(product.getName(), result.getName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
        assertEquals(product.getStockQuantity(), result.getStockQuantity());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProductDto() {
        Long productId = 1L;
        when(productRepo.findById(productId)).thenReturn(Optional.of(product));
        when(productRepo.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.updateProduct(productId, productDto);

        assertNotNull(result);
        assertEquals(product.getId(), result.getId());
        assertEquals(productDto.getName(), result.getName());
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenProductNotFound_ShouldReturnNull() {
        Long productId = 1L;
        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.updateProduct(productId, productDto);
        });

        assertTrue(exception.getMessage().contains("Product not found with id: " + productId));
        verify(productRepo, times(1)).findById(productId);
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WhenProductExists_ShouldReturnTrue() {
        Long productId = 1L;
        when(productRepo.existsById(productId)).thenReturn(true);
        doNothing().when(productRepo).deleteById(productId);

        boolean result = productService.deleteProduct(productId);

        assertTrue(result);
        verify(productRepo, times(1)).existsById(productId);
        verify(productRepo, times(1)).deleteById(productId);
    }

    @Test
    void deleteProduct_WhenProductNotFound_ShouldReturnFalse() {
        Long productId = 1L;
        when(productRepo.existsById(productId)).thenReturn(false);

        boolean result = productService.deleteProduct(productId);

        assertFalse(result);
        verify(productRepo, times(1)).existsById(productId);
        verify(productRepo, never()).deleteById(productId);
    }

    @Test
    void listProducts_ShouldReturnAllProducts() {
        List<Product> products = List.of(product);
        when(productRepo.findAll()).thenReturn(products);

        List<ProductDto> result = productService.listProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(product.getId(), result.get(0).getId());
        assertEquals(product.getName(), result.get(0).getName());
        verify(productRepo, times(1)).findAll();
    }

    @Test
    void addProduct_WhenNotAdmin_ShouldThrowSecurityException() {
        authService.setAdminUser(false);

        assertThrows(SecurityException.class, () -> productService.addProduct(productDto));
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WhenNotAdmin_ShouldThrowSecurityException() {
        Long productId = 1L;
        authService.setAdminUser(false);

        assertThrows(SecurityException.class, () -> productService.updateProduct(productId, productDto));
        verify(productRepo, never()).findById(any());
        verify(productRepo, never()).save(any());
    }

    @Test
    void deleteProduct_WhenNotAdmin_ShouldThrowSecurityException() {
        Long productId = 1L;
        authService.setAdminUser(false);

        assertThrows(SecurityException.class, () -> productService.deleteProduct(productId));
        verify(productRepo, never()).existsById(any());
        verify(productRepo, never()).deleteById(any());
    }

}
