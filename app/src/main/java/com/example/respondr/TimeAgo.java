package com.example.respondr;

import java.util.concurrent.TimeUnit;

public final class TimeAgo {

    private TimeAgo() {}

    public static String format(long timestampMs) {
        if (timestampMs <= 0) return "Unknown time";

        long diff = System.currentTimeMillis() - timestampMs;
        if (diff < 0) diff = 0;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days > 1) return days + " days ago";
        if (days == 1) return "Yesterday";
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (minutes > 0) return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        return "Just now";
    }
}
