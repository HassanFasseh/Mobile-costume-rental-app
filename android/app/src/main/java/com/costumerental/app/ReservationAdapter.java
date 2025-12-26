package com.costumerental.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.costumerental.app.models.Reservation;

import java.util.List;

// RecyclerView adapter for reservations
public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    
    private List<Reservation> reservations;
    private AdminReservationsActivity activity;
    
    public ReservationAdapter(List<Reservation> reservations, AdminReservationsActivity activity) {
        this.reservations = reservations;
        this.activity = activity;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        
        // Set costume name
        if (reservation.getCostume() != null) {
            holder.textViewCostumeName.setText(reservation.getCostume().getName());
        } else {
            holder.textViewCostumeName.setText("Costume #" + reservation.getCostume_id());
        }
        
        // Set user name
        if (reservation.getUser() != null) {
            holder.textViewUserName.setText("User: " + reservation.getUser().getName());
        } else {
            holder.textViewUserName.setText("User ID: " + reservation.getUser_id());
        }
        
        // Set dates
        holder.textViewDates.setText("Dates: " + reservation.getStart_date() + " to " + reservation.getEnd_date());
        
        // Set status
        String status = reservation.getStatus() != null ? reservation.getStatus() : "pending";
        String statusText = status.substring(0, 1).toUpperCase() + status.substring(1);
        holder.textViewStatus.setText("Status: " + statusText);
        
        // Set status color
        int statusColor;
        if (status.equals("approved")) {
            statusColor = 0xFF4CAF50; // Green
        } else if (status.equals("rejected")) {
            statusColor = 0xFFF44336; // Red
        } else {
            statusColor = 0xFFFF9800; // Orange
        }
        holder.textViewStatus.setTextColor(statusColor);
        
        // Enable/disable buttons based on status
        boolean isPending = status.equals("pending");
        holder.buttonApprove.setEnabled(isPending);
        holder.buttonReject.setEnabled(isPending);
        
        // Set click listeners
        holder.buttonApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.approveReservation(reservation.getId());
            }
        });
        
        holder.buttonReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.rejectReservation(reservation.getId());
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return reservations.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCostumeName, textViewUserName, textViewDates, textViewStatus;
        Button buttonApprove, buttonReject;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCostumeName = itemView.findViewById(R.id.textViewCostumeName);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            textViewDates = itemView.findViewById(R.id.textViewDates);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            buttonApprove = itemView.findViewById(R.id.buttonApprove);
            buttonReject = itemView.findViewById(R.id.buttonReject);
        }
    }
}
