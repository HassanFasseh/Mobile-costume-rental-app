package com.costumerental.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.costumerental.app.models.Costume;

import java.util.List;

// RecyclerView adapter for costumes
public class CostumeAdapter extends RecyclerView.Adapter<CostumeAdapter.ViewHolder> {
    
    private List<Costume> costumes;
    private boolean isAdmin;
    private CostumeListActivity activity;
    
    public CostumeAdapter(List<Costume> costumes, boolean isAdmin, CostumeListActivity activity) {
        this.costumes = costumes;
        this.isAdmin = isAdmin;
        this.activity = activity;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_costume, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Costume costume = costumes.get(position);
        holder.textViewName.setText(costume.getName());
        holder.textViewSize.setText("Size: " + costume.getSize());
        holder.textViewPrice.setText("Price: $" + costume.getPrice());
        
        // Display availability information
        boolean isAvailable = costume.isIs_available();
        String nextAvailableDate = costume.getNext_available_date();
        
        if (isAvailable) {
            holder.textViewAvailability.setText("✓ Available Now");
            holder.textViewAvailability.setTextColor(0xFF4CAF50); // Green
        } else if (nextAvailableDate != null && !nextAvailableDate.isEmpty()) {
            holder.textViewAvailability.setText("✗ Not Available Until: " + nextAvailableDate);
            holder.textViewAvailability.setTextColor(0xFFF44336); // Red
        } else {
            holder.textViewAvailability.setText("✗ Not Available");
            holder.textViewAvailability.setTextColor(0xFFF44336); // Red
        }
        
        if (isAdmin) {
            holder.buttonAction.setText("Delete");
            holder.buttonAction.setEnabled(true);
            holder.buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.deleteCostume(costume.getId());
                }
            });
        } else {
            holder.buttonAction.setText("Reserve");
            // Always enable reserve button - user can reserve for future dates
            holder.buttonAction.setEnabled(true);
            holder.buttonAction.setAlpha(1.0f);
            holder.buttonAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Pass availability info to reservation activity
                    activity.reserveCostume(costume.getId(), isAvailable, nextAvailableDate);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return costumes.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewSize, textViewPrice, textViewAvailability;
        Button buttonAction;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewSize = itemView.findViewById(R.id.textViewSize);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewAvailability = itemView.findViewById(R.id.textViewAvailability);
            buttonAction = itemView.findViewById(R.id.buttonAction);
        }
    }
}
