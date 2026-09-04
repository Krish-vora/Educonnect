package com.project.educonnect.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.educonnect.model.OrganizationProfile;
import com.project.educonnect.model.StudentProfile;

public interface OrganizationProfileRepository extends MongoRepository<OrganizationProfile, String> {
        OrganizationProfile findByUserId(String userId);


                         

}
