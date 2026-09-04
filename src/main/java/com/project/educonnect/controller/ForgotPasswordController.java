package com.project.educonnect.controller;

import com.project.educonnect.model.User;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User with this email does not exist."));
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Save OTP and expiration (10 minutes from now)
        user.setResetOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        user.setOtpVerified(false); // Reset verification flag
        userRepository.save(user);

        // Send Email
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
            return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send email. Check configuration."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
        }

        if (user.getResetOtp() == null || user.getOtpExpiryTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No OTP request found."));
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP has expired. Please request a new one."));
        }

        if (!user.getResetOtp().equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP."));
        }

        // Mark as verified
        user.setOtpVerified(true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "OTP verified successfully."));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
        }

        // Security check: Ensure they actually passed step 2
        if (!user.isOtpVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP not verified."));
        }

        // Update password securely
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Clear the OTP fields so they can't be reused
        user.setResetOtp(null);
        user.setOtpExpiryTime(null);
        user.setOtpVerified(false);
        
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}