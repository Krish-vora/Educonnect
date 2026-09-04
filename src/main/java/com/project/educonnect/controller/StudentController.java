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
import org.springframework.web.client.RestTemplate;

import com.project.educonnect.dto.LeetCodeStats;
import com.project.educonnect.model.Application;
import com.project.educonnect.model.CodingProfile;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.ApplicationService;
import com.project.educonnect.service.LeetCodeService;
import com.project.educonnect.service.OrganizationService;
import com.project.educonnect.service.PostService;
import com.project.educonnect.service.StudentService;

@Controller
public class StudentController {

        @Autowired
        private StudentService studentService;

        @Autowired
        private ApplicationService applicationService;

        @Autowired
        private UserRepository userRepo;

        @Autowired
        private StudentProfileRepository studentProfileRepo;

        @Autowired
        private PostService postService;

        @Autowired
        private OrganizationService organizationService;

        @Autowired
        private LeetCodeService leetCodeService;

        @PostMapping("/student/profile/update")
        public String updateProfile(Authentication authentication,
                        @RequestParam String fullName,
                        @RequestParam String location,
                        @RequestParam String education,
                        @RequestParam String university,
                        @RequestParam String bio,
                        @RequestParam String skills,
                        @RequestParam String interests) {

                String email = authentication.getName();
                User user = studentService.getUserByEmail(email);
                StudentProfile student = studentService.getStudentByEmail(email);

                // Update User name
                user.setFullName(fullName);
                userRepo.save(user);

                // Update Student Profile
                student.setLocation(location);
                student.setStudentEducation(education);
                student.setStudentUniversity(university);
                student.setBio(bio);
                student.setStudentSkills(skills);
                student.setInterests(interests);
                studentProfileRepo.save(student);

                return "redirect:/student/home";
        }

        @GetMapping("/student/applications")
        @ResponseBody
        public List<Map<String, Object>> getStudentApplications(Authentication authentication) {

                String email = authentication.getName();
                User user = studentService.getUserByEmail(email);

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

        @PostMapping("/student/save-coding-profiles")
        public String saveCodingProfile(
                        Authentication authentication,
                        @RequestParam String leetcodeUsername,
                        @RequestParam String githubProfile,
                        @RequestParam String codeforcesHandle) {

                String email = authentication.getName();

                StudentProfile student = studentService.getStudentByEmail(email);

                CodingProfile codingProfile = new CodingProfile();

                codingProfile.setLeetCodeUsername(
                                leetcodeUsername);

                codingProfile.setGithubProfile(
                                githubProfile);

                codingProfile.setCodeforcesHandle(
                                codeforcesHandle);

                student.setCodingProfile(codingProfile);

                studentProfileRepo.save(student);

                return "redirect:/student/home";
        }

        @GetMapping("/student/leetcode-stats")
        @ResponseBody
        public LeetCodeStats getLeetCodeStats(
                        @RequestParam String username) {

                return leetCodeService.getStats(username);
        }

        @GetMapping("/api/github/followers")
        public ResponseEntity<Map<String, Object>> getGithubFollowers(
                        @RequestParam String username) {

                Map<String, Object> response = new HashMap<>();

                try {

                        String url = "https://api.github.com/users/" + username;

                        RestTemplate restTemplate = new RestTemplate();

                        Map githubData = restTemplate.getForObject(url, Map.class);

                        response.put("valid", true);

                        response.put(
                                        "followers",
                                        githubData.get("followers"));

                        return ResponseEntity.ok(response);

                } catch (Exception e) {

                        response.put("valid", false);

                        response.put(
                                        "message",
                                        "GitHub username not found");

                        return ResponseEntity.ok(response);
                }
        }

        @GetMapping("/student/post/{id}")
        @ResponseBody
        public OpportunityPost getPostById(
                        @PathVariable String id) {
                System.out.println("datacomeinng");
                return postService.getPostById(id);
        }

        @DeleteMapping("/student/application/cancel/{id}")
        @ResponseBody
        public ResponseEntity<String> cancelApplication(
                        @PathVariable String id) {

                applicationService.cancelApplication(id);

                return ResponseEntity.ok("Application cancelled");
        }

        // ── add these imports if not present ──

        @GetMapping("/student/skill-gap")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> getSkillGap(Authentication authentication) {

                String email = authentication.getName();
                User user = studentService.getUserByEmail(email);
                StudentProfile student = studentProfileRepo.findByUserId(user.getUserId());

          
                Set<String> mySkills = new HashSet<>();
                if (student.getStudentSkills() != null && !student.getStudentSkills().isBlank()) {
                        Arrays.stream(student.getStudentSkills().split(","))
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
                        item.put("demandCount", posts.size()); // how many applied posts need it
                        item.put("seenIn", new ArrayList<>(posts));

                        if (mySkills.contains(skill)) {
                                strengths.add(item);
                        } else {
                                gaps.add(item);
                        }
                }

    
                Comparator<Map<String, Object>> byDemand = Comparator
                                .comparingInt(m -> -((Integer) m.get("demandCount")));
                gaps.sort(byDemand);
                strengths.sort(byDemand);

                // 6. Summary stats
                int totalRequired = skillToPostTitles.size();
                int covered = strengths.size();
                int gapCount = gaps.size();
                int matchPct = totalRequired == 0 ? 0 : (int) Math.round(covered * 100.0 / totalRequired);

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

        // helper
        private String capitalize(String s) {
                if (s == null || s.isEmpty())
                        return s;
                return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
}