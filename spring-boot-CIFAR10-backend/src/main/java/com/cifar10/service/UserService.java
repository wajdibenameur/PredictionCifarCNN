package com.cifar10.service;

import com.cifar10.exception.UserServiceException;
import com.cifar10.model.ERole;
import com.cifar10.model.Role;
import com.cifar10.model.User;
import com.cifar10.payload.request.RegisterRequest;
import com.cifar10.repository.RoleRepository;
import com.cifar10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    public User createUser(RegisterRequest registerRequest) {
        // Créer un nouvel utilisateur
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                encoder.encode(registerRequest.getPassword())
        );

        // Auto-activation immédiate
        user.setActive(true);

        // Définir les rôles
        Set<Role> roles = new HashSet<>();

        // Par défaut, ROLE_USER
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new UserServiceException("Error: Role USER not found."));
        roles.add(userRole);

        // Si des rôles sont spécifiés dans la requête
        if (registerRequest.getRoles() != null) {
            for (String roleName : registerRequest.getRoles()) {
                switch (roleName.toLowerCase()) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new UserServiceException("Error: Role ADMIN not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        // Par défaut, ROLE_USER déjà ajouté
                        break;
                }
            }
        }

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Long getUserId(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    public String getUserEmail(String username) {
        return userRepository.findByUsername(username)
                .map(User::getEmail)
                .orElse(null);
    }

    public Set<String> getUserRoles(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(java.util.stream.Collectors.toSet()))
                .orElse(new HashSet<>());
    }
}
