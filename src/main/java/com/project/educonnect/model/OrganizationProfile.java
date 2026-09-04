package com.project.educonnect.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Data
@NoArgsConstructor
@Document(collection = "organization_profiles")
public class OrganizationProfile {
    
    @Id
    private String id;

    private String userId; // Reference to the User document
    private String organizationName;
    private String industry;
    private String bio;
    private String location;
    private String website;
    private String totalEmployees;

}
