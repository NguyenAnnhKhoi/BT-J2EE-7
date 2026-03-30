package com.example.service;

import com.example.model.Product;
import com.example.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProductsPage(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductsPage(int page, int size, String keyword, Integer categoryId, String sort) {
        PageRequest pageable = PageRequest.of(page, size, resolveSort(sort));
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return productRepository.searchProducts(normalizedKeyword, categoryId, pageable);
    }

    private Sort resolveSort(String sort) {
        if ("priceAsc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.ASC, "price");
        }
        if ("priceDesc".equalsIgnoreCase(sort)) {
            return Sort.by(Sort.Direction.DESC, "price");
        }
        return Sort.by(Sort.Direction.ASC, "id");
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public Product getProductById(int id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteProduct(int id) {
        productRepository.deleteById(id);
    }
}
