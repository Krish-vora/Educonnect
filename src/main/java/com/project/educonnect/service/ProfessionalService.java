package com.project.educonnect.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.UserRepository;

@Service
public class ProfessionalService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ProfessionalProfileRepository profileRepository;


    public ProfessionalProfile getprofessionalByEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return profileRepository.findByUserId(user.getUserId());
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public List<ProfessionalProfile> getAll(){
        return profileRepository.findAll();
    }

}
