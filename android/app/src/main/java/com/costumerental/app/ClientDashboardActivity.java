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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Client Dashboard Activity
public class ClientDashboardActivity extends AppCompatActivity {
    
    private Button buttonViewCostumes, buttonMyReservations, buttonLogout;
    private ApiService apiService;
    private NotificationHelper notificationHelper;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);
        
        // Initialize views
        buttonViewCostumes = findViewById(R.id.buttonViewCostumes);
        buttonMyReservations = findViewById(R.id.buttonMyReservations);
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
        
        // Check for upcoming deadlines when activity starts
        checkUpcomingDeadlines();
        
        // View costumes button
        buttonViewCostumes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientDashboardActivity.this, CostumeListActivity.class);
                intent.putExtra("isAdmin", false);
                startActivity(intent);
            }
        });
        
        // My reservations button
        buttonMyReservations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMyReservations();
            }
        });
        
        // Logout button
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefManager.getInstance(ClientDashboardActivity.this).logout();
                Intent intent = new Intent(ClientDashboardActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    
    // View my reservations
    private void viewMyReservations() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<List<Reservation>>> call = apiService.getMyReservations(token);
        call.enqueue(new Callback<ApiResponse<List<Reservation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Reservation>> apiResponse = response.body();
                    List<Reservation> reservations = null;
                    
                    if (apiResponse.getData() != null) {
                        reservations = apiResponse.getData();
                    }
                    
                    if (reservations != null && !reservations.isEmpty()) {
                        StringBuilder message = new StringBuilder("You have " + reservations.size() + " reservation(s):\n");
                        SharedPrefManager prefManager = SharedPrefManager.getInstance(ClientDashboardActivity.this);
                        
                        for (Reservation res : reservations) {
                            String status = res.getStatus() != null ? res.getStatus() : "pending";
                            String statusText = status.substring(0, 1).toUpperCase() + status.substring(1);
                            message.append("- ").append(res.getCostume() != null ? res.getCostume().getName() : "Costume")
                                   .append(" (").append(statusText).append(")\n");
                            
                            // Check for status changes
                            String lastStatus = prefManager.getReservationStatus(res.getId());
                            if (lastStatus != null && !lastStatus.equals(status)) {
                                // Status changed - show notification
                                notificationHelper.showStatusChangeNotification(res);
                            }
                            
                            // Save current status
                            prefManager.saveReservationStatus(res.getId(), status);
                        }
                        Toast.makeText(ClientDashboardActivity.this, message.toString(), Toast.LENGTH_LONG).show();
                        
                        // Check for upcoming deadlines
                        notificationHelper.checkUpcomingDeadlines(reservations);
                    } else {
                        Toast.makeText(ClientDashboardActivity.this, "No reservations found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ClientDashboardActivity.this, "Failed to load reservations", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                Toast.makeText(ClientDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Check for upcoming deadlines
    private void checkUpcomingDeadlines() {
        String token = SharedPrefManager.getInstance(this).getToken();
        if (token == null || token.isEmpty()) {
            return; // Not logged in, skip notification check
        }
        
        token = "Bearer " + token;
        
        Call<ApiResponse<List<Reservation>>> call = apiService.getMyReservations(token);
        call.enqueue(new Callback<ApiResponse<List<Reservation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Reservation>> apiResponse = response.body();
                        if (apiResponse.getData() != null) {
                            List<Reservation> reservations = apiResponse.getData();
                            SharedPrefManager prefManager = SharedPrefManager.getInstance(ClientDashboardActivity.this);
                            
                            // Check for status changes
                            for (Reservation res : reservations) {
                                try {
                                    String currentStatus = res.getStatus() != null ? res.getStatus() : "pending";
                                    String lastStatus = prefManager.getReservationStatus(res.getId());
                                    if (lastStatus != null && !lastStatus.equals(currentStatus)) {
                                        // Status changed - show notification
                                        notificationHelper.showStatusChangeNotification(res);
                                    }
                                    // Save current status
                                    prefManager.saveReservationStatus(res.getId(), currentStatus);
                                } catch (Exception e) {
                                    // Skip this reservation if there's an error
                                    android.util.Log.e("ClientDashboard", "Error processing reservation: " + e.getMessage());
                                }
                            }
                            
                            // Check for upcoming deadlines
                            notificationHelper.checkUpcomingDeadlines(reservations);
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ClientDashboard", "Error checking deadlines: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                // Silently fail - notifications are not critical
                android.util.Log.d("ClientDashboard", "Failed to check deadlines: " + t.getMessage());
            }
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, check for deadlines
                checkUpcomingDeadlines();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Check for deadlines when activity resumes
        checkUpcomingDeadlines();
    }
}

