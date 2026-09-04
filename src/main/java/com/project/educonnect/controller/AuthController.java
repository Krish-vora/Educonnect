package com.project.educonnect.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.educonnect.dto.ApplicantDTO;
import com.project.educonnect.dto.RegisterRequestDTO;
import com.project.educonnect.model.Application;
import com.project.educonnect.model.CodingProfile;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.ApplicationService;
import com.project.educonnect.service.AuthService;
import com.project.educonnect.service.OrganizationService;
import com.project.educonnect.service.PostService;
import com.project.educonnect.service.ProfessionalService;
import com.project.educonnect.service.SavedApplicationService;
import com.project.educonnect.service.StudentService;
import com.project.educonnect.service.GithubService;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private ProfessionalService professionalService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostService postService;

    @Autowired
    private SavedApplicationService savedApplicationService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private OrganizationProfileRepository organizationProfileRepository;

    @Autowired
    private GithubService githubService;

    @GetMapping("/admin/home")
    public String adminHome() {
        return "adminDashboard";
    }

    @GetMapping("/api/auth/forgot-password")
    public String getForgotPassword() {
        return "forgot-password";
    }

    @GetMapping("/employeeDashboard")
    public String getStudentHome(Authentication auth, Model model) {
        return "studentHome"; // Return the name of the student home view
    }

    @GetMapping("/api/auth/register")
    public String showRegisterPage() {
        return "register"; // Return the name of the registration view
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO request) {
        String response = authService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/home")
    public String studentHome(Authentication authentication, Model model, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String email = authentication.getName();

        User user = studentService.getUserByEmail(email);

        StudentProfile student = studentService.getStudentByEmail(email);

        // Page<OpportunityPost> postPage = postService.getPaginatedPosts(page, size);

        // List<OpportunityPost> posts = postService.getAllPosts();

        List<OpportunityPost> posts = postService.getRankedPostsForStudent(student);

        List<String> savedPostIds = savedApplicationService.getSavedPostIds(user.getUserId());
        List<OpportunityPost> savedPosts = savedApplicationService.getSavedPosts(user.getUserId());
        List<String> appliedPostIds = applicationService.getAppliedPostIds(user.getUserId());
        Map<String, String> orgNameMap = posts.stream()
                .map(OpportunityPost::getOrganizationId)
                .filter(orgId -> orgId != null)
                .distinct()
                .collect(Collectors.toMap(
                        orgId -> orgId,
                        orgId -> {
                            OrganizationProfile org = organizationService.getOrgById(orgId);

                            // Step 3 - Check what getOrgById returns
                            System.out.println("OrgId: " + orgId + " | Org found: " + org);

                            return (org != null) ? org.getOrganizationName() : "Unknown";
                        }));
        System.out.println("name = " + orgNameMap);


        CodingProfile codingProfile = student.getCodingProfile();

        if (codingProfile == null) {
            codingProfile = new CodingProfile();
            student.setCodingProfile(codingProfile);
        }

        int githubFollowers = 0;

        if (codingProfile != null &&
                codingProfile.getGithubProfile() != null &&
                !codingProfile.getGithubProfile().isEmpty()) {

            String githubUsername = codingProfile.getGithubProfile()
                    .replace("https://github.com/", "")
                    .replace("http://github.com/", "")
                    .replace("github.com/", "")
                    .trim();

            githubFollowers = githubService.getGithubFollowers(githubUsername);
        }

        model.addAttribute(
                "githubFollowers",
                githubFollowers);
        model.addAttribute("appliedPostIds", appliedPostIds);
        model.addAttribute("student", student);
        model.addAttribute("user", user); // 🔥 important
        model.addAttribute("email", email);
        model.addAttribute("posts", posts);
        model.addAttribute("savedPostIds", savedPostIds);
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("orgNameMap", orgNameMap);
        model.addAttribute("currentPage", page);
        model.addAttribute(
                "codingProfile",
                student.getCodingProfile());

        return "studentHome";

    }

    @GetMapping("/organization/home")
    public String organizationHome(Authentication authentication, Model model,
            @RequestParam(required = false) String type) {

        String email = authentication.getName();
        User user = organizationService.getUserByEmail(email);
        OrganizationProfile organization = organizationService.getOrganizationByEmail(email);

        List<StudentProfile> student = studentService.getAll();
        List<OpportunityPost> posts = postService.getPosts(user.getUserId(), type);
        System.out.println("xpost" + posts);

        OrganizationProfile org = organizationProfileRepository.findByUserId(user.getUserId());

        List<String> postIds = posts.stream()
                .map(OpportunityPost::getId)
                .collect(Collectors.toList());

        long totalApplicants = postIds.isEmpty() ? 0
                : applicationRepository.countByPostIdIn(postIds);

        Map<String, Long> applicantsPerPost = new HashMap<>();
        for (OpportunityPost post : posts) {
            long count = applicationRepository.countByPostId(post.getId());
            applicantsPerPost.put(post.getId(), count);
        }

        System.out.println("totalApplicants: " + totalApplicants);
        System.out.println("applicantsPerPost: " + applicantsPerPost);

        List<ApplicantDTO> applicants = applicationService.getApplicantsForOrg(user.getUserId());
        Set<String> uniquePosts = new HashSet<>();

        for (ApplicantDTO a : applicants) {
            uniquePosts.add(a.getPostTitle());
        }

        long totalSelected = postIds.isEmpty() ? 0
                : applicationRepository.countByPostIdInAndStatus(postIds, "SHORTLISTED");

        int selectionPercentage = totalApplicants > 0
                ? (int) Math.round((totalSelected * 100.0) / totalApplicants)
                : 0;

        model.addAttribute("totalSelected", totalSelected);
        model.addAttribute("selectionPercentage", selectionPercentage);

        model.addAttribute("uniquePosts", uniquePosts);
        model.addAttribute("organization", organization);
        model.addAttribute("user", user); // 🔥 important
        model.addAttribute("email", email);
        model.addAttribute("postCount", posts.size());
        model.addAttribute("posts", posts);
        model.addAttribute("student", student);
        System.out.println("mystudent" + student);
        model.addAttribute("totalApplicants", totalApplicants);
        model.addAttribute("applicants", applicants);
        return "organizationHome";
    }

    @GetMapping("/professional/home")
    public String professionalHome(Authentication authentication, Model model) {
        String email = authentication.getName();

        // Get User (for name)
        User user = professionalService.getUserByEmail(email);

        // Get Professional Profile
        ProfessionalProfile professional = professionalService.getprofessionalByEmail(email);
        System.out.println("professionalprt" + professional);

        List<OpportunityPost> posts = postService.getRankedPostsForProfessional(professional);

        List<String> savedPostIds = savedApplicationService.getSavedPostIds(user.getUserId());
        List<OpportunityPost> savedPosts = savedApplicationService.getSavedPosts(user.getUserId());
        List<String> appliedPostIds = applicationService.getAppliedPostIds(user.getUserId());
        Map<String, String> orgNameMap = posts.stream()
                .map(OpportunityPost::getOrganizationId)
                .filter(orgId -> orgId != null)
                .distinct()
                .collect(Collectors.toMap(
                        orgId -> orgId,
                        orgId -> {
                            OrganizationProfile org = organizationService.getOrgById(orgId);
                            System.out.println("OrgId: " + orgId + " | Org found: " + org);
                            return (org != null) ? org.getOrganizationName() : "Unknown";
                        }));
        System.out.println("name = " + orgNameMap);

        model.addAttribute("appliedPostIds", appliedPostIds);
        model.addAttribute("professional", professional);
        model.addAttribute("user", user); // 🔥 important
        model.addAttribute("email", email);

        model.addAttribute("posts", posts);
        model.addAttribute("savedPostIds", savedPostIds);
        model.addAttribute("savedPosts", savedPosts);
        model.addAttribute("orgNameMap", orgNameMap);
        return "professionalHome";
    }

    @PostMapping("/professional/saved/toggle/{postId}")
    @ResponseBody
    public Map<String, Object> toggleSaveProfessional(@PathVariable String postId, Authentication authentication) {
        String email = authentication.getName();
        User user = professionalService.getUserByEmail(email);
        boolean saved = savedApplicationService.toggleSave(user.getUserId(), postId);
        return Map.of("saved", saved);
    }

    @GetMapping("/professional/saved/saved-posts")
    @ResponseBody
    public List<OpportunityPost> getProfessionalSavedPosts(Authentication authentication) {
        String email = authentication.getName();
        User user = professionalService.getUserByEmail(email);
        return savedApplicationService.getSavedPosts(user.getUserId());
    }

    @GetMapping("api/auth/check-email")
    @ResponseBody
    public boolean checkEmail(@RequestParam String email) {
        System.out.println("email is fired: " + email);
        return userRepository.existsByEmail(email);
    }

    @GetMapping("/student/posts")
    @ResponseBody
    public List<OpportunityPost> getPosts(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size) {

        Page<OpportunityPost> postPage = postService
                .getPaginatedPosts(
                        page,
                        size);

        return postPage.getContent();
    }
}
