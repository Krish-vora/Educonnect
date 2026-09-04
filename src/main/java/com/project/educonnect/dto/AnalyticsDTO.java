package com.project.educonnect.dto;

import lombok.Data;

@Data
public class AnalyticsDTO {

    private int totalApplications;

    private int totalPosts;

    private double avgPerPost;

    private double conversionRate;

    private int shortlistedCount;

    private int rejectedCount;

    private int activePosts;

    private int closedPosts;
}