package com.project.educonnect.config;

import com.project.educonnect.model.User;
import com.project.educonnect.model.AdminProfile;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.repository.AdminProfileRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    CommandLineRunner initAdmin(
            UserRepository userRepository,
            AdminProfileRepository adminProfileRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            // ✅ STEP 1: check if admin already exists
            if (userRepository.findByEmail("admin@educonnect.com").isEmpty()) {

                // =========================
                // 1. CREATE USER (LOGIN DATA)
                // =========================
                User admin = new User();
                admin.setFullName("System Admin");
                admin.setEmail("admin@educonnect.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");

                User savedAdmin = userRepository.save(admin);

                // =========================
                // 2. CREATE ADMIN PROFILE
                // =========================
                AdminProfile profile = new AdminProfile();
                profile.setUserId(savedAdmin.getUserId()); // IMPORTANT
                profile.setFullname("System Admin");
                profile.setLocation("HQ Office");
                profile.setPhone("9653265945");

                adminProfileRepository.save(profile);
            }
        };
    }
}