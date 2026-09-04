package com.project.educonnect.controller;

import com.project.educonnect.model.AppNotification;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.AppNotificationService;
import com.project.educonnect.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/organization/notifications")
public class OrgNotificationController {

    @Autowired
    private AppNotificationService appNotificationService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private UserRepository userRepository;

    // GET /organization/notifications  → list for bell dropdown
    @GetMapping
    public List<Map<String, Object>> getNotifications(Authentication authentication) {
        String orgId = getOrgId(authentication);
        List<AppNotification> list = appNotificationService.getNotificationsForOrg(orgId);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM, hh:mm a");

        return list.stream().map(n -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",               n.getId());
            map.put("eventType",        n.getEventType());
            map.put("read",             n.isRead());
            map.put("postTitle",        n.getPostTitle());
            map.put("postType",         n.getPostType());
            map.put("studentName",      n.getStudentName());
            map.put("studentEmail",     n.getStudentEmail());
            map.put("studentSkills",    n.getStudentSkills());
            map.put("studentUniversity",n.getStudentUniversity());
            map.put("studentEducation", n.getStudentEducation());
            map.put("studentLocation",  n.getStudentLocation());
            map.put("createdAt",        n.getCreatedAt() != null ? n.getCreatedAt().format(fmt) : "");
            return map;
        }).collect(Collectors.toList());
    }

    // GET /organization/notifications/unread-count
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        String orgId = getOrgId(authentication);
        return Map.of("count", appNotificationService.countUnread(orgId));
    }

    // POST /organization/notifications/mark-read
    @PostMapping("/mark-read")
    public Map<String, String> markRead(Authentication authentication) {
        String orgId = getOrgId(authentication);
        appNotificationService.markAllRead(orgId);
        return Map.of("status", "ok");
    }

    private String getOrgId(Authentication authentication) {
        User user = organizationService.getUserByEmail(authentication.getName());
        return user.getUserId();
    }
}