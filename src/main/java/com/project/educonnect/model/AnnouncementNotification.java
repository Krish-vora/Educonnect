package com.project.educonnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "announcement_notifications")
public class AnnouncementNotification {

    @Id
    private String id;

    private String title;           // announcement title
    private String body;            // announcement message
    private String targetRole;      // "ALL", "STUDENT", "ORGANIZATION", "PROFESSIONAL"
    private LocalDateTime createdAt;


    private List<String> readByUserIds = new ArrayList<>();
}