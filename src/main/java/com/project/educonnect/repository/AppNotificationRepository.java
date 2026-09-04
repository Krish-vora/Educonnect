package com.project.educonnect.repository;

import com.project.educonnect.model.AppNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AppNotificationRepository extends MongoRepository<AppNotification, String> {

    // All notifications for an org, newest first
    List<AppNotification> findByOrganizationIdOrderByCreatedAtDesc(String organizationId);

    // Count unread for an org
    long countByOrganizationIdAndReadFalse(String organizationId);

    // All unread for an org
    List<AppNotification> findByOrganizationIdAndReadFalse(String organizationId);
}