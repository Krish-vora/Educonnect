package com.project.educonnect.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.educonnect.dto.AnalyticsDTO;
import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.SavedApplicationRepository;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.ApplicationService;
import com.project.educonnect.service.OrganizationService;
import com.project.educonnect.service.PostService;

@Controller
public class OrganizationController {

        @Autowired
        private OrganizationService organizationService;

        @Autowired
        private PostService postService;

        @Autowired
        private ApplicationService applicationService;

        @Autowired
        private UserRepository userRepo;

        @Autowired
        private OrganizationProfileRepository organizationProfileRepository;

        @Autowired
        private ApplicationRepository applicationRepository;

        @Autowired
        private SavedApplicationRepository savedApplicationRepository;

        @Autowired
        private PostRepository postRepository;

        @GetMapping("/organization/analytics")
        @ResponseBody
        public AnalyticsDTO getAnalytics(
                        Authentication auth) {

                String email = auth.getName();

                // FIND USER FROM EMAIL

                User user = userRepo
                                .findByEmail(email)
                                .orElse(null);

                if (user == null) {

                        return new AnalyticsDTO();
                }

                // FIND ORGANIZATION PROFILE USING userId

                OrganizationProfile org = organizationService.getOrgByUserId(
                                user.getUserId());

                if (org == null) {

                        return new AnalyticsDTO();
                }

                List<OpportunityPost> posts = postService.getPosts(
                                org.getUserId());
                List<Application> allApplications = new ArrayList<>();

                int activePosts = 0;
                int closedPosts = 0;

                for (OpportunityPost post : posts) {

                        if ("ACTIVE".equalsIgnoreCase(
                                        post.getStatus())) {

                                activePosts++;

                        } else {

                                closedPosts++;
                        }

                        List<Application> applications = applicationService
                                        .getApplicationsByPostId(
                                                        post.getId());

                        allApplications.addAll(applications);
                }

                int totalApplications = allApplications.size();

                int shortlisted = 0;
                int rejected = 0;

                for (Application app : allApplications) {

                        if ("SHORTLISTED".equalsIgnoreCase(
                                        app.getStatus())) {

                                shortlisted++;
                        }

                        if ("REJECTED".equalsIgnoreCase(
                                        app.getStatus())) {

                                rejected++;
                        }
                }

                double avgPerPost = posts.size() > 0
                                ? (double) totalApplications
                                                / posts.size()
                                : 0;

                double conversionRate = totalApplications > 0
                                ? ((double) shortlisted
                                                / totalApplications) * 100
                                : 0;

                AnalyticsDTO dto = new AnalyticsDTO();

                dto.setTotalApplications(
                                totalApplications);

                dto.setTotalPosts(posts.size());

                dto.setAvgPerPost(avgPerPost);

                dto.setConversionRate(
                                conversionRate);

                dto.setShortlistedCount(
                                shortlisted);

                dto.setRejectedCount(
                                rejected);

                dto.setActivePosts(activePosts);

                dto.setClosedPosts(closedPosts);

                return dto;
        }

        @PostMapping("/organization/application/status")
        @ResponseBody
        public String updateApplicationStatus(

                        @RequestParam String applicationId,

                        @RequestParam String status) {

                Application updated = applicationService.updateStatus(
                                applicationId,
                                status);
                // System.out.println("dataxome");
                if (updated == null) {

                        return "FAILED";
                }

                return "SUCCESS";
        }

        @GetMapping("/organization/recent-posts")
        @ResponseBody
        public List<OpportunityPost> getRecentPosts(
                        Authentication auth) {

                String email = auth.getName();

                User user = userRepo
                                .findByEmail(email)
                                .orElse(null);

                if (user == null) {

                        return new ArrayList<>();
                }

                OrganizationProfile org = organizationService
                                .getOrgByUserId(
                                                user.getUserId());

                if (org == null) {

                        return new ArrayList<>();
                }

                return postService.getRecentPosts(
                                org.getUserId());
        }

        @GetMapping("/organization/post/{id}")
        @ResponseBody
        public OpportunityPost getPostById(
                        @PathVariable String id) {

                return postService.getPostById(id);
        }

        @PutMapping("/organization/update-post")
        @ResponseBody
        public String updatePost(
                        @RequestBody OpportunityPost updatedPost) {

                postService.updatePost(updatedPost);

                return "UPDATED";
        }

        @PostMapping("/organization/profile/update")
        public String updateOrganizationProfile(
                        @RequestParam String organizationName,
                        @RequestParam String industry,
                        @RequestParam String location,
                        @RequestParam String website,
                        @RequestParam String totalEmployees,
                        @RequestParam String bio,
                        Authentication authentication) {

                // 1. Get the logged-in user's email
                String email = authentication.getName();

                // 2. Fetch the User and their existing OrganizationProfile
                User user = organizationService.getUserByEmail(email);
                OrganizationProfile existingProfile = organizationProfileRepository.findByUserId(user.getUserId());

                // 3. Update all fields from the model
                if (existingProfile != null) {
                        existingProfile.setOrganizationName(organizationName);
                        existingProfile.setIndustry(industry);
                        existingProfile.setLocation(location);
                        existingProfile.setWebsite(website);
                        existingProfile.setTotalEmployees(totalEmployees);
                        existingProfile.setBio(bio);

                        // 4. Save to the database
                        organizationProfileRepository.save(existingProfile);
                }

                // 5. Redirect back to the dashboard to show the updated data
                return "redirect:/organization/home";
        }

        @DeleteMapping("/organization/api/post/{id}")
        @ResponseBody
        public ResponseEntity<String> deletePost(@PathVariable String id) {
                postRepository.deleteById(id);
                applicationRepository.deleteByPostId(id);
                savedApplicationRepository.deleteByPostId(id);
                return ResponseEntity.ok("Deleted");
        }
}
