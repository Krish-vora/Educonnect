package com.project.educonnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your.email@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("EduConnect - Password Reset OTP");
        message.setText("Hello,\n\n" +
                "You have requested to reset your password.\n" +
                "Your 6-digit OTP code is: " + otp + "\n\n" +
                "This code will expire in 10 minutes.\n\n" +
                "If you did not request this, please ignore this email.");

        mailSender.send(message);
    }
}