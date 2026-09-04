package com.project.educonnect.repository;

import com.project.educonnect.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends MongoRepository<Application, String> {

    Optional<Application> findByStudentIdAndPostId(
            String studentId,
            String postId);

    List<Application> findByStudentId(String studentId);

    // count applications for a list of postIds
    long countByPostIdIn(List<String> postIds);

    // get all applications for a list of postIds
    List<Application> findByPostIdIn(List<String> postIds);

    // count by single postId
    long countByPostId(String postId);

    List<Application> findByPostId(String postId);

    Optional<Application> findById(String id);

    long countByPostIdInAndStatus(List<String> postIds, String status);

    long countByStudentId(String studentId);

    void deleteByPostId(String postId);

    void deleteByStudentId(String studentId);

    boolean existsByStudentIdAndPostId(
            String studentId,
            String postId);

    List<Application> findByStudentIdAndAppliedAt(
            String studentId,
            LocalDate appliedAt

    );

}