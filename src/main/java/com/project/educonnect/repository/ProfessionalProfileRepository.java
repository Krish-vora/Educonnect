package com.project.educonnect.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.ProfessionalProfile;

public interface ProfessionalProfileRepository extends MongoRepository<ProfessionalProfile, String> {
        ProfessionalProfile findByUserId(String userId);

} 