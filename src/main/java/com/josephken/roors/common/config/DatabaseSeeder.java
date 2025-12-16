package com.josephken.roors.common.config;

import com.josephken.roors.auth.entity.User;
import com.josephken.roors.auth.entity.UserRole;
import com.josephken.roors.auth.repository.UserRepository;
import com.josephken.roors.common.util.LogCategory;
import com.josephken.roors.reservation.repository.DiningTableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DatabaseSeeder {

    @Value("${ADMIN_EMAIL:josephken020605@gmail.com}")
    private String adminEmail;

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:password}")
    private String adminPassword;

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DiningTableRepository diningTableRepository,
            DataSource dataSource
    ) {
        return args -> {
            // ---------------------------------------------------------
            //            // 1. ADMIN USER SEEDING
            // ---------------------------------------------------------
            if (userRepository.existsByUsername(adminUsername)) {
                log.info(LogCategory.system("Admin user already exists with username: {}"), adminUsername);
            } else {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.MANAGER);
                admin.setVerified(true);

                userRepository.save(admin);
                log.info(LogCategory.system("Admin user created with username: {}"), adminUsername);
            }

            // ---------------------------------------------------------
            // 2. SQL DATA SEEDING (Only if tables are empty)
            // ---------------------------------------------------------
            long tableCount = diningTableRepository.count();

            if (tableCount == 0) {
                log.info("Dining tables count is 0. Initializing database with 'initialData.sql'...");

                try (Connection conn = dataSource.getConnection()) {
                    Resource resource = new ClassPathResource("data/initialData.sql");
                    ScriptUtils.executeSqlScript(conn, resource);

                    log.info("Successfully loaded initial data.");
                } catch (SQLException e) {
                    log.error("Failed to load 'initialData.sql'", e);
                }
            } else {
                log.info("Dining tables found (count: {}). Skipping initial data loading.", tableCount);
            }
        };
    }
}