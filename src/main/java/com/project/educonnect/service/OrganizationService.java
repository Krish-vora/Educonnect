package com.project.educonnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.OrganizationProfileRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;

@Service
public class OrganizationService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrganizationProfileRepository organizationRepo;

    public OrganizationProfile getOrganizationByEmail(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return organizationRepo.findByUserId(user.getUserId());
    }

    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public OrganizationProfile getOrgById(String orgId) {
        OrganizationProfile org = organizationRepo.findByUserId(orgId); // ✅ changed from findById to findByUserId
        return org;
    }

    public OrganizationProfile getOrgByUserId(String userId) {
        return organizationRepo.findByUserId(userId);
    }

}
