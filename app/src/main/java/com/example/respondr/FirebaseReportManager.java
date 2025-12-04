package com.example.respondr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FirebaseReportManager {
    private static final String TAG = "FirebaseReportManager";
    private static final String REPORTS_PATH = "emergency_reports";
    
    private final DatabaseReference databaseReference;

    public static class EmergencyReport {
        public String id;
        public String emergencyType;
        public String description;
        public String location;
        public Object latitude;  // Can be String or Double from Firebase
        public Object longitude; // Can be String or Double from Firebase
        public String address; // User-provided address from conversation
        public String addressStatus; // "provided", "not_provided", "gps_only"
        public String timestamp;
        public String status;
        public String aiResponse;

        public EmergencyReport() {
            // Default constructor required for Firebase
        }

        public EmergencyReport(String emergencyType, String description, String location,
                               String latitude, String longitude, String address, 
                               String addressStatus, String aiResponse) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.emergencyType = emergencyType;
            this.description = description;
            this.location = location;
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.addressStatus = addressStatus;
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.status = "Sent";
            this.aiResponse = aiResponse;
        }
        
        // Helper methods to get latitude/longitude as String
        public String getLatitudeString() {
            if (latitude == null) return "0.0";
            return latitude.toString();
        }
        
        public String getLongitudeString() {
            if (longitude == null) return "0.0";
            return longitude.toString();
        }

        public Map<String, Object> toMap() {
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("emergencyType", emergencyType);
            result.put("description", description);
            result.put("location", location);
            result.put("latitude", getLatitudeString());
            result.put("longitude", getLongitudeString());
            result.put("address", address != null ? address : "Not provided");
            result.put("addressStatus", addressStatus != null ? addressStatus : "not_provided");
            result.put("timestamp", timestamp);
            result.put("status", status);
            result.put("aiResponse", aiResponse);
            return result;
        }
    }

    public interface ReportsCallback {
        void onSuccess(List<EmergencyReport> reports);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public FirebaseReportManager() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            // Only enable persistence once per app lifecycle
            try {
                database.setPersistenceEnabled(true);
            } catch (com.google.firebase.database.DatabaseException e) {
                // Persistence already enabled, ignore
                Log.d(TAG, "Firebase persistence already enabled");
            }
            databaseReference = database.getReference(REPORTS_PATH);
            Log.d(TAG, "Firebase initialized successfully with path: " + REPORTS_PATH);
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error", e);
            throw new RuntimeException("Firebase initialization failed: " + e.getMessage(), e);
        }
    }

    public void saveReport(EmergencyReport report, SaveCallback callback) {
        Log.d(TAG, "Attempting to save report to Firebase...");
        
        String key = databaseReference.push().getKey();
        if (key == null) {
            String error = "Failed to generate report key";
            Log.e(TAG, error);
            callback.onError(error);
            return;
        }

        report.id = key;
        Map<String, Object> reportData = report.toMap();
        
        Log.d(TAG, "Saving report with key: " + key);
        Log.d(TAG, "Report data: " + reportData.toString());
        
        databaseReference.child(key).setValue(reportData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Report saved successfully to Firebase with ID: " + key);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                String error = e.getMessage() != null ? e.getMessage() : "Unknown error";
                Log.e(TAG, "Error saving report to Firebase: " + error, e);
                callback.onError("Firebase write failed: " + error);
            });
    }

    public void loadReports(ReportsCallback callback) {
        databaseReference.orderByChild("timestamp")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<EmergencyReport> reports = new ArrayList<>();
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        EmergencyReport report = snapshot.getValue(EmergencyReport.class);
                        if (report != null) {
                            reports.add(0, report); // Add to beginning for reverse chronological order
                        }
                    }
                    
                    Log.d(TAG, "Loaded " + reports.size() + " reports from Firebase");
                    callback.onSuccess(reports);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "Error loading reports from Firebase", databaseError.toException());
                    callback.onError(databaseError.getMessage());
                }
            });
    }

    public void updateReportStatus(String reportId, String newStatus, SaveCallback callback) {
        databaseReference.child(reportId).child("status").setValue(newStatus)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Report status updated successfully");
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating report status", e);
                callback.onError(e.getMessage());
            });
    }

    public void updateReport(String reportId, String description, String address, 
                            String addressStatus, String location, String aiResponse, SaveCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("description", description);
        updates.put("address", address != null && !address.isEmpty() ? address : "Not provided");
        updates.put("addressStatus", addressStatus != null ? addressStatus : "not_provided");
        updates.put("location", location);
        updates.put("aiResponse", aiResponse);
        updates.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        
        databaseReference.child(reportId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Report updated successfully: " + reportId);
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error updating report", e);
                callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
            });
    }

    public List<HistoryItem> convertToHistoryItems(List<EmergencyReport> reports) {
        List<HistoryItem> historyItems = new ArrayList<>();
        
        if (reports == null || reports.isEmpty()) {
            Log.d(TAG, "No reports to convert");
            return historyItems;
        }
        
        for (EmergencyReport report : reports) {
            try {
                String timeAgo = getTimeAgo(report.timestamp != null ? report.timestamp : "");
                String location = (report.latitude != null && report.longitude != null) 
                    ? report.getLatitudeString() + "° N, " + report.getLongitudeString() + "° E" 
                    : "Location unavailable";
                
                historyItems.add(new HistoryItem(
                    report.emergencyType != null ? report.emergencyType : "Emergency",
                    timeAgo,
                    location,
                    report.description != null ? report.description : "No description",
                    report.status != null ? report.status : "Sent",
                    report.id,
                    report.aiResponse
                ));
            } catch (Exception e) {
                Log.e(TAG, "Error converting report to history item", e);
            }
        }
        
        return historyItems;
    }

    private String getTimeAgo(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            if (date == null) return timestamp;
            
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 1) {
                return days + " days ago";
            } else if (days == 1) {
                return "Yesterday";
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "Just now";
            }
        } catch (Exception e) {
            return timestamp;
        }
    }
}
