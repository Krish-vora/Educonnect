package com.project.educonnect.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.educonnect.dto.RankedApplicantDTO;
import com.project.educonnect.dto.StudentApplicantResponse;
import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.ApplicationService;

@RestController
public class OrganizationApiController {

        @Autowired
        private ApplicationService applicationService;

        @Autowired
        private UserRepository userRepo;

        @Autowired
        private StudentProfileRepository studentProfileRepo;

        @Autowired
        private ProfessionalProfileRepository professionalProfileRepository;

        @Autowired
        private PostRepository postRepository;

        @GetMapping("/organization/post/applicants")
        public List<StudentApplicantResponse> getApplicants(@RequestParam String postId) {

                List<Application> applications = applicationService.getApplicationsByPostId(postId);
                List<StudentApplicantResponse> response = new ArrayList<>();

                for (Application app : applications) {

                        // FETCH USER
                        Optional<User> optionalUser = userRepo.findByUserId(app.getStudentId());

                        // CHECK USER EXISTS
                        if (optionalUser.isEmpty()) {
                                continue;
                        }

                        // GET REAL USER OBJECT
                        User user = optionalUser.get();

                        // DTO OBJECT
                        StudentApplicantResponse dto = new StudentApplicantResponse();

                        // COMMON USER DATA
                        dto.setFullName(user.getFullName());
                        dto.setEmail(user.getEmail());
                        dto.setApplicationId(app.getId());
                        dto.setStatus(app.getStatus());

                        // ==========================================
                        // FETCH BOTH PROFILES
                        // ==========================================
                        StudentProfile student = studentProfileRepo.findByUserId(app.getStudentId());

                        // Assuming you have autowired professionalProfileRepo in this class
                        ProfessionalProfile professional = professionalProfileRepository
                                        .findByUserId(app.getStudentId());

                        // ==========================================
                        // MAP DATA BASED ON PROFILE TYPE
                        // ==========================================
                        if (student != null) {
                                dto.setApplicantType("Student");
                                dto.setLocation(student.getLocation());
                                dto.setUniversity(student.getStudentUniversity());
                                dto.setEducation(student.getStudentEducation());
                                dto.setSkills(student.getStudentSkills());
                                dto.setBio(student.getBio());
                        } else if (professional != null) {
                                dto.setApplicantType("Professional");
                                dto.setLocation(professional.getLocation());
                                dto.setEducation(professional.getEducation());
                                dto.setSkills(professional.getSkills());
                                dto.setBio(professional.getBio());

                                // Professional-specific fields
                                dto.setCompany(professional.getCompany());
                                dto.setExperience(professional.getExperience());
                        }

                        response.add(dto);
                }

                return response;
        }

        @GetMapping("/organization/ai-ranking")
        public List<RankedApplicantDTO> getAIRanking(@RequestParam String postId) {

                // 1. Fetch the post to get required skills + location
                OpportunityPost post = postRepository.findById(postId).orElse(null);
                List<String> requiredSkills = new ArrayList<>();
                String postLocation = "";

                if (post != null) {
                        if (post.getSkillsRequired() != null) {
                                requiredSkills = post.getSkillsRequired().stream()
                                                .map(String::trim).map(String::toLowerCase)
                                                .collect(Collectors.toList());
                        }
                        if (post.getLocation() != null)
                                postLocation = post.getLocation().trim().toLowerCase();
                }

                List<Application> applications = applicationService.getApplicationsByPostId(postId);

                List<RankedApplicantDTO> result = new ArrayList<>();

                for (Application app : applications) {

                        Optional<User> optUser = userRepo.findByUserId(app.getStudentId());
                        if (optUser.isEmpty())
                                continue;
                        User user = optUser.get();

                        StudentProfile student = studentProfileRepo.findByUserId(app.getStudentId());

                        RankedApplicantDTO dto = new RankedApplicantDTO();
                        dto.setApplicationId(app.getId());
                        dto.setStudentId(app.getStudentId());
                        dto.setFullName(user.getFullName());
                        dto.setEmail(user.getEmail());
                        dto.setStatus(app.getStatus());

                        if (student != null) {
                                dto.setLocation(student.getLocation());
                                dto.setUniversity(student.getStudentUniversity());
                                dto.setEducation(student.getStudentEducation());
                                dto.setSkills(student.getStudentSkills());
                                dto.setBio(student.getBio());
                        }

                

                        // Skill score (60 pts)
                        List<String> studentSkillList = splitSkills(dto.getSkills());
                        List<String> matched = new ArrayList<>();
                        List<String> missing = new ArrayList<>();

                        for (String req : requiredSkills) {
                                if (studentSkillList.contains(req))
                                        matched.add(capitalize(req));
                                else
                                        missing.add(capitalize(req));
                        }

                        double skillScore = requiredSkills.isEmpty() ? 60.0
                                        : (matched.size() * 60.0) / requiredSkills.size();

                        // Location score (20 pts)
                        double locationScore = 0.0;
                        String mode = post != null ? (post.getMode() != null ? post.getMode().toLowerCase() : "") : "";
                        if (mode.contains("remote") || mode.contains("online")) {
                                locationScore = 20.0;
                        } else if (dto.getLocation() != null) {
                                String sl = dto.getLocation().trim().toLowerCase();
                                if (sl.equals(postLocation))
                                        locationScore = 20.0;
                                else if (sl.contains(postLocation) || postLocation.contains(sl))
                                        locationScore = 12.0;
                        }

                      
                        double profileScore = 0.0;
                        if (dto.getBio() != null && !dto.getBio().isBlank())
                                profileScore += 7.0;
                        if (dto.getUniversity() != null && !dto.getUniversity().isBlank())
                                profileScore += 7.0;
                        if (dto.getEducation() != null && !dto.getEducation().isBlank())
                                profileScore += 6.0;

                        int totalScore = (int) Math.round(skillScore + locationScore + profileScore);
                        totalScore = Math.min(100, totalScore);

                        dto.setAiScore(totalScore);
                        dto.setMatchedSkills(matched);
                        dto.setMissingSkills(missing);
                        dto.setScoreReason(getReason(totalScore, matched.size(), requiredSkills.size()));

                        result.add(dto);
                }

                // 3. Sort by score descending, assign rank
                result.sort(Comparator.comparingInt(RankedApplicantDTO::getAiScore).reversed());
                for (int i = 0; i < result.size(); i++)
                        result.get(i).setRank(i + 1);

                return result;
        }


        private List<String> splitSkills(String raw) {
                if (raw == null || raw.isBlank())
                        return new ArrayList<>();
                return Arrays.stream(raw.split(","))
                                .map(String::trim).map(String::toLowerCase)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
        }

        private String capitalize(String s) {
                if (s == null || s.isEmpty())
                        return s;
                return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }

        private String getReason(int score, int matched, int total) {
                if (score >= 85)
                        return "Excellent overall fit";
                if (score >= 70)
                        return "Strong skill match";
                if (matched == total && total > 0)
                        return "All skills matched";
                if (score >= 50)
                        return "Partial skill match";
                if (score >= 30)
                        return "Some potential";
                return "Low match";
        }

        @PostMapping("/organization/application/{applicationId}/status")
        public ResponseEntity<String> updateApplicationStatus(
                        @PathVariable String applicationId,
                        @RequestBody Map<String, String> body) {

                String status = body.get("status");
                applicationService.updateStatus(applicationId, status);
                return ResponseEntity.ok("Status updated");
        }
}