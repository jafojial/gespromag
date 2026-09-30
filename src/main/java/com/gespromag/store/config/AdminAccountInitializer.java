package com.gespromag.store.config;

import com.gespromag.store.entity.Role;
import com.gespromag.store.entity.User;
import com.gespromag.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree le premier compte ADMINISTRATEUR au demarrage si la table users est vide,
 * a partir des variables d'environnement ADMIN_USERNAME / ADMIN_EMAIL / ADMIN_PASSWORD
 * (voir .env.example et README).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMINISTRATEUR);
        admin.setActive(true);
        userRepository.save(admin);

        log.info("Compte administrateur initial cree : {}", adminUsername);
    }
}
