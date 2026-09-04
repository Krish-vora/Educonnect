package com.project.educonnect.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    // ✅ Post info — stored ONCE
    private String postId;
    private String postTitle;
    private String postType;
    private String orgName;
    private String organizationId;
    private LocalDateTime createdAt;

    // ✅ Only store IDs of students who READ it
    // unread = all students NOT in this list
    private List<String> readByUserIds = new ArrayList<>();
}