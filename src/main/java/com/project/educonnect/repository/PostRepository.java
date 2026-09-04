package com.project.educonnect.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.educonnect.model.OpportunityPost;

public interface PostRepository extends MongoRepository<OpportunityPost, String> {
    List<OpportunityPost> findByOrganizationId(String organizationId);

    List<OpportunityPost> findByType(String type);

    List<OpportunityPost> findBySkillsRequiredIn(List<String> skills);

    List<OpportunityPost> findByOrganizationIdAndType(String organizationId, String type);

    Page<OpportunityPost> findAll(Pageable pageable);

    List<OpportunityPost> findByOrganizationIdOrderByCreatedAtDesc(
            String organizationId);

    List<OpportunityPost> findByStatus(String status);

    long countByOrganizationId(String organizationId);

    void deleteById(String postId);

    void deleteByOrganizationId(String organizationId);
    
}
