package com.TDD.Ecom.repo;

import com.TDD.Ecom.model.Cart;
import com.TDD.Ecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
} 