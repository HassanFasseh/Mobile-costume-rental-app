package com.costumerental.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Costume;
import com.costumerental.app.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Add Costume Activity (Admin only)
public class AddCostumeActivity extends AppCompatActivity {
    
    private EditText editTextName, editTextSize, editTextPrice;
    private Button buttonSave;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_costume);
        
        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextSize = findViewById(R.id.editTextSize);
        editTextPrice = findViewById(R.id.editTextPrice);
        buttonSave = findViewById(R.id.buttonSave);
        
        apiService = ApiClient.getApiService();
        
        // Save button click
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCostume();
            }
        });
    }
    
    // Add costume method
    private void addCostume() {
        String name = editTextName.getText().toString().trim();
        String size = editTextSize.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        
        if (name.isEmpty() || size.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            
            Map<String, Object> costumeData = new HashMap<>();
            costumeData.put("name", name);
            costumeData.put("size", size);
            costumeData.put("price", price);
            costumeData.put("image", "default.jpg");
            
            String token = "Bearer " + SharedPrefManager.getInstance(this).getToken();
            
            Call<ApiResponse<Costume>> call = apiService.addCostume(token, costumeData);
            call.enqueue(new Callback<ApiResponse<Costume>>() {
                @Override
                public void onResponse(Call<ApiResponse<Costume>> call, Response<ApiResponse<Costume>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Costume> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(AddCostumeActivity.this, "Costume added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddCostumeActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddCostumeActivity.this, "Failed to add costume", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<Costume>> call, Throwable t) {
                    Toast.makeText(AddCostumeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
        }
    }
}

