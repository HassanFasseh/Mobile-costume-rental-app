package com.costumerental.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.User;
import com.costumerental.app.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Login Activity - First screen
public class LoginActivity extends AppCompatActivity {
    
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Check if already logged in
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            redirectToDashboard();
            return;
        }
        
        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        
        apiService = ApiClient.getApiService();
        
        // Login button click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        
        // Register button click
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }
    
    // Login method
    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);
        
        // Show loading
        buttonLogin.setEnabled(false);
        buttonLogin.setText("Logging in...");
        
        Call<ApiResponse<User>> call = apiService.login(credentials);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Login");
                
                android.util.Log.d("LoginActivity", "Response code: " + response.code());
                android.util.Log.d("LoginActivity", "Response successful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    android.util.Log.d("LoginActivity", "Success: " + apiResponse.isSuccess());
                    android.util.Log.d("LoginActivity", "Token: " + (apiResponse.getToken() != null ? "present" : "null"));
                    android.util.Log.d("LoginActivity", "User: " + (apiResponse.getUser() != null ? "present" : "null"));
                    
                    if (apiResponse.isSuccess()) {
                        // Check if user and token are present
                        if (apiResponse.getUser() == null || apiResponse.getToken() == null) {
                            Toast.makeText(LoginActivity.this, "Invalid response: missing user or token", Toast.LENGTH_LONG).show();
                            android.util.Log.e("LoginActivity", "User: " + apiResponse.getUser() + ", Token: " + apiResponse.getToken());
                            
                            // Try to log raw response
                            try {
                                if (response.raw() != null && response.raw().body() != null) {
                                    android.util.Log.e("LoginActivity", "Raw response body exists");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("LoginActivity", "Error reading raw response: " + e.getMessage());
                            }
                            return;
                        }
                        
                        // Save user session
                        SharedPrefManager.getInstance(LoginActivity.this)
                                .saveUser(apiResponse.getUser(), apiResponse.getToken());
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        redirectToDashboard();
                    } else {
                        String errorMsg = apiResponse.getMessage();
                        if (errorMsg == null || errorMsg.isEmpty()) {
                            errorMsg = "Login failed";
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Try to parse error response
                    String errorMessage = "Login failed (Code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(response.errorBody().byteStream(), "UTF-8"));
                            StringBuilder errorJson = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                errorJson.append(line);
                            }
                            reader.close();
                            
                            // Try to parse as ApiResponse
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            ApiResponse<?> errorApiResponse = gson.fromJson(errorJson.toString(), ApiResponse.class);
                            if (errorApiResponse != null && errorApiResponse.getMessage() != null) {
                                errorMessage = errorApiResponse.getMessage();
                            } else {
                                // Try to parse Laravel validation errors
                                try {
                                    com.google.gson.JsonObject jsonObject = gson.fromJson(errorJson.toString(), com.google.gson.JsonObject.class);
                                    if (jsonObject.has("message")) {
                                        errorMessage = jsonObject.get("message").getAsString();
                                    }
                                } catch (Exception e2) {
                                    // If parsing fails, show raw error
                                    errorMessage = "Error: " + errorJson.toString().substring(0, Math.min(100, errorJson.length()));
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("LoginActivity", "Error parsing response: " + e.getMessage());
                    }
                    
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    android.util.Log.e("LoginActivity", "Login failed: " + errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                buttonLogin.setEnabled(true);
                buttonLogin.setText("Login");
                
                String errorMsg = "Network error: " + t.getMessage();
                if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Cannot connect to server. Please check your connection.";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Connection timeout. Please try again.";
                } else if (t instanceof java.io.IOException) {
                    errorMsg = "Network error. Please check your internet connection.";
                }
                
                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                android.util.Log.e("LoginActivity", "Login network error", t);
            }
        });
    }
    
    // Register method
    private void register() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, String> userData = new HashMap<>();
        userData.put("name", "Client"); // Simple name
        userData.put("email", email);
        userData.put("password", password);
        
        Call<ApiResponse<User>> call = apiService.register(userData);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        SharedPrefManager.getInstance(LoginActivity.this)
                                .saveUser(apiResponse.getUser(), apiResponse.getToken());
                        Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        redirectToDashboard();
                    } else {
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // Redirect to appropriate dashboard
    private void redirectToDashboard() {
        String role = SharedPrefManager.getInstance(this).getUserRole();
        Intent intent;
        
        if ("admin".equals(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, ClientDashboardActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}
