package com.josephken.roors.common.config;

import com.josephken.roors.auth.entity.UserRole;
import com.josephken.roors.common.util.LogCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.auth.entity.User;

@Slf4j
@Configuration
public class DatabaseSeeder {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.MANAGER);
                admin.setVerified(true);

                userRepository.save(admin);
                log.info(LogCategory.system("Admin user created with username: {}"), adminUsername);
            } else {
                log.info(LogCategory.system("Admin user already exists with username: {}"), adminUsername);
            }
        };
    }
}