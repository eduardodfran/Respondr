package com.example.respondr;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPreferences {

    private AppPreferences() {}

    public static final String PREFS_NAME = "RespondrSettings";

    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_AUTO_SEND_LOCATION = "auto_send_location";
    public static final String KEY_SAVE_HISTORY_ENABLED = "save_history_enabled";

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isNotificationsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public static boolean isAutoSendLocationEnabled(Context context) {
        return prefs(context).getBoolean(KEY_AUTO_SEND_LOCATION, true);
    }

    public static boolean isSaveHistoryEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SAVE_HISTORY_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public static void setAutoSendLocationEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_AUTO_SEND_LOCATION, enabled).apply();
    }

    public static void setSaveHistoryEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_SAVE_HISTORY_ENABLED, enabled).apply();
    }
}
