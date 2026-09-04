package com.project.educonnect.controller;

import com.project.educonnect.model.AnnouncementNotification;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.UserRepository;
import com.project.educonnect.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserRepository userRepository;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // ── shared helper ─────────────────────────────────────────────────
    private List<Map<String, Object>> buildResponse(
            List<AnnouncementNotification> list, String userId) {

        return list.stream().map(ann -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",        ann.getId());
            map.put("title",     ann.getTitle());
            map.put("body",      ann.getBody());
            map.put("targetRole",ann.getTargetRole());
            map.put("createdAt", ann.getCreatedAt() != null
                    ? ann.getCreatedAt().format(FMT) : "");
            boolean read = ann.getReadByUserIds() != null
                    && ann.getReadByUserIds().contains(userId);
            map.put("read", read);
            return map;
        }).collect(Collectors.toList());
    }

    private User currentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow();
    }

    // ════════════════════════════════════════════════════
    // STUDENT endpoints
    // ════════════════════════════════════════════════════

    @GetMapping("/student/announcements")
    public List<Map<String, Object>> studentAnnouncements(
            Authentication auth) {
        User u = currentUser(auth);
        return buildResponse(
                announcementService.getForRole("STUDENT"), u.getUserId());
    }

    @GetMapping("/student/announcements/unread-count")
    public Map<String, Long> studentUnread(Authentication auth) {
        User u = currentUser(auth);
        return Map.of("count",
                announcementService.countUnread("STUDENT", u.getUserId()));
    }

    @PostMapping("/student/announcements/mark-read")
    public Map<String, String> studentMarkRead(Authentication auth) {
        User u = currentUser(auth);
        announcementService.markAllRead("STUDENT", u.getUserId());
        return Map.of("status", "ok");
    }

    // ════════════════════════════════════════════════════
    // ORGANIZATION endpoints
    // ════════════════════════════════════════════════════

    @GetMapping("/organization/announcements")
    public List<Map<String, Object>> orgAnnouncements(Authentication auth) {
        User u = currentUser(auth);
        return buildResponse(
                announcementService.getForRole("ORGANIZATION"), u.getUserId());
    }

    @GetMapping("/organization/announcements/unread-count")
    public Map<String, Long> orgUnread(Authentication auth) {
        User u = currentUser(auth);
        return Map.of("count",
                announcementService.countUnread("ORGANIZATION", u.getUserId()));
    }

    @PostMapping("/organization/announcements/mark-read")
    public Map<String, String> orgMarkRead(Authentication auth) {
        User u = currentUser(auth);
        announcementService.markAllRead("ORGANIZATION", u.getUserId());
        return Map.of("status", "ok");
    }

    // ════════════════════════════════════════════════════
    // PROFESSIONAL endpoints
    // ════════════════════════════════════════════════════

    @GetMapping("/professional/announcements")
    public List<Map<String, Object>> profAnnouncements(
            Authentication auth) {
        User u = currentUser(auth);
        return buildResponse(
                announcementService.getForRole("PROFESSIONAL"), u.getUserId());
    }

    @GetMapping("/professional/announcements/unread-count")
    public Map<String, Long> profUnread(Authentication auth) {
        User u = currentUser(auth);
        return Map.of("count",
                announcementService.countUnread("PROFESSIONAL", u.getUserId()));
    }

    @PostMapping("/professional/announcements/mark-read")
    public Map<String, String> profMarkRead(Authentication auth) {
        User u = currentUser(auth);
        announcementService.markAllRead("PROFESSIONAL", u.getUserId());
        return Map.of("status", "ok");
    }
}