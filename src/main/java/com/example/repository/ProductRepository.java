package com.example.repository;

import com.example.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
        @Query("""
                        select p from Product p
                        where (:keyword is null or :keyword = '' or lower(p.name) like lower(concat('%', :keyword, '%')))
                            and (:categoryId is null or p.category.id = :categoryId)
                        """)
        Page<Product> searchProducts(@Param("keyword") String keyword,
                        @Param("categoryId") Integer categoryId,
                        Pageable pageable);
}
