package com.cifar10.controller;

import com.cifar10.model.User;
import com.cifar10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "active", user.getActive(),
                        "roles", user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList()),
                        "createdAt", user.getCreatedAt(),
                        "updatedAt", user.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "active", user.getActive(),
                        "roles", user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList()),
                        "createdAt", user.getCreatedAt(),
                        "updatedAt", user.getUpdatedAt()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActive(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setActive(!user.getActive());
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of(
                            "message", "User " + (user.getActive() ? "activated" : "deactivated"),
                            "active", user.getActive()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(User::getActive)
                .count();

        return ResponseEntity.ok(Map.of(
                "total_users", totalUsers,
                "active_users", activeUsers,
                "inactive_users", totalUsers - activeUsers
        ));
    }
}