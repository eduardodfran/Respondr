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

    public static final String KEY_SAVE_HISTORY_ENABLED = AppPreferences.KEY_SAVE_HISTORY_ENABLED;

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
        prefs = requireContext().getSharedPreferences(AppPreferences.PREFS_NAME, requireContext().MODE_PRIVATE);

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
        switchNotifications.setChecked(prefs.getBoolean(AppPreferences.KEY_NOTIFICATIONS_ENABLED, true));
        switchAutoSend.setChecked(prefs.getBoolean(AppPreferences.KEY_AUTO_SEND_LOCATION, true));
        switchSaveHistory.setChecked(prefs.getBoolean(AppPreferences.KEY_SAVE_HISTORY_ENABLED, true));
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(AppPreferences.KEY_NOTIFICATIONS_ENABLED, isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Notifications enabled" : "Notifications disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchAutoSend.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(AppPreferences.KEY_AUTO_SEND_LOCATION, isChecked).apply();
            Toast.makeText(requireContext(), 
                isChecked ? "Auto-send location enabled" : "Auto-send location disabled", 
                Toast.LENGTH_SHORT).show();
        });

        switchSaveHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(AppPreferences.KEY_SAVE_HISTORY_ENABLED, isChecked).apply();
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
        btnClearHistory.setEnabled(false);
        try {
            new LocalHistoryStore(requireContext()).clearAll();
            // Also clear any legacy local JSON cache if present
            try {
                new ReportManager(requireContext()).clearAllReports();
            } catch (Exception ignored) {
                // Best-effort
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                btnClearHistory.setEnabled(true);
                Toast.makeText(requireContext(), "History cleared", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                btnClearHistory.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to clear history", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
