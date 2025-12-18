package com.example.respondr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "RespondrSettings";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_AUTO_SEND_LOCATION = "auto_send_location";
    public static final String KEY_SAVE_HISTORY_ENABLED = "save_history_enabled";

    private Switch switchNotifications;
    private Switch switchAutoSend;
    private Switch switchSaveHistory;
    private Button btnClearHistory;
    private TextView tvAppVersion;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SharedPreferences
        prefs = requireContext().getSharedPreferences(PREFS_NAME, requireContext().MODE_PRIVATE);

        // Initialize views
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchAutoSend = view.findViewById(R.id.switchAutoSend);
        switchSaveHistory = view.findViewById(R.id.switchSaveHistory);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
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
        switchNotifications.setChecked(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true));
        switchAutoSend.setChecked(prefs.getBoolean(KEY_AUTO_SEND_LOCATION, true));
        switchSaveHistory.setChecked(prefs.getBoolean(KEY_SAVE_HISTORY_ENABLED, true));
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Notifications enabled" : "Notifications disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchAutoSend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_AUTO_SEND_LOCATION, isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Auto-send location enabled" : "Auto-send location disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchSaveHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_SAVE_HISTORY_ENABLED, isChecked).apply();
            Toast.makeText(requireContext(),
                isChecked ? "History saving enabled" : "History saving disabled",
                Toast.LENGTH_SHORT).show();
        });

        btnClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                .setTitle("Clear history?")
                .setMessage("This will delete your saved history for this device.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Clear", (dialog, which) -> clearHistory())
                .show();
        });
    }

    private void clearHistory() {
        FirebaseReportManager manager = new FirebaseReportManager(requireContext());
        btnClearHistory.setEnabled(false);
        manager.clearAllReports(new FirebaseReportManager.SaveCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnClearHistory.setEnabled(true);
                    Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    btnClearHistory.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to clear history", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
