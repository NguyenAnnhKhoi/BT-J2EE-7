package com.example.dto;

import com.example.model.Product;

import java.math.BigDecimal;

public record CartItemView(
        Product product,
        int quantity,
        BigDecimal lineTotal) {
}
