package service;

import com.TDD.Ecom.dto.ProductDto;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.AuthService;
import com.TDD.Ecom.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductManagementTest {

    private ProductService productService;
    private ProductRepo productRepo;
    private UserRepo userRepo;
    private TestAuthService authService;


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

    @BeforeEach
    void setUp() {
        productRepo = mock(ProductRepo.class);
        userRepo = mock(UserRepo.class);
        authService = new TestAuthService(userRepo);
        productService = new ProductService(productRepo, authService);


        // Set up a mock authenticated user
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin@example.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddProduct_Success() {
        ProductDto dto = new ProductDto(null, "Test Product", "Desc", 9.99, 10, "img.png");
        Product savedProduct = new Product(1L, "Test Product", "Desc", "img.png", 9.99, 10);

        when(productRepo.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.addProduct(dto);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        verify(productRepo, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        Long productId = 1L;
        Product existingProduct = new Product(productId, "Old", "OldDesc", "old.png", 5.99, 5);
        ProductDto updateDto = new ProductDto(productId, "Updated", "NewDesc", 12.99, 20, "new.png");

        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Product.class))).thenReturn(existingProduct);

        ProductDto result = productService.updateProduct(productId, updateDto);

        assertNotNull(result);
        assertEquals("Updated", result.getName());
        verify(productRepo).save(existingProduct);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepo.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(999L);
        });

        assertTrue(exception.getMessage().contains("Product not found with id"));
    }

    @Test
    void testListProducts_ReturnsList() {
        List<Product> productList = Arrays.asList(
                new Product(1L, "P1", "D1", "img1", 1.0, 1),
                new Product(2L, "P2", "D2", "img2", 2.0, 2)
        );

        when(productRepo.findAll()).thenReturn(productList);

        List<ProductDto> results = productService.listProducts();

        assertEquals(2, results.size());
        verify(productRepo, times(1)).findAll();
    }

    @Test
    void testDeleteProduct_Success() {
        Long productId = 1L;

        when(productRepo.existsById(productId)).thenReturn(true);
        doNothing().when(productRepo).deleteById(productId);

        boolean result = productService.deleteProduct(productId);

        assertTrue(result);
        verify(productRepo).deleteById(productId);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepo.existsById(999L)).thenReturn(false);

        boolean result = productService.deleteProduct(999L);

        assertFalse(result);
    }

    @Test
    void testUpdateProduct_ProductNotFound() {
        Long productId = 123L;
        ProductDto dto = new ProductDto(productId, "Name", "Desc", 10.0, 5, "img.png");

        when(productRepo.findById(productId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProduct(productId, dto));
        assertTrue(ex.getMessage().contains("Product not found with id"));
    }

    @Test
    void testUpdateProduct_DuplicateName() {
        Long productId = 1L;
        Product existingProduct = new Product(productId, "Old", "OldDesc", "img.png", 10.0, 5);
        ProductDto updateDto = new ProductDto(productId, "New", "NewDesc", 12.0, 6, "img2");

        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepo.save(any(Product.class))).thenThrow(new DataIntegrityViolationException("Duplicate"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productService.updateProduct(productId, updateDto));
        assertEquals("Product with this name already exists", ex.getMessage());
    }

    @Test
    void testUpdateProduct_ValidationFails() {
        Long productId = 1L;
        Product existingProduct = new Product(productId, "Old", "Desc", "img", 10.0, 5);
        ProductDto dto = new ProductDto(productId, "", "Desc", 10.0, 5, "img");

        when(productRepo.findById(productId)).thenReturn(Optional.of(existingProduct));

        var ex = assertThrows(IllegalArgumentException.class, () -> productService.updateProduct(productId, dto));
        assertEquals("Product name cannot be empty", ex.getMessage());
    }

    @Test
    void testDeleteProduct_UnauthenticatedUser() {
        SecurityContextHolder.clearContext(); // simulate no login
        var ex = assertThrows(SecurityException.class, () -> productService.deleteProduct(1L));
        assertEquals("User is not authenticated", ex.getMessage());
    }

    @Test
    void testDeleteProduct_NonAdmin() {
        TestAuthService testAuthService = (TestAuthService) getPrivateField(productService, "authService");
        testAuthService.setAdminUser(false); // simulate non-admin user

        // Set authenticated user (simulate a logged-in normal user)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", null, Collections.emptyList())
        );

        var ex = assertThrows(SecurityException.class, () -> productService.deleteProduct(1L));
        assertEquals("Access denied. Admin privileges required.", ex.getMessage());
    }

    private static Object getPrivateField(Object target, String fieldName) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access private field", e);
        }
    }



}
