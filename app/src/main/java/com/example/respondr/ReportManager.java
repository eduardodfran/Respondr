package com.example.respondr;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportManager {
    private static final String TAG = "ReportManager";
    private static final String REPORTS_FILE = "emergency_reports.json";
    private final Context context;
    private final Gson gson;

    public static class ReportData {
        public List<EmergencyReport> reports;

        public ReportData() {
            this.reports = new ArrayList<>();
        }
    }

    public static class EmergencyReport {
        public String id;
        public String emergencyType;
        public String description;
        public String location;
        public String latitude;
        public String longitude;
        public String timestamp;
        public String status;
        public String aiResponse;

        public EmergencyReport(String emergencyType, String description, String location,
                               String latitude, String longitude, String aiResponse) {
            this.id = String.valueOf(System.currentTimeMillis());
            this.emergencyType = emergencyType;
            this.description = description;
            this.location = location;
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            this.status = "Sent";
            this.aiResponse = aiResponse;
        }
    }

    public ReportManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private File getReportsFile() {
        return new File(context.getFilesDir(), REPORTS_FILE);
    }

    public void saveReport(EmergencyReport report) {
        try {
            ReportData data = loadReports();
            data.reports.add(0, report); // Add to beginning of list
            
            String json = gson.toJson(data);
            
            FileOutputStream fos = context.openFileOutput(REPORTS_FILE, Context.MODE_PRIVATE);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            writer.write(json);
            writer.close();
            fos.close();
            
            Log.d(TAG, "Report saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving report", e);
        }
    }

    public ReportData loadReports() {
        File file = getReportsFile();
        
        try {
            InputStream inputStream;
            
            if (file.exists()) {
                // Read from internal storage
                inputStream = new FileInputStream(file);
            } else {
                // First time - read from assets
                inputStream = context.getAssets().open(REPORTS_FILE);
            }
            
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            Type type = new TypeToken<ReportData>(){}.getType();
            ReportData data = gson.fromJson(json, type);
            
            return data != null ? data : new ReportData();
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading reports", e);
            return new ReportData();
        }
    }

    public List<HistoryItem> getHistoryItems() {
        ReportData data = loadReports();
        List<HistoryItem> historyItems = new ArrayList<>();
        
        for (EmergencyReport report : data.reports) {
            String timeAgo = getTimeAgo(report.timestamp);
            String location = report.latitude + "° N, " + report.longitude + "° E";
            
            historyItems.add(new HistoryItem(
                report.emergencyType,
                timeAgo,
                location,
                report.description,
                report.status,
                report.id != null ? report.id : "",
                report.aiResponse != null ? report.aiResponse : ""
            ));
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

    public void clearAllReports() {
        File file = getReportsFile();
        if (file.exists()) {
            file.delete();
        }
    }
}
