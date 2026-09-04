package com.project.educonnect.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.project.educonnect.model.AdminProfile;

public interface AdminProfileRepository extends MongoRepository<AdminProfile, String> {
}