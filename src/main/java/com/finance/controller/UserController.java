package com.finance.controller;

import com.finance.entity.User;
import com.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok("User activated");
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok("User deactivated");
    }
}