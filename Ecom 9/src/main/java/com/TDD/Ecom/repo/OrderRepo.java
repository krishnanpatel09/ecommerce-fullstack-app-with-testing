package com.TDD.Ecom.repo;

import com.TDD.Ecom.model.Order;
import com.TDD.Ecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByOrderDateDesc(User user);
} 