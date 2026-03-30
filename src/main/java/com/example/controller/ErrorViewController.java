package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorViewController {

    @GetMapping("/access-denied")
    public String accessDeniedPage() {
        return "error/403";
    }
}
