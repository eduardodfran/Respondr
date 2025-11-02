package com.example.respondr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerViewHistory;
    private LinearLayout emptyState;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize views
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        emptyState = view.findViewById(R.id.emptyState);

        // Setup RecyclerView
        historyItems = new ArrayList<>();
        adapter = new HistoryAdapter(historyItems);
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHistory.setAdapter(adapter);

        // Load data
        loadHistory();

        return view;
    }

    private void loadHistory() {
        // Sample data - replace with Firebase later
        historyItems.add(new HistoryItem(
                "Medical Emergency",
                "2 hours ago",
                "14.5995° N, 120.9842° E",
                "Patient experiencing chest pain and difficulty breathing",
                "Sent"
        ));

        historyItems.add(new HistoryItem(
                "Fire Emergency",
                "Yesterday",
                "14.6042° N, 121.0017° E",
                "Small fire in residential building, residents evacuated",
                "Resolved"
        ));

        historyItems.add(new HistoryItem(
                "Police Emergency",
                "3 days ago",
                "14.5547° N, 121.0244° E",
                "Two-vehicle collision, minor injuries reported",
                "Resolved"
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
