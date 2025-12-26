package com.costumerental.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Reservation;
import com.costumerental.app.utils.NotificationHelper;
import com.costumerental.app.utils.SharedPrefManager;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Admin Dashboard Activity
public class AdminDashboardActivity extends AppCompatActivity {
    
    private Button buttonViewCostumes, buttonAddCostume, buttonViewReservations, buttonLogout;
    private ApiService apiService;
    private NotificationHelper notificationHelper;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 200;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        
        // Initialize views
        buttonViewCostumes = findViewById(R.id.buttonViewCostumes);
        buttonAddCostume = findViewById(R.id.buttonAddCostume);
        buttonViewReservations = findViewById(R.id.buttonViewReservations);
        buttonLogout = findViewById(R.id.buttonLogout);
        
        apiService = ApiClient.getApiService();
        notificationHelper = new NotificationHelper(this);
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        
        // Check for new reservations when activity starts
        checkNewReservations();
        
        // View costumes button
        buttonViewCostumes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, CostumeListActivity.class);
                intent.putExtra("isAdmin", true);
                startActivity(intent);
            }
        });
        
        // Add costume button
        buttonAddCostume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AddCostumeActivity.class);
                startActivity(intent);
            }
        });
        
        // View all reservations button
        buttonViewReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminDashboardActivity.this, AdminReservationsActivity.class);
                startActivity(intent);
            }
        });
        
        // Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefManager.getInstance(AdminDashboardActivity.this).logout();
                Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    
    // Check for new pending reservations
    private void checkNewReservations() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            return; // Not logged in, skip notification check
        }
        
        token = "Bearer " + token;
        
        Call<ApiResponse<List<Reservation>>> call = apiService.getAllReservations(token);
        call.enqueue(new Callback<ApiResponse<List<Reservation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Reservation>> apiResponse = response.body();
                        if (apiResponse.getData() != null) {
                            List<Reservation> reservations = apiResponse.getData();
                            SharedPrefManager prefManager = SharedPrefManager.getInstance(AdminDashboardActivity.this);
                            Set<Integer> seenReservationIds = prefManager.getSeenReservationIds();
                            
                            int newPendingCount = 0;
                            
                            // Check for new pending reservations
                            for (Reservation reservation : reservations) {
                                String status = reservation.getStatus() != null ? reservation.getStatus() : "pending";
                                
                                // Only notify for pending reservations
                                if (status.equals("pending")) {
                                    int reservationId = reservation.getId();
                                    
                                    // Check if this is a new reservation (not seen before)
                                    if (!seenReservationIds.contains(reservationId)) {
                                        newPendingCount++;
                                        // Mark as seen
                                        prefManager.markReservationAsSeen(reservationId);
                                        
                                        // Show notification for individual reservation
                                        notificationHelper.showNewReservationNotification(reservation);
                                    }
                                }
                            }
                            
                            // If there are multiple new reservations, show a summary notification
                            if (newPendingCount > 1) {
                                notificationHelper.showMultipleReservationsNotification(newPendingCount);
                            }
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("AdminDashboard", "Error checking new reservations: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                // Silently fail - notifications are not critical
                android.util.Log.d("AdminDashboard", "Failed to check new reservations: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, check for new reservations
                checkNewReservations();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check for new reservations when activity resumes
        checkNewReservations();
    }
    
}

