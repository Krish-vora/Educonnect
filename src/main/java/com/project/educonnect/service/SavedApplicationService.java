package com.project.educonnect.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.SavedApplication;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.SavedApplicationRepository;

@Service
public class SavedApplicationService {

    @Autowired
    private SavedApplicationRepository repository;

    @Autowired
    private PostRepository opportunityPostRepository;

    public boolean toggleSave(String studentId, String postId) {

        Optional<SavedApplication> existing = repository.findByStudentIdAndPostId(
                studentId,
                postId);

        if (existing.isPresent()) {

            repository.delete(existing.get());

            return false;
        }

        SavedApplication save = new SavedApplication();

        save.setStudentId(studentId);
        save.setPostId(postId);
        save.setSavedAt(LocalDateTime.now());

        repository.save(save);

        return true;
    }

    public boolean isSaved(String studentId, String postId) {

        return repository
                .findByStudentIdAndPostId(studentId, postId)
                .isPresent();
    }

    public List<String> getSavedPostIds(String studentId) {

        return repository.findByStudentId(studentId)
                .stream()
                .map(SavedApplication::getPostId)
                .collect(Collectors.toList());
    }

    public List<OpportunityPost> getSavedPosts(String studentId) {

        List<SavedApplication> savedPosts = repository.findByStudentId(studentId);

        List<String> postIds = savedPosts.stream()
                .map(SavedApplication::getPostId)
                .toList();

        return opportunityPostRepository.findAllById(postIds);
    }

}