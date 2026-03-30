package com.example.repository;

import com.example.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserAccountUsernameOrderByOrderDateDescIdDesc(String username);

    List<Order> findAllByOrderByOrderDateDescIdDesc();
}
