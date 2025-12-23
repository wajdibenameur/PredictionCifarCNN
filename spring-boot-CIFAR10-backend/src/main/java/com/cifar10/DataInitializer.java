package com.cifar10;

import com.cifar10.model.ERole;
import com.cifar10.model.Role;
import com.cifar10.model.User;
import com.cifar10.repository.RoleRepository;
import com.cifar10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder encoder;

    /* ========= ADMIN ========= */
    @Value("${APP_ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${APP_ADMIN_EMAIL:admin@cifar10.com}")
    private String adminEmail;

    @Value("${APP_ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    /* ========= USER ========= */
    @Value("${APP_USER_USERNAME:user}")
    private String userUsername;

    @Value("${APP_USER_EMAIL:user@cifar10.com}")
    private String userEmail;

    @Value("${APP_USER_PASSWORD:user123}")
    private String userPassword;

    @Override
    public void run(String... args) {

        /* ===== Création des rôles (idempotent) ===== */
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));

        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));

        /* ===== Admin ===== */
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User(
                    adminUsername,
                    adminEmail,
                    encoder.encode(adminPassword)
            );
            admin.setRoles(Set.of(adminRole));
            userRepository.save(admin);
        }

        /* ===== User ===== */
        if (!userRepository.existsByUsername(userUsername)) {
            User user = new User(
                    userUsername,
                    userEmail,
                    encoder.encode(userPassword)
            );
            user.setRoles(Set.of(userRole));
            userRepository.save(user);
        }

        System.out.println("✔ Default users initialized");
    }
}
