package com.project.educonnect.repository;

import com.project.educonnect.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    // // get all notifications for a student
    // List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);

    // // count unread
    // long countByUserIdAndReadFalse(String userId);

    // // mark all read
    // List<Notification> findByUserIdAndReadFalse(String userId);

    // ✅ Get recent notifications (all students see same list)

    List<Notification> findTop20ByOrderByCreatedAtDesc();

    // ✅ Find notifications NOT read by this user
    @Query("{ 'readByUserIds': { $not: { $elemMatch: { $eq: ?0 } } } }")
    List<Notification> findUnreadByUserId(String userId);

    // ✅ Count notifications NOT read by this user
    @Query(value = "{ 'readByUserIds': { $not: { $elemMatch: { $eq: ?0 } } } }", count = true)
    long countUnreadByUserId(String userId);
}