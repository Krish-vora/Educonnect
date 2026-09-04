package com.project.educonnect.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String userId;

    @Indexed(unique = true)
    private String email;

    private String fullName;
    private String password;
    private String role;

    private String resetOtp;
    private LocalDateTime otpExpiryTime;
    private boolean isOtpVerified;
}
