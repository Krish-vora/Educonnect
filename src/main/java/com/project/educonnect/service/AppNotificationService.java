package com.project.educonnect.service;

import com.project.educonnect.model.AppNotification;
import com.project.educonnect.model.OpportunityPost;
import com.project.educonnect.model.StudentProfile;
import com.project.educonnect.model.User;
import com.project.educonnect.repository.AppNotificationRepository;
import com.project.educonnect.repository.PostRepository;
import com.project.educonnect.repository.StudentProfileRepository;
import com.project.educonnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppNotificationService {

    @Autowired
    private AppNotificationRepository repository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Called when a student applies or cancels — creates one notification
     * that goes to the organization that owns the post.
     */
    public void createApplicationNotification(String studentId, String postId, String eventType) {

        // Get post
        OpportunityPost post = postRepository.findById(postId).orElse(null);
        if (post == null) return;

        // Get student user + profile
        Optional<User> optUser = userRepository.findByUserId(studentId);
        if (optUser.isEmpty()) return;
        User user = optUser.get();

        StudentProfile profile = studentProfileRepository.findByUserId(studentId);

        AppNotification notif = new AppNotification();
        notif.setOrganizationId(post.getOrganizationId());
        notif.setStudentId(studentId);
        notif.setStudentName(user.getFullName());
        notif.setStudentEmail(user.getEmail());
        notif.setPostId(postId);
        notif.setPostTitle(post.getTitle());
        notif.setPostType(post.getType());
        notif.setEventType(eventType);   // "APPLIED" or "CANCELLED"
        notif.setRead(false);
        notif.setCreatedAt(LocalDateTime.now());

        if (profile != null) {
            notif.setStudentSkills(profile.getStudentSkills());
            notif.setStudentUniversity(profile.getStudentUniversity());
            notif.setStudentEducation(profile.getStudentEducation());
            notif.setStudentLocation(profile.getLocation());
        }

        repository.save(notif);
    }

    public List<AppNotification> getNotificationsForOrg(String organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    public long countUnread(String organizationId) {
        return repository.countByOrganizationIdAndReadFalse(organizationId);
    }

    public void markAllRead(String organizationId) {
        List<AppNotification> unread = repository.findByOrganizationIdAndReadFalse(organizationId);
        unread.forEach(n -> n.setRead(true));
        repository.saveAll(unread);
    }
}