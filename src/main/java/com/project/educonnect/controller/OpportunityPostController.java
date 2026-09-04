package com.project.educonnect.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.educonnect.model.Application;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ApplicationRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.service.ApplicationService;
import com.project.educonnect.service.OrganizationService;
import com.project.educonnect.service.PostService;
import com.project.educonnect.service.StudentService;

@RestController
@RequestMapping("/")
@CrossOrigin("*")
public class OpportunityPostController {
    @Autowired
    private PostService service;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private PostService postService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PostRepository  postRepository;

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private StudentService studentService;

    @PostMapping("api/opportunities/create")
    public OpportunityPost createPost(@RequestBody OpportunityPost post,
            Authentication auth) {

        String orgId = auth.getName(); // or extract from JWT
        return service.createPost(post, orgId);
    }

    @GetMapping("/all")
    public List<OpportunityPost> getAllPosts() {
        return service.getAllPosts();
    }

    @GetMapping("/my-posts")
    public List<OpportunityPost> getMyPosts(Authentication auth) {
        return service.getPostsByOrg(auth.getName());
    }

    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable String id) {
        service.deletePost(id);
        return "Deleted Successfully";
    }

    @GetMapping("/organization/posts")
    @ResponseBody
    public List<OpportunityPost> getFilteredPosts(
            @RequestParam(required = false) String type,
            Authentication authentication) {

        String email = authentication.getName();
        User user = organizationService.getUserByEmail(email);

        // 🔥 SAME METHOD AGAIN (NO DUPLICATE LOGIC)
        System.out.println("🔥 API HIT: /organization/posts");
        System.out.println("Type: " + type);
        System.out.println("User: " + email);
        System.out.println("Posts: " + postService.getPosts(user.getUserId(), type));
        return postService.getPosts(user.getUserId(), type);
    }

    @PostMapping("/student/apply/{postId}")
    @ResponseBody
    public Map<String, Object> applyPost(
            @PathVariable String postId,
            Authentication authentication) {

        String email = authentication.getName();

        User user = studentService.getUserByEmail(email);

        boolean applied = applicationService.apply(user.getUserId(), postId);

        return Map.of(
                "success", applied);
    }


}
