package com.example.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class CartService {

    public static final String CART_SESSION_KEY = "SHOPPING_CART";

    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getCart(HttpSession session) {
        Object cartObj = session.getAttribute(CART_SESSION_KEY);
        if (cartObj instanceof Map<?, ?>) {
            return (Map<Integer, Integer>) cartObj;
        }

        Map<Integer, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public void addToCart(HttpSession session, Integer productId, int quantity) {
        if (productId == null || quantity <= 0) {
            return;
        }

        Map<Integer, Integer> cart = getCart(session);
        int currentQty = cart.getOrDefault(productId, 0);
        cart.put(productId, currentQty + quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateQuantity(HttpSession session, Integer productId, int quantity) {
        Map<Integer, Integer> cart = getCart(session);

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeItem(HttpSession session, Integer productId) {
        Map<Integer, Integer> cart = getCart(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clear(HttpSession session) {
        session.setAttribute(CART_SESSION_KEY, new LinkedHashMap<Integer, Integer>());
    }
}
