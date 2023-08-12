package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Models.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
