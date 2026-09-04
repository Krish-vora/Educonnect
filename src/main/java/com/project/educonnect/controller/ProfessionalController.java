package com.project.educonnect.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.ApplicationService;
import com.project.educonnect.service.OrganizationService;
import com.project.educonnect.service.PostService;
import com.project.educonnect.service.ProfessionalService;

@Controller
public class ProfessionalController {

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    @Autowired
    private ProfessionalService professionalService;
    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepo;
    @Autowired
    private OrganizationService organizationService;

    ProfessionalController(ProfessionalProfileRepository professionalProfileRepository) {
        this.professionalProfileRepository = professionalProfileRepository;
    }

    @PostMapping("/professional/apply/{postId}")
    @ResponseBody
    public Map<String, Object> applyPost(
            @PathVariable String postId,
            Authentication authentication) {
        System.out.println("this iscalled");
        String email = authentication.getName();

        User user = professionalService.getUserByEmail(email);

        boolean applied = applicationService.apply(user.getUserId(), postId);

        return Map.of(
                "success", applied);
    }

    @PostMapping("/professional/profile/update")
    public String updateProfile(Authentication authentication,
            @RequestParam String fullName,
            @RequestParam String location,
            @RequestParam String education,
            @RequestParam String experience,
            @RequestParam String company,
            @RequestParam String bio,
            @RequestParam String skills) {

        String email = authentication.getName();
        User user = professionalService.getUserByEmail(email);
        ProfessionalProfile professional = professionalService.getprofessionalByEmail(email);

        user.setFullName(fullName);
        userRepo.save(user);

        professional.setLocation(location);
        professional.setEducation(education);
        professional.setCompany(company);
        professional.setBio(bio);
        professional.setExperience(experience);
        professional.setSkills(skills);
        professionalProfileRepository.save(professional);

        return "redirect:/professional/home";
    }

    @GetMapping("/professional/post/{id}")
    @ResponseBody
    public OpportunityPost getPostById(
            @PathVariable String id) {
        System.out.println("datacomeinng");
        return postService.getPostById(id);
    }

    @GetMapping("/professional/applications")
    @ResponseBody
    public List<Map<String, Object>> getStudentApplications(Authentication authentication) {

        String email = authentication.getName();
        User user = professionalService.getUserByEmail(email);

        List<Application> applications = applicationService.getStudentApplications(user.getUserId());

        List<Map<String, Object>> response = new ArrayList<>();

        for (Application app : applications) {

            OpportunityPost post = postService.getPostById(app.getPostId());

            if (post == null)
                continue;

            OrganizationProfile org = organizationService.getOrgById(post.getOrganizationId());
            String orgName = (org != null) ? org.getOrganizationName() : "Unknown Organization";

            Map<String, Object> map = new HashMap<>();

            map.put("applicationId", app.getId());
            map.put("status", app.getStatus());
            map.put("appliedAt", app.getAppliedAt());
            map.put("title", post.getTitle());
            map.put("type", post.getType());
            map.put("location", post.getLocation());
            map.put("mode", post.getMode());
            map.put("company", orgName); // ✅ now correctly set

            response.add(map);
        }

        System.out.println("Applications for user " + email + ": " + response);
        return response;
    }

    @GetMapping("/professional/skill-gap")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSkillGap(Authentication authentication) {

        String email = authentication.getName();
        User user = professionalService.getUserByEmail(email);
        ProfessionalProfile profile = professionalProfileRepository.findByUserId(user.getUserId());

        // 1. Professional's current skills (comma-separated String → Set)
        Set<String> mySkills = new HashSet<>();
        if (profile != null && profile.getSkills() != null && !profile.getSkills().isBlank()) {
            Arrays.stream(profile.getSkills().split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .forEach(mySkills::add);
        }

        List<Application> applications = applicationService.getStudentApplications(user.getUserId());

        Map<String, Set<String>> skillToPostTitles = new LinkedHashMap<>();

        for (Application app : applications) {
            OpportunityPost post = postService.getPostById(app.getPostId());
            if (post == null || post.getSkillsRequired() == null)
                continue;

            for (String skill : post.getSkillsRequired()) {
                if (skill == null || skill.isBlank())
                    continue;
                String key = skill.trim().toLowerCase();
                skillToPostTitles
                        .computeIfAbsent(key, k -> new LinkedHashSet<>())
                        .add(post.getTitle());
            }
        }

        List<Map<String, Object>> gaps = new ArrayList<>();
        List<Map<String, Object>> strengths = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : skillToPostTitles.entrySet()) {
            String skill = entry.getKey();
            Set<String> posts = entry.getValue();

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("skill", capitalize(skill));
            item.put("demandCount", posts.size());
            item.put("seenIn", new ArrayList<>(posts));

            if (mySkills.contains(skill))
                strengths.add(item);
            else
                gaps.add(item);
        }

        // 5. Sort by demand (most demanded first)
        Comparator<Map<String, Object>> byDemand = Comparator.comparingInt(m -> -((Integer) m.get("demandCount")));
        gaps.sort(byDemand);
        strengths.sort(byDemand);

        // 6. Summary stats
        int totalRequired = skillToPostTitles.size();
        int covered = strengths.size();
        int gapCount = gaps.size();
        int matchPct = totalRequired == 0 ? 0
                : (int) Math.round(covered * 100.0 / totalRequired);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalApplied", applications.size());
        result.put("totalRequired", totalRequired);
        result.put("covered", covered);
        result.put("gapCount", gapCount);
        result.put("matchPercent", matchPct);
        result.put("gaps", gaps);
        result.put("strengths", strengths);

        return ResponseEntity.ok(result);
    }

    // ── /professional/application/cancel/{id} ────────────────────────
    @DeleteMapping("/professional/application/cancel/{id}")
    @ResponseBody
    public ResponseEntity<String> cancelApplication(@PathVariable String id) {
        applicationService.cancelApplication(id);
        return ResponseEntity.ok("cancelled");
    }

    // ── helper ────────────────────────────────────────────────────────
    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
