package com.project.educonnect.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Data
@NoArgsConstructor
@Document(collection = "student_profiles")
public class StudentProfile {
    @Id
    private String id;

    private String userId; // Reference to the User document
    private String interests;
    private String studentSkills;
    private String bio;
    private String location;
    private String studentEducation;
    private String studentUniversity;
    private CodingProfile codingProfile = new CodingProfile();
}
