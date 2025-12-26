package com.costumerental.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Reservation;
import com.costumerental.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Admin Reservations Activity - Browse and manage reservations
public class AdminReservationsActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private List<Reservation> reservationList;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reservations);
        
        // Initialize
        recyclerView = findViewById(R.id.recyclerView);
        reservationList = new ArrayList<>();
        adapter = new ReservationAdapter(reservationList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        apiService = ApiClient.getApiService();
        
        // Load reservations
        loadReservations();
    }
    
    // Load all reservations
    private void loadReservations() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<List<Reservation>>> call = apiService.getAllReservations(token);
        call.enqueue(new Callback<ApiResponse<List<Reservation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Reservation>>> call, Response<ApiResponse<List<Reservation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Reservation>> apiResponse = response.body();
                    if (apiResponse.getData() != null) {
                        reservationList.clear();
                        reservationList.addAll(apiResponse.getData());
                        adapter.notifyDataSetChanged();
                        
                        if (reservationList.isEmpty()) {
                            Toast.makeText(AdminReservationsActivity.this, "No reservations found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(AdminReservationsActivity.this, "Failed to load reservations", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Reservation>>> call, Throwable t) {
                Toast.makeText(AdminReservationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Approve reservation
    public void approveReservation(int reservationId) {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<Reservation>> call = apiService.approveReservation(token, reservationId);
        call.enqueue(new Callback<ApiResponse<Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Reservation> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminReservationsActivity.this, "Reservation approved", Toast.LENGTH_SHORT).show();
                        loadReservations(); // Refresh list
                    } else {
                        Toast.makeText(AdminReservationsActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Try to parse error message
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("message")) {
                                // Simple JSON parsing for error message
                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                int end = errorBody.indexOf("\"", start);
                                if (start > 10 && end > start) {
                                    String errorMsg = errorBody.substring(start, end);
                                    Toast.makeText(AdminReservationsActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                    Toast.makeText(AdminReservationsActivity.this, "Failed to approve reservation", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                Toast.makeText(AdminReservationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Reject reservation
    public void rejectReservation(int reservationId) {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<Reservation>> call = apiService.rejectReservation(token, reservationId);
        call.enqueue(new Callback<ApiResponse<Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Reservation> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AdminReservationsActivity.this, "Reservation rejected", Toast.LENGTH_SHORT).show();
                        loadReservations(); // Refresh list
                    } else {
                        Toast.makeText(AdminReservationsActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminReservationsActivity.this, "Failed to reject reservation", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                Toast.makeText(AdminReservationsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

