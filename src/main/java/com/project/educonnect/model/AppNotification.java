package com.project.educonnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "app_notifications")
public class AppNotification {

    @Id
    private String id;

    private String organizationId;   // which org receives this
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String studentSkills;
    private String studentUniversity;
    private String studentEducation;
    private String studentLocation;

    private String postId;
    private String postTitle;
    private String postType;

    private String eventType;        // "APPLIED" or "CANCELLED"
    private boolean read;
    private LocalDateTime createdAt;
}