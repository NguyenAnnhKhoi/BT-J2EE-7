package com.example.controller;

import com.example.model.Order;
import com.example.service.OrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/orders/my-orders")
    public String myOrders(Authentication authentication,
            @RequestParam(required = false) Integer orderId,
            @RequestParam(required = false, defaultValue = "false") boolean fromSuccess,
            Model model) {
        List<Order> orders = orderService.getMyOrders(authentication.getName());
        model.addAttribute("orders", orders);
        model.addAttribute("selectedOrderId", orderId);
        model.addAttribute("fromSuccess", fromSuccess);
        return "order/my-orders";
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/orders/success")
    public String orderSuccess(@RequestParam(required = false) Integer orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order/success";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/orders")
    public String adminOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "order/admin-list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Integer orderId,
            @RequestParam String orderStatus) {
        boolean updated = orderService.updateOrderStatus(orderId, orderStatus);
        if (updated) {
            return "redirect:/admin/orders?updated";
        }
        return "redirect:/admin/orders?updateError";
    }
}
