package com.example.respondr;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;
    private LinearLayout emptyState;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);
        emptyState = findViewById(R.id.emptyState);
        ImageView btnBack = findViewById(R.id.btnBack);

        // Setup RecyclerView
        historyItems = new ArrayList<>();
        adapter = new HistoryAdapter(historyItems, item -> {
            // Handle click - could navigate to detail view if needed
            if ("Resolved".equals(item.getStatus())) {
                android.widget.Toast.makeText(this, 
                    "This emergency has been resolved", 
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Load data
        loadHistory();
    }

    private void loadHistory() {
        // Sample data - replace with Firebase later
        historyItems.add(new HistoryItem(
                "Medical Emergency",
                "2 hours ago",
                "14.5995° N, 120.9842° E",
                "Patient experiencing chest pain and difficulty breathing",
                "Sent",
                "sample_id_1",
                "Emergency services have been notified."
        ));

        historyItems.add(new HistoryItem(
                "Fire Emergency",
                "Yesterday",
                "14.6042° N, 121.0017° E",
                "Small fire in residential building, residents evacuated",
                "Resolved",
                "sample_id_2",
                "Fire department responded successfully."
        ));

        historyItems.add(new HistoryItem(
                "Police Emergency",
                "3 days ago",
                "14.5547° N, 121.0244° E",
                "Two-vehicle collision, minor injuries reported",
                "Resolved",
                "sample_id_3",
                "Police arrived and filed incident report."
        ));

        updateUI();
    }

    private void updateUI() {
        if (historyItems.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerViewHistory.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerViewHistory.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }
}
