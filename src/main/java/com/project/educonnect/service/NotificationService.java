package com.project.educonnect.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.educonnect.model.Notification;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.repository.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // ✅ Create ONE notification per post (not per student)
    public void createNotification(OpportunityPost post, String orgName) {

        Notification notification = new Notification();
        notification.setPostId(post.getId());
        notification.setPostTitle(post.getTitle());
        notification.setPostType(post.getType());
        notification.setOrgName(orgName);
        notification.setOrganizationId(post.getOrganizationId());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadByUserIds(new ArrayList<>()); // ✅ empty list, not null

        notificationRepository.save(notification);

        System.out.println("Created notification for post: " + post.getTitle());
    }

    // ✅ Mark all as read for this student
    // just add their userId to readByUserIds list
    public void markAllRead(String userId) {

        // ✅ Get ALL notifications (not just unread for this user)
        List<Notification> all = notificationRepository.findTop20ByOrderByCreatedAtDesc();

        for (Notification n : all) {

            // ✅ Initialize list if null
            if (n.getReadByUserIds() == null) {
                n.setReadByUserIds(new ArrayList<>());
            }

            // ✅ Add userId only if not already there
            if (!n.getReadByUserIds().contains(userId)) {
                n.getReadByUserIds().add(userId);
                System.out.println("Marking as read for userId: " + userId +
                        " | notification: " + n.getPostTitle());
            }
        }

        notificationRepository.saveAll(all);
    }

    // ✅ Check if this student has read a notification
    public boolean isRead(Notification n, String userId) {
        return n.getReadByUserIds().contains(userId);
    }

    public long countUnread(String userId) {
        long count = notificationRepository.countUnreadByUserId(userId);
        System.out.println("Unread count for userId: " + userId + " = " + count);
        return count;
    }

    public List<Notification> getNotifications() {
        List<Notification> list = notificationRepository.findTop20ByOrderByCreatedAtDesc();
        System.out.println("Total notifications in DB: " + list.size());
        list.forEach(
                n -> System.out.println("Notification: " + n.getPostTitle() + " | readBy: " + n.getReadByUserIds()));
        return list;
    }
}