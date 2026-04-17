package com.TDD.Ecom.service;

import com.TDD.Ecom.dto.ProductDto;
import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepo productRepo;
    private final AuthService authService;

    @Autowired
    public ProductService(ProductRepo productRepo, AuthService authService) {
        this.productRepo = productRepo;
        this.authService = authService;
    }

    private void checkAdminAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("User is not authenticated");
        }

        String userEmail = authentication.getName();
        if (!authService.isAdmin(userEmail)) {
            throw new SecurityException("Access denied. Admin privileges required.");
        }
    }

    private void validateProduct(ProductDto productDto) {
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (productDto.getPrice() == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (productDto.getPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (productDto.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
    }

    /**
     * Get a product by its ID.
     *
     * @param productId The ID of the product to retrieve.
     * @return The product as a ProductDto, or null if not found.
     */
    public ProductDto getProductById(Long productId) {
        try {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
            return mapToDto(product);
        } catch (Exception e) {
            logger.error("Error fetching product with id {}: {}", productId, e.getMessage());
            throw new RuntimeException("Error fetching product: " + e.getMessage());
        }
    }

    /**
     * Retrieve a list of all products.
     *
     * @return a list of ProductDto objects.
     */
    public List<ProductDto> listProducts() {
        try {
            logger.debug("Fetching all products");
            List<Product> products = productRepo.findAll();
            return products.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching products from database: {}", e.getMessage());
            throw new RuntimeException("Error fetching products from database: " + e.getMessage());
        }
    }

    /**
     * Add a new product using ProductDto.
     *
     * @param productDto The DTO containing product details.
     * @return The created product as a ProductDto.
     */
    public ProductDto addProduct(ProductDto productDto) {
        checkAdminAccess();
        validateProduct(productDto);

        try {
            Product product = new Product();
            updateProductFromDto(product, productDto);
            Product savedProduct = productRepo.save(product);
            return mapToDto(savedProduct);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Product with this name already exists");
        } catch (Exception e) {
            throw new RuntimeException("Failed to save product: " + e.getMessage());
        }
    }

    /**
     * Update an existing product.
     *
     * @param productId  The ID of the product to update.
     * @param productDto The DTO containing updated product details.
     * @return The updated product DTO, or null if product not found.
     */
    public ProductDto updateProduct(Long productId, ProductDto productDto) {
        checkAdminAccess();
        validateProduct(productDto);

        try {
            Product product = productRepo.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            updateProductFromDto(product, productDto);
            Product updatedProduct = productRepo.save(product);
            return mapToDto(updatedProduct);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Product with this name already exists");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product: " + e.getMessage());
        }
    }

    /**
     * Delete an existing product.
     *
     * @param productId The ID of the product to delete.
     * @return true if the product was found and deleted; false otherwise.
     */
    public boolean deleteProduct(Long productId) {
        checkAdminAccess();
        try {
            if (!productRepo.existsById(productId)) {
                return false;
            }
            productRepo.deleteById(productId);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete product: " + e.getMessage());
        }
    }

    /**
     * Search products by name.
     *
     * @param searchTerm The term to search for in product names.
     * @return a list of ProductDto objects matching the search term.
     */
    public List<ProductDto> searchProducts(String searchTerm) {
        try {
            logger.debug("Searching products with term: {}", searchTerm);
            List<Product> products = productRepo.findByNameContainingIgnoreCase(searchTerm);
            return products.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error searching products: {}", e.getMessage());
            throw new RuntimeException("Error searching products: " + e.getMessage());
        }
    }

    private void updateProductFromDto(Product product, ProductDto dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setImageUrl(dto.getImageUrl());
    }

    // Helper method to convert Product entity to ProductDto
    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }
}
