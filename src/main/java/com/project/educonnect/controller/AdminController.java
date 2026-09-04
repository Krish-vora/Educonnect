package com.project.educonnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.AnnouncementService;
import com.project.educonnect.service.ProfessionalService;
import com.project.educonnect.service.StudentService;

@Controller
public class AdminController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OrganizationProfileRepository organizationProfileRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private ProfessionalProfileRepository professionalProfileRepository;

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/admin/api/users")
    @ResponseBody
    public List<Map<String, Object>> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole()))
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getUserId());
                    map.put("name", u.getFullName());
                    map.put("email", u.getEmail());
                    map.put("role", u.getRole());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/api/posts")
    @ResponseBody
    public List<Map<String, Object>> getAllPosts() {

        return postRepository.findAll()
                .stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("title", p.getTitle());
                    map.put("type", p.getType());
                    map.put("location", p.getLocation());
                    map.put("orgId", p.getOrganizationId());
                    map.put("status", p.getStatus());
                    map.put("deadline", p.getDeadline());
                    map.put("totalApplicants", p.getTotalApplicants());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/api/applications")
    @ResponseBody
    public List<Map<String, Object>> getAllApplications() {

        return applicationRepository.findAll()
                .stream()
                .map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("studentId", a.getStudentId());
                    map.put("postId", a.getPostId());
                    map.put("status", a.getStatus());
                    map.put("appliedAt", a.getAppliedAt());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/api/students")
    @ResponseBody
    public List<Map<String, Object>> getAllStudents() {

        List<StudentProfile> students = studentService.getAll();

        return students.stream().map(s -> {

            Map<String, Object> map = new HashMap<>();

            map.put("userId", s.getUserId());
            map.put("university", s.getStudentUniversity());
            map.put("skills", s.getStudentSkills());
            map.put("location", s.getLocation());
            map.put("education", s.getStudentEducation());

            // join with user table for name + email
            userRepository.findByUserId(s.getUserId()).ifPresent(u -> {
                map.put("fullName", u.getFullName());
                map.put("email", u.getEmail());
            });

            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/api/organizations")
    @ResponseBody
    public List<Map<String, Object>> getAllOrganizations() {

        List<OrganizationProfile> orgs = organizationProfileRepository.findAll();

        return orgs.stream().map(o -> {

            Map<String, Object> map = new HashMap<>();

            map.put("userId", o.getUserId());
            map.put("organizationName", o.getOrganizationName());
            map.put("industry", o.getIndustry());
            map.put("location", o.getLocation());
            map.put("website", o.getWebsite());

            // fetch user details
            userRepository.findByUserId(o.getUserId()).ifPresent(u -> {
                map.put("email", u.getEmail());
                map.put("fullName", u.getFullName());
            });

            // optional: post count
            long postCount = postRepository.countByOrganizationId(o.getUserId());
            map.put("postCount", postCount);

            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/api/professionals")
    @ResponseBody
    public List<Map<String, Object>> getAllProfessionals() {

        List<ProfessionalProfile> profs = professionalService.getAll();

        return profs.stream().map(p -> {

            Map<String, Object> map = new HashMap<>();

            map.put("userId", p.getUserId());
            map.put("company", p.getCompany());
            map.put("experience", p.getExperience());
            map.put("skills", p.getSkills());
            map.put("location", p.getLocation());

            userRepository.findByUserId(p.getUserId()).ifPresent(u -> {
                map.put("fullName", u.getFullName());
                map.put("email", u.getEmail());
            });

            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/api/applications-detail")
    @ResponseBody
    public List<Map<String, Object>> getAllApplicationDetails() {

        List<Application> applications = applicationRepository.findAll();

        return applications.stream().map(app -> {

            Map<String, Object> map = new HashMap<>();

            map.put("applicationId", app.getId());
            map.put("status", app.getStatus());
            map.put("appliedAt", app.getAppliedAt());

            // POST INFO
            OpportunityPost post = postRepository.findById(app.getPostId()).orElse(null);
            if (post != null) {
                map.put("postId", post.getId());
                map.put("title", post.getTitle());
                map.put("type", post.getType());
                map.put("location", post.getLocation());

                // organization name
                OrganizationProfile org = organizationProfileRepository.findByUserId(post.getOrganizationId());
                map.put("organization", org != null ? org.getOrganizationName() : "Unknown");
            }

            // STUDENT INFO
            User user = userRepository.findByUserId(app.getStudentId()).orElse(null);
            if (user != null) {
                map.put("studentName", user.getFullName());
                map.put("studentEmail", user.getEmail());
            }

            StudentProfile student = studentService.getStudentByUserId(app.getStudentId());
            if (student != null) {
                map.put("skills", student.getStudentSkills());
                map.put("education", student.getStudentEducation());
                map.put("university", student.getStudentUniversity());
            }

            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/admin/api/user/{userId}")
    @ResponseBody
    public Map<String, Object> getUserDetail(@PathVariable String userId) {

        Map<String, Object> response = new HashMap<>();

        // 1. USER BASIC INFO
        User user = userRepository.findByUserId(userId).orElse(null);
        response.put("user", user);

        if (user == null) {
            response.put("profile", null);
            return response;
        }

        String role = user.getRole();

        // 2. ROLE-BASED PROFILE FETCHING
        switch (role) {

            case "STUDENT":
                StudentProfile student = studentProfileRepository.findByUserId(userId);
                response.put("profile", student);
                break;

            case "ORGANIZATION":
                OrganizationProfile org = organizationProfileRepository.findByUserId(userId);
                response.put("profile", org);
                break;

            case "PROFESSIONAL":
                ProfessionalProfile prof = professionalProfileRepository.findByUserId(userId);
                response.put("profile", prof);
                break;

            default:
                response.put("profile", null);
        }

        return response;
    }

    @GetMapping("/admin/api/post/{postId}")
    @ResponseBody
    public Map<String, Object> getPostDetail(@PathVariable String postId) {

        OpportunityPost post = postRepository.findById(postId).orElse(null);

        if (post == null)
            return Map.of();

        Map<String, Object> map = new HashMap<>();

        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("type", post.getType());
        map.put("status", post.getStatus());
        map.put("location", post.getLocation());
        map.put("deadline", post.getDeadline());
        map.put("description", post.getDescription());
        map.put("mode", post.getMode());
        map.put("totalApplicants", post.getTotalApplicants());
        map.put("skillsRequired", post.getSkillsRequired());

        long totalApplicants = applicationRepository.countByPostId(postId);
        map.put("totalApplicants", totalApplicants);

        return map;
    }

    @PostMapping("/admin/api/announcement")
    @ResponseBody
    public ResponseEntity<String> sendAnnouncement(
            @RequestBody Map<String, String> body) {

        String target = body.getOrDefault("target", "ALL");
        String title = body.getOrDefault("title", "");
        String msg = body.getOrDefault("body", "");

        if (title.isBlank() || msg.isBlank())
            return ResponseEntity.badRequest().body("Title and message required");

        announcementService.create(title, msg, target);
        return ResponseEntity.ok("Announcement sent");
    }

    // ── DELETE /admin/api/user/{id} ───────────────────────────────────
    @DeleteMapping("/admin/api/user/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable String id) {

        User user = userRepository.findByUserId(id).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String role = user.getRole();

        // 2. Delete role-specific profile
        switch (role) {

            case "STUDENT":
                StudentProfile studentProfile = studentProfileRepository.findByUserId(id);
                if (studentProfile != null) {
                    studentProfileRepository.delete(studentProfile);
                }
                // Also delete student's applications
                applicationRepository.deleteByStudentId(id);
                break;

            case "PROFESSIONAL":
                ProfessionalProfile professionalProfile = professionalProfileRepository.findByUserId(id);
                if (professionalProfile != null) {
                    professionalProfileRepository.delete(professionalProfile);
                }
                // Also delete professional's applications
                applicationRepository.deleteByStudentId(id);
                break;

            case "ORGANIZATION":
                OrganizationProfile orgProfile = organizationProfileRepository.findByUserId(id);
                if (orgProfile != null) {
                    organizationProfileRepository.delete(orgProfile);
                }
                // Also delete all posts by this org
                postRepository.deleteByOrganizationId(id);
                // Also delete applications for those posts
                // (optional — depends on your data integrity needs)
                break;
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok("User and all related data deleted successfully");
    }

    // ── DELETE /admin/api/post/{id} ───────────────────────────────────
    @DeleteMapping("/admin/api/post/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePost(@PathVariable String id) {
        postRepository.deleteById(id);
        applicationRepository.deleteByPostId(id);

        return ResponseEntity.ok("Deleted");
    }

}
