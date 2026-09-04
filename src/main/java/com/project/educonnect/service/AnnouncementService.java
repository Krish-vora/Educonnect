package com.project.educonnect.service;

import com.project.educonnect.model.AnnouncementNotification;
import com.project.educonnect.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository repository;
    public AnnouncementNotification create(String title, String body,
            String target) {
        AnnouncementNotification ann = new AnnouncementNotification();
        ann.setTitle(title);
        ann.setBody(body);
        // Normalise: "all" → "ALL", "STUDENT" → "STUDENT", etc.
        ann.setTargetRole(target.toUpperCase());
        ann.setCreatedAt(LocalDateTime.now());
        ann.setReadByUserIds(new ArrayList<>());
        return repository.save(ann);
    }

    public List<AnnouncementNotification> getForRole(String role) {
        
        return repository.findByTargetRoleInOrderByCreatedAtDesc(
                List.of(role.toUpperCase(), "ALL"));
    }
    public long countUnread(String role, String userId) {
        return repository.countUnreadForUser(
                List.of(role.toUpperCase(), "ALL"), userId);
    }

    public void markAllRead(String role, String userId) {
        List<AnnouncementNotification> unread = repository.findUnreadForUser(
                List.of(role.toUpperCase(), "ALL"), userId);

        unread.forEach(ann -> {
            if (ann.getReadByUserIds() == null)
                ann.setReadByUserIds(new ArrayList<>());
            if (!ann.getReadByUserIds().contains(userId))
                ann.getReadByUserIds().add(userId);
        });

        repository.saveAll(unread);
    }
}