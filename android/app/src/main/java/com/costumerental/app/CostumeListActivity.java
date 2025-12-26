package com.costumerental.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.database.DatabaseHelper;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Costume;
import com.costumerental.app.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Costume List Activity - Shows list of costumes
public class CostumeListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private CostumeAdapter adapter;
    private List<Costume> costumeList;
    private boolean isAdmin;
    private ApiService apiService;
    private DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_costume_list);
        
        // Get admin flag
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);
        
        // Initialize
        recyclerView = findViewById(R.id.recyclerView);
        costumeList = new ArrayList<>();
        adapter = new CostumeAdapter(costumeList, isAdmin, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        apiService = ApiClient.getApiService();
        databaseHelper = new DatabaseHelper(this);
        
        // Load costumes
        loadCostumes();
    }
    
    // Load costumes (online or offline)
    private void loadCostumes() {
        if (isNetworkAvailable()) {
            // Load from API
            loadCostumesFromAPI();
        } else {
            // Load from SQLite
            loadCostumesFromLocal();
        }
    }
    
    // Load costumes from API
    private void loadCostumesFromAPI() {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<List<Costume>>> call = apiService.getCostumes(token);
        call.enqueue(new Callback<ApiResponse<List<Costume>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Costume>>> call, Response<ApiResponse<List<Costume>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Costume>> apiResponse = response.body();
                    List<Costume> costumes = null;
                    
                    // Get from data field (Laravel returns data as array)
                    if (apiResponse.getData() != null) {
                        costumes = apiResponse.getData();
                    }
                    
                    if (costumes != null && !costumes.isEmpty()) {
                        costumeList.clear();
                        costumeList.addAll(costumes);
                        adapter.notifyDataSetChanged();
                        
                        // Save to local database
                        databaseHelper.saveCostumes(costumes);
                    }
                } else {
                    // If API fails, try local
                    loadCostumesFromLocal();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Costume>>> call, Throwable t) {
                // If API fails, try local
                loadCostumesFromLocal();
            }
        });
    }
    
    // Load costumes from local database
    private void loadCostumesFromLocal() {
        List<Costume> costumes = databaseHelper.getAllCostumes();
        costumeList.clear();
        costumeList.addAll(costumes);
        adapter.notifyDataSetChanged();
        
        if (costumes.isEmpty()) {
            Toast.makeText(this, "No costumes available offline", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Loaded from offline storage", Toast.LENGTH_SHORT).show();
        }
    }
    
    // Check network availability
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    // Delete costume (admin only)
    public void deleteCostume(int costumeId) {
        String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
        
        Call<ApiResponse<Void>> call = apiService.deleteCostume(token, costumeId);
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CostumeListActivity.this, "Costume deleted", Toast.LENGTH_SHORT).show();
                    loadCostumes(); // Reload list
                } else {
                    Toast.makeText(CostumeListActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(CostumeListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Reserve costume (client only)
    public void reserveCostume(int costumeId, boolean isAvailable, String nextAvailableDate) {
        Intent intent = new Intent(this, ReservationActivity.class);
        intent.putExtra("costume_id", costumeId);
        intent.putExtra("is_available", isAvailable);
        if (nextAvailableDate != null) {
            intent.putExtra("next_available_date", nextAvailableDate);
        }
        startActivity(intent);
    }
}
