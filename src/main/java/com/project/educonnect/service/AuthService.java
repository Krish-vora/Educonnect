package com.project.educonnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.educonnect.dto.RegisterRequestDTO;
import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.ProfessionalProfileRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentProfileRepository studentRepo;

    @Autowired
    private OrganizationProfileRepository orgRepo;

    @Autowired
    private ProfessionalProfileRepository professionalRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(RegisterRequestDTO request) {

        // 1. Save user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        switch (request.getRole()) {

            case "STUDENT":
                StudentProfile student = new StudentProfile();
                student.setUserId(savedUser.getUserId());
                student.setStudentSkills(request.getStudentSkills());
                student.setBio(request.getBio());
                student.setStudentEducation(request.getStudentEducation());
                student.setInterests(request.getStudentInterests());
                student.setLocation(request.getLocation());
                student.setStudentUniversity(request.getStudentUniversity());
                studentRepo.save(student);
                break;

            case "ORGANIZATION":
                OrganizationProfile org = new OrganizationProfile();
                org.setUserId(savedUser.getUserId());
                org.setBio(request.getBio());
                org.setOrganizationName(request.getOrganizationName());
                org.setWebsite(request.getWebsite());
                org.setIndustry(request.getIndustry());
                org.setTotalEmployees(request.getTotalEmployees());
                org.setLocation(request.getLocation());
                orgRepo.save(org);
                break;

            case "PROFESSIONAL":
                ProfessionalProfile prof = new ProfessionalProfile();
                prof.setUserId(savedUser.getUserId());
                prof.setCompany(request.getCompany());
                prof.setBio(request.getBio());
                prof.setLocation(request.getLocation());
                prof.setExperience(request.getExperience());
                prof.setEducation(request.getEducation());
                prof.setSkills(request.getProfessionalSkills());
                professionalRepo.save(prof);
                break;

        }
        return "User Registered Successfully";
    }
}
