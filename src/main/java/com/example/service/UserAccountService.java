package com.example.service;

import com.example.dto.UserRegistrationRequest;
import com.example.model.Role;
import com.example.model.UserAccount;
import com.example.repository.RoleRepository;
import com.example.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository userAccountRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userAccountRepository.existsByEmail(email);
    }

    public void registerUser(UserRegistrationRequest request) {
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            return roleRepository.save(role);
        });

        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(request.getUsername().trim());
        userAccount.setEmail(request.getEmail().trim().toLowerCase());
        userAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        userAccount.setRoles(Set.of(userRole));
        userAccountRepository.save(userAccount);
    }
}
