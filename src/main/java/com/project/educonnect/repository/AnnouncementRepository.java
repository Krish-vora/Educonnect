package com.project.educonnect.repository;

import com.project.educonnect.model.AnnouncementNotification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface AnnouncementRepository
        extends MongoRepository<AnnouncementNotification, String> {

    // All announcements for a role (targetRole = role or "ALL"), newest first
    List<AnnouncementNotification> findByTargetRoleInOrderByCreatedAtDesc(
            List<String> roles);

    // Count unread for a user (not in readByUserIds) + matching role
    @Query(value = "{ 'targetRole': { $in: ?0 }, "
                 + "'readByUserIds': { $not: { $elemMatch: { $eq: ?1 } } } }",
           count = true)
    long countUnreadForUser(List<String> roles, String userId);

    // Find unread for a user
    @Query("{ 'targetRole': { $in: ?0 }, "
         + "'readByUserIds': { $not: { $elemMatch: { $eq: ?1 } } } }")
    List<AnnouncementNotification> findUnreadForUser(
            List<String> roles, String userId);
}