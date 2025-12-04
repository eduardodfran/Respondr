package com.example.respondr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Switch switchNotifications;
    private Switch switchAutoSend;
    private Switch switchLocationTracking;
    private TextView tvAppVersion;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SharedPreferences
        prefs = requireContext().getSharedPreferences("RespondrSettings", requireContext().MODE_PRIVATE);

        // Initialize views
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchAutoSend = view.findViewById(R.id.switchAutoSend);
        switchLocationTracking = view.findViewById(R.id.switchLocationTracking);
        tvAppVersion = view.findViewById(R.id.tvAppVersion);

        // Load saved preferences
        loadSettings();

        // Set up listeners
        setupListeners();

        // Set app version
        tvAppVersion.setText("Version 1.0.0");

        return view;
    }

    private void loadSettings() {
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchAutoSend.setChecked(prefs.getBoolean("auto_send_location", true));
        switchLocationTracking.setChecked(prefs.getBoolean("location_tracking", true));
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Notifications enabled" : "Notifications disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchAutoSend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_send_location", isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Auto-send location enabled" : "Auto-send location disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchLocationTracking.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("location_tracking", isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Location tracking enabled" : "Location tracking disabled", 
                Toast.LENGTH_SHORT).show();
        });
    }
}
