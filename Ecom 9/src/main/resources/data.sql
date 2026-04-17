-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, role)
SELECT 'admin', 'admin@example.com', '$2a$10$rDkqAqvZZ7YxU.ZqYNJ7H.wQI.JkFAzx4EyYV3sYQF8jLWrEjuQXi', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, image_url)
SELECT 'Sample Product 1', 'This is a sample product description', 99.99, 10, 'https://example.com/images/product1.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sample Product 1');

INSERT INTO products (name, description, price, stock_quantity, image_url)
SELECT 'Sample Product 2', 'Another sample product description', 149.99, 15, 'https://example.com/images/product2.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sample Product 2');

INSERT INTO products (name, description, price, stock_quantity, image_url)
SELECT 'Sample Product 3', 'Yet another sample product description', 199.99, 20, 'https://example.com/images/product3.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sample Product 3');

-- Default regular user (password: user123)
INSERT INTO users (username, email, password, role)
SELECT 'user', 'user@example.com', '$2a$10$lF9pZNdHbXvEtg.gxVNE8.YlxFHr0T4pVcOHHxQxuv7hJYGK7Zcuy', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'user@example.com'); 