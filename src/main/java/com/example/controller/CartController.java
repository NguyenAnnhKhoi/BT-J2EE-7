package com.example.controller;

import com.example.dto.CartItemView;
import com.example.dto.CheckoutRequest;
import com.example.model.Product;
import com.example.service.CartService;
import com.example.service.OrderService;
import com.example.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class CartController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final ProductService productService;
    private final OrderService orderService;

    public CartController(CartService cartService,
            ProductService productService,
            OrderService orderService) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Integer productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        cartService.addToCart(session, productId, quantity);
        return "redirect:/home?addedToCart";
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        Map<Integer, Integer> cart = cartService.getCart(session);
        List<CartItemView> items = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            if (product == null) {
                continue;
            }

            int quantity = entry.getValue();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            cartTotal = cartTotal.add(lineTotal);
            items.add(new CartItemView(product, quantity, lineTotal));
        }

        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", cartTotal);
        return "cart/index";
    }

    @PostMapping("/cart/update/{productId}")
    public String updateCartItem(@PathVariable Integer productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        cartService.updateQuantity(session, productId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/cart/remove/{productId}")
    public String removeCartItem(@PathVariable Integer productId, HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        List<CartItemView> cartItems = buildCartItems(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart?checkoutError";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", sumTotal(cartItems));
        if (!model.containsAttribute("checkoutForm")) {
            CheckoutRequest checkoutRequest = new CheckoutRequest();
            checkoutRequest.setPaymentMethod("COD");
            model.addAttribute("checkoutForm", checkoutRequest);
        }
        return "checkout/index";
    }

    @GetMapping("/cart/checkout")
    public String checkoutPageAlias(HttpSession session, Model model) {
        return checkoutPage(session, model);
    }

    @PostMapping("/cart/checkout")
    public String checkout(Authentication authentication,
            @Valid CheckoutRequest checkoutForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        List<CartItemView> cartItems = buildCartItems(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart?checkoutError";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", sumTotal(cartItems));
            model.addAttribute("checkoutForm", checkoutForm);
            return "checkout/index";
        }

        Map<Integer, Integer> cart = cartService.getCart(session);
        try {
            Integer createdOrderId = orderService.placeOrderFromCart(authentication.getName(), cart, checkoutForm);
            if (createdOrderId != null) {
                cartService.clear(session);
                return "redirect:/orders/success?orderId=" + createdOrderId;
            }
        } catch (Exception ex) {
            LOGGER.error("Checkout failed due to internal error", ex);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", sumTotal(cartItems));
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("checkoutError", true);
        return "checkout/index";
    }

    @PostMapping("/checkout")
    public String checkoutAlias(Authentication authentication,
            @Valid CheckoutRequest checkoutForm,
            BindingResult bindingResult,
            HttpSession session,
            Model model) {
        return checkout(authentication, checkoutForm, bindingResult, session, model);
    }

    private List<CartItemView> buildCartItems(HttpSession session) {
        Map<Integer, Integer> cart = cartService.getCart(session);
        List<CartItemView> items = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            if (product == null) {
                continue;
            }

            int quantity = entry.getValue();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartItemView(product, quantity, lineTotal));
        }
        return items;
    }

    private BigDecimal sumTotal(List<CartItemView> items) {
        return items.stream()
                .map(CartItemView::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
