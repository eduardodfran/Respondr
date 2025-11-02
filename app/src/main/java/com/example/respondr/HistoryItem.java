package com.example.respondr;

public class HistoryItem {
    private String emergencyType;
    private String timestamp;
    private String location;
    private String description;
    private String status;

    public HistoryItem(String emergencyType, String timestamp, String location, String description, String status) {
        this.emergencyType = emergencyType;
        this.timestamp = timestamp;
        this.location = location;
        this.description = description;
        this.status = status;
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
}
