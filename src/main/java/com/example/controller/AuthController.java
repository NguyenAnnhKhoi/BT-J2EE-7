package com.example.controller;

import com.example.dto.UserRegistrationRequest;
import com.example.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new UserRegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registerForm") UserRegistrationRequest request,
            BindingResult result) {
        if (!result.hasFieldErrors("username") && userAccountService.usernameExists(request.getUsername())) {
            result.rejectValue("username", "duplicate", "Username da ton tai");
        }

        if (!result.hasFieldErrors("email") && userAccountService.emailExists(request.getEmail())) {
            result.rejectValue("email", "duplicate", "Email da ton tai");
        }

        if (result.hasErrors()) {
            return "register";
        }

        userAccountService.registerUser(request);
        return "redirect:/login?registered";
    }
}
