package com.project.educonnect.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    private String studentId;

    private String postId;
    private String status;
    private LocalDate appliedAt;
}