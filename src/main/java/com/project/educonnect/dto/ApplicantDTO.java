// package com.project.educonnect.dto;


// import lombok.Data;

// @Data
// public class ApplicantDTO {
//     private String applicationId;
//     private String status;
//     private String appliedAt;

//     // Post info
//     private String postId;
//     private String postTitle;
//     private String postType;

//     // Student info
//     private String studentId;
//     private String studentName;
//     private String studentEmail;
//     private String location;
//     private String education;
//     private String university;
//     private String skills;
//     private String bio;
// }
package com.project.educonnect.dto;

import lombok.Data;

@Data
public class ApplicantDTO {
    private String applicationId;
    private String status;
    private String appliedAt;

    // Post info
    private String postId;
    private String postTitle;
    private String postType;

    // User info (Common)
    private String studentId; 
    private String studentName;
    private String studentEmail;
    private String location;
    private String education;
    private String skills;
    private String bio;
    
    // Profile Type Identifier (CRITICAL FOR FRONTEND)
    private String applicantType; // Will be "Student" or "Professional"

    // Student Specific
    private String university;

    // Professional Specific
    private String company;
    private String experience;
}