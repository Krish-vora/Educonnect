package com.project.educonnect.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.educonnect.model.SavedApplication;

public interface SavedApplicationRepository
                extends MongoRepository<SavedApplication, String> {

        Optional<SavedApplication> findByStudentIdAndPostId(
                        String studentId,
                        String postId);

        void deleteByStudentIdAndPostId(
                        String studentId,
                        String postId);

        List<SavedApplication> findByStudentId(String studentId);

        Optional<SavedApplication> findByPostId(String postIdString);

        void deleteByPostId(String postId);
}