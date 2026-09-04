package com.project.educonnect.dto;

import java.util.List;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    private String fullName;
    private String email;
    private String password;
    private String role;
    private String bio;
    // Student
    private String studentSkills;
    private String studentInterests;
    private String studentEducation;
    private String studentUniversity;
    // Organization
    private String organizationName;
    private String website;
    private String industry;
    private String totalEmployees;

    // Professional
    private String company;
    private String experience;
    private String education;
    private String professionalSkills;

    // Common
    private String location;
}