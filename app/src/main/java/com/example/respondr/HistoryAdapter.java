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

    private List<HistoryItem> historyItems;

    public HistoryAdapter(List<HistoryItem> historyItems) {
        this.historyItems = historyItems;
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
        HistoryItem item = historyItems.get(position);
        
        holder.tvEmergencyType.setText(item.getEmergencyType());
        holder.tvTimestamp.setText(item.getTimestamp());
        holder.tvLocation.setText(item.getLocation());
        holder.tvDescription.setText(item.getDescription());
        holder.tvStatus.setText(item.getStatus());
        
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
        if (type.contains("Medical")) {
            holder.tvEmergencyIcon.setText("🚑");
        } else if (type.contains("Fire")) {
            holder.tvEmergencyIcon.setText("🔥");
        } else if (type.contains("Police") || type.contains("Traffic")) {
            holder.tvEmergencyIcon.setText("🚓");
        } else {
            holder.tvEmergencyIcon.setText("🚨");
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
