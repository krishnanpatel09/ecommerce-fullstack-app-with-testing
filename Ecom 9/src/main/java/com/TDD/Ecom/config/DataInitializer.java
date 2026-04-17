package com.TDD.Ecom.config;

import com.TDD.Ecom.model.Product;
import com.TDD.Ecom.model.User;
import com.TDD.Ecom.repo.ProductRepo;
import com.TDD.Ecom.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepo productRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    @Override
    public void run(String... args) {
        // Initialize default admin user
        if (userRepo.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setRole("ADMIN");
            userRepo.save(adminUser);

            System.out.println("Default admin user created:");
            System.out.println("Email: admin@example.com");
            System.out.println("Password: admin123");
        }

        // Check if products already exist
        if (productRepo.count() == 0) {
            // Add sample products
            Product product1 = new Product();
            product1.setName("Sample Product 1");
            product1.setDescription("This is a sample product description");
            product1.setPrice(99.99);
            product1.setStockQuantity(10);
            product1.setImageUrl("https://example.com/images/product1.jpg");
            productRepo.save(product1);

            Product product2 = new Product();
            product2.setName("Sample Product 2");
            product2.setDescription("Another sample product description");
            product2.setPrice(149.99);
            product2.setStockQuantity(15);
            product2.setImageUrl("https://example.com/images/product2.jpg");
            productRepo.save(product2);

            Product product3 = new Product();
            product3.setName("Sample Product 3");
            product3.setDescription("Yet another sample product description");
            product3.setPrice(199.99);
            product3.setStockQuantity(20);
            product3.setImageUrl("https://example.com/images/product3.jpg");
            productRepo.save(product3);

            System.out.println("Sample products created");
        }
    }
} 