package com.project.educonnect.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.educonnect.model.Notification;
import com.project.educonnect.model.User;
import com.project.educonnect.service.NotificationService;
import com.project.educonnect.service.ProfessionalService;

@RestController
@RequestMapping("/professional/notifications")
public class ProfessionalNotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProfessionalService professionalService;

    // ── GET /professional/notifications ──────────────────────────────
    // Returns last 20 new-post notifications, with per-professional read flag
    @GetMapping
    public List<Map<String, Object>> getNotifications(Authentication authentication) {

        User user   = professionalService.getUserByEmail(authentication.getName());
        String uid  = user.getUserId();

        List<Notification> notifications = notificationService.getNotifications();

        return notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id",        n.getId());
            map.put("postId",    n.getPostId());
            map.put("postTitle", n.getPostTitle());
            map.put("postType",  n.getPostType());
            map.put("orgName",   n.getOrgName());
            map.put("createdAt", n.getCreatedAt());

            // read = this professional's userId is in the readByUserIds list
            boolean isRead = n.getReadByUserIds() != null
                    && n.getReadByUserIds().contains(uid);
            map.put("read", isRead);

            return map;
        }).collect(Collectors.toList());
    }

    // ── GET /professional/notifications/unread-count ──────────────────
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        User user  = professionalService.getUserByEmail(authentication.getName());
        long count = notificationService.countUnread(user.getUserId());
        return Map.of("count", count);
    }

    // ── POST /professional/notifications/mark-read ────────────────────
    @PostMapping("/mark-read")
    public Map<String, String> markAllRead(Authentication authentication) {
        User user = professionalService.getUserByEmail(authentication.getName());
        notificationService.markAllRead(user.getUserId());
        return Map.of("status", "ok");
    }
}