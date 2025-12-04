package com.example.respondr;

public class HistoryItem {
    private String emergencyType;
    private String timestamp;
    private String location;
    private String description;
    private String status;
    private String reportId;
    private String aiResponse;

    public HistoryItem(String emergencyType, String timestamp, String location, String description, String status, String reportId, String aiResponse) {
        this.emergencyType = emergencyType;
        this.timestamp = timestamp;
        this.location = location;
        this.description = description;
        this.status = status;
        this.reportId = reportId;
        this.aiResponse = aiResponse;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
    
    public String getReportId() {
        return reportId;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
}
