package com.project.educonnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // disable for testing APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/images/**", "/api/org/**",
                                "/css/**",
                                "/js/**",
                                "/static/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers("/organization/**").hasRole("ORGANIZATION") // 🔥 allow register/login
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/api/auth/register")
                        .loginProcessingUrl("/userLogin")
                        .successHandler((request, response, authentication) -> {

                            var authorities = authentication.getAuthorities();

                            String role = authorities.iterator().next().getAuthority();

                            if (role.equals("ROLE_STUDENT")) {
                                response.sendRedirect("/student/home");
                            } else if (role.equals("ROLE_ORGANIZATION")) {
                                response.sendRedirect("/organization/home");
                            } else if (role.equals("ROLE_PROFESSIONAL")) {
                                response.sendRedirect("/professional/home");
                            } else if (role.equals("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/home");
                            }

                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/api/auth/register?logout=true"));
        // ❌ disable default login page

        return http.build();
    }

}
