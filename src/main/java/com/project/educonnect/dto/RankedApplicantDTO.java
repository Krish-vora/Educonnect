package com.project.educonnect.dto;

import java.util.List;

import lombok.Data;

@Data
public class RankedApplicantDTO {
    private String applicationId;
    private String studentId;
    private String fullName;
    private String email;
    private String location;
    private String university;
    private String education;
    private String skills;          // raw comma-separated string
    private String bio;
    private String status;

    // AI Ranking fields
    private int     aiScore;        // 0-100
    private int     rank;           // 1, 2, 3 ...
    private List<String> matchedSkills;   // skills student HAS that post needs
    private List<String> missingSkills;   // skills student LACKS that post needs
    private String  scoreReason;    // short label e.g. "Strong skill match"
}