package com.project.educonnect.model;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "opportunity_posts")
public class OpportunityPost {
    @Id
    private String id;

    // ================= COMMON FIELDS (FOR ALL TYPES) =================
    private String organizationId;
    private String title;
    private String description;
    private String type; // JOB, INTERNSHIP, HACKATHON, EVENT, CONTEST
    private String mode;
    private String category; // Tech, Finance, Marketing
    private List<String> skillsRequired;
    private String location;
    // private boolean remote;

    private String applyLink;
    private LocalDate createdAt;

    private LocalDate deadline;
    private String status; // ACTIVE, CLOSED

    // ================= JOB SPECIFIC FIELDS =================
    private String jobRole;
    private String employmentType; // Full-time, Part-time
    private String experienceRequired;
    private String salaryRange;
    // private String workMode;
    private List<String> responsibilities;
    private List<String> jobBenefits;

    // ================= INTERNSHIP SPECIFIC FIELDS =================
    private String internshipDuration; // 3 months, 6 months
    private String stipend;
    // private String internshipMode; // Remote / Onsite
    private boolean ppoAvailable; // Pre-placement offer
    private String internshipStartDate;

    // ================= HACKATHON SPECIFIC FIELDS =================
    private LocalDate hackathonStartDate;
    private LocalDate hackathonEndDate;
    // private String hackathonMode; // Online / Offline
    private String teamSize;
    private String prizePool;
    private List<String> themes; // AI, Blockchain, etc.

    // ================= CODING CONTEST SPECIFIC FIELDS =================
    private LocalDate contestDate;
    private String contestPlatform; // Codeforces, Leetcode
    private String contestDuration;
    private String difficultyLevel; // Easy, Medium, Hard

    // ================= EVENT / WORKSHOP SPECIFIC FIELDS =================
    private LocalDate eventDate;
    // private String eventMode; // Online / Offline
    private String eventDuration;
    private String registrationFee;

    // ================= EXTRA (SMART FEATURES) =================
    private int totalApplicants;
    private int totalViews;
    private List<String> tags; // trending tags

    @org.springframework.data.annotation.Transient
    private double matchScore;
}
