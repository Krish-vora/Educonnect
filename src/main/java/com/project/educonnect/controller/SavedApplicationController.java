package com.project.educonnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.User;
import com.project.educonnect.service.SavedApplicationService;
import com.project.educonnect.service.StudentService;

@RestController
@RequestMapping("/student/saved")
public class SavedApplicationController {

    @Autowired
    private SavedApplicationService service;

    @Autowired
    private StudentService studentService;

    @PostMapping("/toggle/{postId}")
    public Map<String, Object> toggleSave(
            @PathVariable String postId,
            Authentication authentication) {

        System.out.println("toggle save called for postId: " + postId);
        String email = authentication.getName();

        User user = studentService.getUserByEmail(email);

        boolean saved = service.toggleSave(
                user.getUserId(),
                postId);

        Map<String, Object> response = new HashMap<>();

        response.put("saved", saved);

        return response;
    }

    @GetMapping("/student/saved-posts")
    @ResponseBody
    public List<OpportunityPost> getSavedPosts(Authentication authentication) {
        System.out.println("Methodcalled");
        String email = authentication.getName();

        User user = studentService.getUserByEmail(email);
        System.out.println("muysaced = " + service.getSavedPosts(user.getUserId()));
        return service.getSavedPosts(user.getUserId());
    }
}