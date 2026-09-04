package com.project.educonnect.dto;

import lombok.Data;

@Data
public class StudentApplicantResponse {
    private String applicationId;
    private String status;
    private String fullName;
    private String email;
    
    // Common Profile Fields
    private String location;
    private String education;
    private String skills;
    private String bio;
    
    // Frontend Helper
    private String applicantType; // "Student" or "Professional"

    // Student Specific
    private String university;

    // Professional Specific
    private String company;
    private String experience;
}