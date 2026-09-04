package com.project.educonnect.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.educonnect.model.StudentProfile;

public interface StudentProfileRepository extends MongoRepository<StudentProfile, String> {
    StudentProfile findByUserId(String userId);

}