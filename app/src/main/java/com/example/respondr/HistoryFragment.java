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
    private FirebaseReportManager firebaseReportManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Initialize views
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        emptyState = view.findViewById(R.id.emptyState);

        // Initialize FirebaseReportManager
        firebaseReportManager = new FirebaseReportManager();

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
        // Load from Firebase
        firebaseReportManager.loadReports(new FirebaseReportManager.ReportsCallback() {
            @Override
            public void onSuccess(List<FirebaseReportManager.EmergencyReport> reports) {
                if (!isAdded()) return;
                
                requireActivity().runOnUiThread(() -> {
                    historyItems.clear();
                    historyItems.addAll(firebaseReportManager.convertToHistoryItems(reports));
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                
                requireActivity().runOnUiThread(() -> {
                    android.widget.Toast.makeText(requireContext(), 
                        "Error loading history: " + error, 
                        android.widget.Toast.LENGTH_SHORT).show();
                    updateUI();
                });
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh history when fragment becomes visible
        loadHistory();
    }
}
