package com.project.educonnect.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "saved_applications")
public class SavedApplication {

    @Id
    private String id;

    private String studentId;

    private String postId;

    private LocalDateTime savedAt;
}