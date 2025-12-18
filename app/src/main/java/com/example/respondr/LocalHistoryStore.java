package com.example.respondr;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocalHistoryStore extends SQLiteOpenHelper {

    private static final String DB_NAME = "respondr_history.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_REPORTS = "reports";

    private static final String COL_ID = "id";
    private static final String COL_EMERGENCY_TYPE = "emergencyType";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_LOCATION = "location";
    private static final String COL_LATITUDE = "latitude";
    private static final String COL_LONGITUDE = "longitude";
    private static final String COL_ADDRESS = "address";
    private static final String COL_ADDRESS_STATUS = "addressStatus";
    private static final String COL_STATUS = "status";
    private static final String COL_AI_RESPONSE = "aiResponse";
    private static final String COL_TIMESTAMP_MS = "timestampMs";

    public static class LocalReport {
        public String id;
        public String emergencyType;
        public String description;
        public String location;
        public String latitude;
        public String longitude;
        public String address;
        public String addressStatus;
        public String status;
        public String aiResponse;
        public long timestampMs;
    }

    public LocalHistoryStore(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_REPORTS + " (" +
                COL_ID + " TEXT PRIMARY KEY," +
                COL_EMERGENCY_TYPE + " TEXT," +
                COL_DESCRIPTION + " TEXT," +
                COL_LOCATION + " TEXT," +
                COL_LATITUDE + " TEXT," +
                COL_LONGITUDE + " TEXT," +
                COL_ADDRESS + " TEXT," +
                COL_ADDRESS_STATUS + " TEXT," +
                COL_STATUS + " TEXT," +
                COL_AI_RESPONSE + " TEXT," +
                COL_TIMESTAMP_MS + " INTEGER" +
            ")"
        );

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_reports_ts ON " + TABLE_REPORTS + "(" + COL_TIMESTAMP_MS + " DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For MVP: destructive migrations
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        onCreate(db);
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REPORTS, null, null);
    }

    public void upsert(LocalReport report) {
        if (report == null || report.id == null) return;

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, report.id);
        values.put(COL_EMERGENCY_TYPE, report.emergencyType);
        values.put(COL_DESCRIPTION, report.description);
        values.put(COL_LOCATION, report.location);
        values.put(COL_LATITUDE, report.latitude);
        values.put(COL_LONGITUDE, report.longitude);
        values.put(COL_ADDRESS, report.address);
        values.put(COL_ADDRESS_STATUS, report.addressStatus);
        values.put(COL_STATUS, report.status);
        values.put(COL_AI_RESPONSE, report.aiResponse);
        values.put(COL_TIMESTAMP_MS, report.timestampMs);

        db.insertWithOnConflict(TABLE_REPORTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public LocalReport getById(String id) {
        if (id == null) return null;

        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(
            TABLE_REPORTS,
            null,
            COL_ID + "=?",
            new String[]{ id },
            null,
            null,
            null,
            "1"
        )) {
            if (!c.moveToFirst()) return null;
            return readReport(c);
        }
    }

    public List<HistoryItem> getHistoryItems() {
        SQLiteDatabase db = getReadableDatabase();
        List<HistoryItem> items = new ArrayList<>();

        try (Cursor c = db.query(
            TABLE_REPORTS,
            new String[]{ COL_ID, COL_EMERGENCY_TYPE, COL_DESCRIPTION, COL_LOCATION, COL_STATUS, COL_AI_RESPONSE, COL_TIMESTAMP_MS },
            null,
            null,
            null,
            null,
            COL_TIMESTAMP_MS + " DESC"
        )) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String type = c.getString(1);
                String description = c.getString(2);
                String location = c.getString(3);
                String status = c.getString(4);
                String aiResponse = c.getString(5);
                long ts = c.getLong(6);

                items.add(new HistoryItem(
                    type != null ? type : "Emergency",
                    TimeAgo.format(ts),
                    location != null ? location : "Location unavailable",
                    description != null ? description : "No description",
                    status != null ? status : "Sent",
                    id,
                    aiResponse != null ? aiResponse : ""
                ));
            }
        }

        return items;
    }

    private LocalReport readReport(Cursor c) {
        LocalReport r = new LocalReport();
        r.id = c.getString(c.getColumnIndexOrThrow(COL_ID));
        r.emergencyType = getString(c, COL_EMERGENCY_TYPE);
        r.description = getString(c, COL_DESCRIPTION);
        r.location = getString(c, COL_LOCATION);
        r.latitude = getString(c, COL_LATITUDE);
        r.longitude = getString(c, COL_LONGITUDE);
        r.address = getString(c, COL_ADDRESS);
        r.addressStatus = getString(c, COL_ADDRESS_STATUS);
        r.status = getString(c, COL_STATUS);
        r.aiResponse = getString(c, COL_AI_RESPONSE);
        r.timestampMs = c.getLong(c.getColumnIndexOrThrow(COL_TIMESTAMP_MS));
        return r;
    }

    private static String getString(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        return idx >= 0 ? c.getString(idx) : null;
    }
}
