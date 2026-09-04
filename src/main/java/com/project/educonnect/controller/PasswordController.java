package com.project.educonnect.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.educonnect.dto.PasswordChangeDto;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.UserRepository;

@RestController
@RequestMapping("/api/user")
public class PasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeDto dto,
            Principal principal) { // Gets the currently logged-in user
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
        }

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Incorrect current password"));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}