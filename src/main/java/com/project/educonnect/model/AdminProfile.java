package com.project.educonnect.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Data
@NoArgsConstructor
@Document(collection = "admin_profile")
public class AdminProfile {

    @Id
    private String id;
    private String userId; // Reference to the User document
    private String fullname;
    private String location;
    private String phone;
}
