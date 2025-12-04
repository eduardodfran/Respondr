package com.example.respondr;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HistoryItem item);
    }

    private List<HistoryItem> historyItems;
    private OnItemClickListener listener;

    public HistoryAdapter(List<HistoryItem> historyItems, OnItemClickListener listener) {
        this.historyItems = historyItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        try {
            HistoryItem item = historyItems.get(position);
            
            if (item == null) return;
            
            // Set click listener only if status is not "Resolved"
            if (!"Resolved".equals(item.getStatus())) {
                holder.itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                });
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.itemView.setOnClickListener(null);
                holder.itemView.setAlpha(0.6f); // Dim resolved items
            }
            
            holder.tvEmergencyType.setText(item.getEmergencyType() != null ? item.getEmergencyType() : "Emergency");
            holder.tvTimestamp.setText(item.getTimestamp() != null ? item.getTimestamp() : "Unknown time");
            holder.tvLocation.setText(item.getLocation() != null ? item.getLocation() : "Location unavailable");
            holder.tvDescription.setText(item.getDescription() != null ? item.getDescription() : "No description");
            holder.tvStatus.setText(item.getStatus() != null ? item.getStatus() : "Sent");
            
            // Set status badge color based on status
            String status = item.getStatus();
            if ("Sent".equals(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.status_badge_sent);
            } else if ("Resolved".equals(status)) {
                holder.tvStatus.setBackgroundResource(R.drawable.status_badge_resolved);
            } else {
                holder.tvStatus.setBackgroundResource(R.drawable.status_badge);
            }
            
            // Set emoji based on emergency type
            String type = item.getEmergencyType();
            if (type != null) {
                if (type.contains("MEDICAL") || type.contains("Medical")) {
                    holder.tvEmergencyIcon.setText("🚑");
                } else if (type.contains("FIRE") || type.contains("Fire")) {
                    holder.tvEmergencyIcon.setText("🔥");
                } else if (type.contains("POLICE") || type.contains("Police")) {
                    holder.tvEmergencyIcon.setText("🚓");
                } else {
                    holder.tvEmergencyIcon.setText("🚨");
                }
            } else {
                holder.tvEmergencyIcon.setText("🚨");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmergencyIcon, tvEmergencyType, tvTimestamp, tvLocation, tvDescription, tvStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmergencyIcon = itemView.findViewById(R.id.tvEmergencyIcon);
            tvEmergencyType = itemView.findViewById(R.id.tvEmergencyType);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
