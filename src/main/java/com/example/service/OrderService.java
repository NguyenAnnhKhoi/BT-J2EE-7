package com.example.service;

import com.example.dto.CheckoutRequest;
import com.example.model.OrderDetail;
import com.example.model.Order;
import com.example.model.Product;
import com.example.model.UserAccount;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;
import com.example.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserAccountRepository userAccountRepository;
    private final ProductRepository productRepository;
    private final JdbcTemplate jdbcTemplate;

    public OrderService(OrderRepository orderRepository,
            UserAccountRepository userAccountRepository,
            ProductRepository productRepository,
            JdbcTemplate jdbcTemplate) {
        this.orderRepository = orderRepository;
        this.userAccountRepository = userAccountRepository;
        this.productRepository = productRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Integer placeOrderFromCart(String username, Map<Integer, Integer> cart, CheckoutRequest checkoutRequest) {
        ensureLegacyCustomerOrderSchemaCompatible();

        if (cart == null || cart.isEmpty()) {
            LOGGER.warn("Checkout failed: cart is empty for user {}", username);
            return null;
        }

        if (checkoutRequest == null
                || isBlank(checkoutRequest.getReceiverName())
                || isBlank(checkoutRequest.getReceiverPhone())
                || isBlank(checkoutRequest.getShippingAddress())
                || isBlank(checkoutRequest.getPaymentMethod())) {
            LOGGER.warn("Checkout failed: missing receiver/shipping/payment info for user {}", username);
            return null;
        }

        UserAccount userAccount = userAccountRepository.findByUsername(username).orElse(null);
        if (userAccount == null) {
            LOGGER.warn("Checkout failed: user account not found for username {}", username);
            return null;
        }

        Order order = new Order();
        order.setUserAccount(userAccount);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(BigDecimal.ZERO);
        order.setReceiverName(checkoutRequest.getReceiverName().trim());
        order.setReceiverPhone(checkoutRequest.getReceiverPhone().trim());
        order.setShippingAddress(checkoutRequest.getShippingAddress().trim());
        order.setNote(isBlank(checkoutRequest.getNote()) ? null : checkoutRequest.getNote().trim());

        String paymentMethod = normalizePaymentMethod(checkoutRequest.getPaymentMethod());
        order.setPaymentMethod(paymentMethod);

        if ("COD".equals(paymentMethod)) {
            order.setPaymentStatus("PENDING_CONFIRMATION");
            order.setOrderStatus("PENDING_CONFIRMATION");
            order.setPaidAt(null);
        } else {
            order.setPaymentStatus("CONFIRMED");
            order.setOrderStatus("CONFIRMED");
            order.setPaidAt(LocalDateTime.now());
        }

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Integer productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity == null || quantity <= 0) {
                continue;
            }

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                LOGGER.warn("Checkout warning: product id {} not found, skipping", productId);
                continue;
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(quantity);
            detail.setUnitPrice(product.getPrice());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            detail.setLineTotal(lineTotal);

            totalAmount = totalAmount.add(lineTotal);
            details.add(detail);
        }

        if (details.isEmpty()) {
            LOGGER.warn("Checkout failed: no valid cart lines remain for user {}", username);
            return null;
        }

        // Save order header first, then insert detail lines explicitly with product_id.
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        for (OrderDetail detail : details) {
            jdbcTemplate.update(
                    "INSERT INTO order_detail(order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)",
                    savedOrder.getId(),
                    detail.getProduct().getId(),
                    detail.getQuantity(),
                    detail.getUnitPrice(),
                    detail.getLineTotal()
            );
        }

        return savedOrder.getId();
    }

    public List<Order> getMyOrders(String username) {
        return orderRepository.findByUserAccountUsernameOrderByOrderDateDescIdDesc(username);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDescIdDesc();
    }

    public boolean updateOrderStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        String normalizedStatus = normalizeOrderStatus(newStatus);
        if (normalizedStatus == null) {
            return false;
        }

        order.setPaymentStatus(normalizedStatus);
        order.setOrderStatus(normalizedStatus);
        orderRepository.save(order);
        return true;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        String method = paymentMethod.trim().toUpperCase();
        if ("COD".equals(method) || "BANKING".equals(method) || "EWALLET".equals(method)) {
            return method;
        }
        return "COD";
    }

    private String normalizeOrderStatus(String orderStatus) {
        if (isBlank(orderStatus)) {
            return null;
        }

        String status = orderStatus.trim().toUpperCase();
        List<String> allowedStatuses = Arrays.asList(
                "PENDING_CONFIRMATION",
                "CONFIRMED",
                "SHIPPING",
                "DELIVERED",
                "CANCELLED"
        );

        if (allowedStatuses.contains(status)) {
            return status;
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void ensureLegacyCustomerOrderSchemaCompatible() {
        ensureColumnNullableIfExists("product_id");
        ensureColumnNullableIfExists("student_id");
        ensureColumnNullableIfExists("course_id");
        }

        private void ensureColumnNullableIfExists(String columnName) {
        Integer columnCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'customer_order' " +
                "AND COLUMN_NAME = ?",
            Integer.class,
            columnName
        );

        if (columnCount == null || columnCount == 0) {
            return;
        }

        String nullable = jdbcTemplate.queryForObject(
            "SELECT IS_NULLABLE FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'customer_order' " +
                "AND COLUMN_NAME = ?",
            String.class,
            columnName
        );

        if (!"YES".equalsIgnoreCase(nullable)) {
            LOGGER.warn("Detected legacy schema: customer_order.{} is NOT NULL. Auto-adjusting to NULLABLE.", columnName);
            jdbcTemplate.execute("ALTER TABLE customer_order MODIFY COLUMN " + columnName + " INT NULL");
        }
    }
}
