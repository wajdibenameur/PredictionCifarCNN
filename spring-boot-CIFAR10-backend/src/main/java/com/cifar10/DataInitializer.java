package com.cifar10;

import com.cifar10.model.ERole;
import com.cifar10.model.Role;
import com.cifar10.model.User;
import com.cifar10.repository.RoleRepository;
import com.cifar10.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            // Création des rôles
            Role userRole = roleRepository.save(new Role(ERole.ROLE_USER));
            Role adminRole = roleRepository.save(new Role(ERole.ROLE_ADMIN));

            // Création de l'admin
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User("admin", "admin@cifar10.com", encoder.encode("admin123"));
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
            }

            // Création du user
            if (!userRepository.existsByUsername("user")) {
                User user = new User("user", "user@cifar10.com", encoder.encode("user123"));
                user.setRoles(Set.of(userRole));
                userRepository.save(user);
            }

            System.out.println("Users initialized: admin/admin123, user/user123");
        }
    }
}
