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
import com.project.educonnect.service.StudentService;

@RestController
@RequestMapping("/student/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StudentService studentService;

    // ✅ Get all notifications with read status per student
    @GetMapping
    public List<Map<String, Object>> getNotifications(Authentication authentication) {
        System.out.println("mycalled");
        User user = studentService.getUserByEmail(authentication.getName());
        String userId = user.getUserId();

        System.out.println("Fetching notifications for userId: " + userId);

        List<Notification> notifications = notificationService.getNotifications();

        return notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("postId", n.getPostId());
            map.put("postTitle", n.getPostTitle());
            map.put("postType", n.getPostType());
            map.put("orgName", n.getOrgName());
            map.put("createdAt", n.getCreatedAt());

            // ✅ Check if THIS student has read it
            boolean isRead = n.getReadByUserIds() != null &&
                    n.getReadByUserIds().contains(userId);
            map.put("read", isRead);

            System.out.println("Notification: " + n.getPostTitle() +
                    " | read by " + userId + " = " + isRead);

            return map;
        }).collect(Collectors.toList());
    }

    // ✅ Get unread count for THIS student
    @GetMapping("/unread-count")
    public Map<String, Long> getUnreadCount(Authentication authentication) {
        User user = studentService.getUserByEmail(authentication.getName());
        String userId = user.getUserId();

        long count = notificationService.countUnread(userId);
        System.out.println("Unread count for " + userId + " = " + count);

        return Map.of("count", count);
    }



    // ✅ Mark all as read for THIS student only
    @PostMapping("/mark-read")
    public Map<String, String> markAllRead(Authentication authentication) {
        User user = studentService.getUserByEmail(authentication.getName());
        notificationService.markAllRead(user.getUserId());
        return Map.of("status", "ok");
    }

    @PostMapping("professional/notifications/mark-read")
    public Map<String, String> marksAllRead(Authentication authentication) {
        User user = studentService.getUserByEmail(authentication.getName());
        notificationService.markAllRead(user.getUserId());
        System.out.println("thimasfae");
        return Map.of("status", "ok");
    }
    @GetMapping("professional/notifications/unread-count")
    public Map<String, Long> getsUnreadCount(Authentication authentication) {
        User user = studentService.getUserByEmail(authentication.getName());
        String userId = user.getUserId();

        long count = notificationService.countUnread(userId);
        System.out.println("Unread count for " + userId + " = " + count);

        return Map.of("count", count);
    }
}