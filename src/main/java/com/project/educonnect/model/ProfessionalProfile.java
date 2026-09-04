package com.project.educonnect.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Data
@NoArgsConstructor
@Document(collection = "professional_profiles")
public class ProfessionalProfile {
    @Id
    private String id;

    private String userId; // Reference to the User document
    private String company;
    private String bio;
    private String location;
    private String experience;
    private String education;
    private String skills;

}
