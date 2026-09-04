package com.project.educonnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;

@Service
public class StudentService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private StudentProfileRepository studentRepo;

    public StudentProfile getStudentByEmail(String email) {

        // Step 1: Get User
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 2: Get StudentProfile using userId
        return studentRepo.findByUserId(user.getUserId());
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<StudentProfile> getAll() {
        return studentRepo.findAll();
    }

    public StudentProfile getStudentByUserId(String userId) {
        return studentRepo.findByUserId(userId);
    }

}
