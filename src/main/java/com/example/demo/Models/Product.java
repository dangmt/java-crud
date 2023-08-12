package com.example.demo.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = { "orderItems", "cartItems" })

public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull

    private String name;
    private double price;
    private String image;

    // Getters and setters
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference

    private List<OrderItem> orderItems = new ArrayList<>();
    @JsonManagedReference

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)

    private List<CartItem> cartItems = new ArrayList<>();
}
