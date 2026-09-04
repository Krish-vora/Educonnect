package com.project.educonnect.dto;

public class DashboardStatsDto {
    private long totalApplications;
    private long totalSelected;
    private double selectionPercentage;

    public DashboardStatsDto(long totalApplications, long totalSelected) {
        this.totalApplications = totalApplications;
        this.totalSelected = totalSelected;
        this.selectionPercentage = totalApplications == 0 ? 0.0 : 
            Math.round(((double) totalSelected / totalApplications) * 1000.0) / 10.0; // Rounds to 1 decimal place
    }

    // Getters
    public long getTotalApplications() { return totalApplications; }
    public long getTotalSelected() { return totalSelected; }
    public double getSelectionPercentage() { return selectionPercentage; }
}