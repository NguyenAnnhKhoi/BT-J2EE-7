package com.example.config;

import com.example.model.Category;
import com.example.model.Product;
import com.example.model.UserAccount;
import com.example.model.Role;
import com.example.repository.CategoryRepository;
import com.example.repository.ProductRepository;
import com.example.repository.RoleRepository;
import com.example.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(RoleRepository roleRepository,
            UserAccountRepository userAccountRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                Role role = new Role();
                role.setName("ADMIN");
                return roleRepository.save(role);
            });

            Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
                Role role = new Role();
                role.setName("USER");
                return roleRepository.save(role);
            });

            upsertUserAccount(userAccountRepository, passwordEncoder,
                    "admin", "admin@univ.edu.vn", "admin123", Set.of(adminRole));
            upsertUserAccount(userAccountRepository, passwordEncoder,
                    "user001", "user001@shop.vn", "user001123", Set.of(userRole));
            upsertUserAccount(userAccountRepository, passwordEncoder,
                    "user002", "user002@shop.vn", "user002123", Set.of(userRole));

                seedSampleProducts(categoryRepository, productRepository);
        };
    }

            private void seedSampleProducts(CategoryRepository categoryRepository,
                ProductRepository productRepository) {
            if (productRepository.count() > 0) {
                return;
            }

            Category dienThoai = getOrCreateCategory(categoryRepository, "Dien thoai");
            Category laptop = getOrCreateCategory(categoryRepository, "Laptop");
            Category phuKien = getOrCreateCategory(categoryRepository, "Phu kien");
            Category giaDung = getOrCreateCategory(categoryRepository, "Gia dung thong minh");

            saveProduct(productRepository, "iPhone 15 Pro 128GB", "Apple", new BigDecimal("23990000"),
                "https://images.unsplash.com/photo-1592286927505-1def25115558?w=1200", dienThoai);
            saveProduct(productRepository, "Samsung Galaxy S24", "Samsung", new BigDecimal("19990000"),
                "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?w=1200", dienThoai);
            saveProduct(productRepository, "Xiaomi 14", "Xiaomi", new BigDecimal("15990000"),
                "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=1200", dienThoai);

            saveProduct(productRepository, "MacBook Air M3", "Apple", new BigDecimal("28990000"),
                "https://images.unsplash.com/photo-1517336714739-489689fd1ca8?w=1200", laptop);
            saveProduct(productRepository, "Dell XPS 13", "Dell", new BigDecimal("31990000"),
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=1200", laptop);
            saveProduct(productRepository, "Asus ROG Zephyrus G14", "Asus", new BigDecimal("35990000"),
                "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=1200", laptop);

            saveProduct(productRepository, "Tai nghe Sony WH-1000XM5", "Sony", new BigDecimal("7990000"),
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=1200", phuKien);
            saveProduct(productRepository, "Ban phim co Logitech MX", "Logitech", new BigDecimal("3290000"),
                "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=1200", phuKien);
            saveProduct(productRepository, "Chuot Logitech MX Master 3S", "Logitech", new BigDecimal("2490000"),
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=1200", phuKien);

            saveProduct(productRepository, "Robot hut bui Xiaomi S10", "Xiaomi", new BigDecimal("6990000"),
                "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=1200", giaDung);
            saveProduct(productRepository, "Loa thong minh Google Nest", "Google", new BigDecimal("2890000"),
                "https://images.unsplash.com/photo-1543512214-318c7553f230?w=1200", giaDung);
            saveProduct(productRepository, "Camera an ninh TP-Link Tapo", "TP-Link", new BigDecimal("1190000"),
                "https://images.unsplash.com/photo-1558002038-1055907df827?w=1200", giaDung);
            }

            private Category getOrCreateCategory(CategoryRepository categoryRepository, String categoryName) {
            return categoryRepository.findAll().stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElseGet(() -> {
                    Category category = new Category();
                    category.setName(categoryName);
                    return categoryRepository.save(category);
                });
            }

            private void saveProduct(ProductRepository productRepository,
                String name,
                String brand,
                BigDecimal price,
                String image,
                Category category) {
            Product product = new Product();
            product.setName(name);
            product.setBrand(brand);
            product.setPrice(price);
            product.setImage(image);
            product.setCategory(category);
            productRepository.save(product);
            }

    private void upsertUserAccount(UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String rawPassword,
            Set<Role> roles) {
        UserAccount userAccount = userAccountRepository.findByUsername(username).orElseGet(UserAccount::new);
        userAccount.setUsername(username);
        userAccount.setEmail(email);
        userAccount.setPassword(passwordEncoder.encode(rawPassword));
        userAccount.setRoles(roles);
        userAccountRepository.save(userAccount);
    }
}
