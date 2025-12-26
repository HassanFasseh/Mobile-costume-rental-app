package com.costumerental.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.costumerental.app.api.ApiClient;
import com.costumerental.app.api.ApiService;
import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Reservation;
import com.costumerental.app.utils.NotificationHelper;
import com.costumerental.app.utils.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Reservation Activity (Client only)
public class ReservationActivity extends AppCompatActivity {
    
    private EditText editTextStartDate, editTextEndDate;
    private Button buttonReserve;
    private TextView textViewCostumeInfo;
    private int costumeId;
    private ApiService apiService;
    private NotificationHelper notificationHelper;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private boolean isCostumeAvailable;
    private String nextAvailableDate;
    private TextView textViewAvailabilityInfo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);
        
        // Get costume ID and availability info
        costumeId = getIntent().getIntExtra("costume_id", -1);
        isCostumeAvailable = getIntent().getBooleanExtra("is_available", true);
        nextAvailableDate = getIntent().getStringExtra("next_available_date");
        
        if (costumeId == -1) {
            Toast.makeText(this, "Invalid costume", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);
        buttonReserve = findViewById(R.id.buttonReserve);
        textViewCostumeInfo = findViewById(R.id.textViewCostumeInfo);
        textViewAvailabilityInfo = findViewById(R.id.textViewAvailabilityInfo);
        
        // Make date fields non-editable (only via date picker)
        editTextStartDate.setFocusable(false);
        editTextEndDate.setFocusable(false);
        editTextStartDate.setClickable(true);
        editTextEndDate.setClickable(true);
        
        // Set date hints
        editTextStartDate.setHint("Tap to select start date");
        editTextEndDate.setHint("Tap to select end date");
        
        // Display availability information
        if (!isCostumeAvailable && nextAvailableDate != null && !nextAvailableDate.isEmpty()) {
            textViewAvailabilityInfo.setVisibility(View.VISIBLE);
            textViewAvailabilityInfo.setText("⚠ This costume is currently booked. You can reserve it starting from: " + nextAvailableDate);
            textViewAvailabilityInfo.setTextColor(0xFFFF9800); // Orange
        } else if (isCostumeAvailable) {
            textViewAvailabilityInfo.setVisibility(View.GONE);
        } else {
            textViewAvailabilityInfo.setVisibility(View.GONE);
        }
        
        apiService = ApiClient.getApiService();
        notificationHelper = new NotificationHelper(this);
        
        // Start date picker
        editTextStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartDatePicker();
            }
        });
        
        // End date picker
        editTextEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndDatePicker();
            }
        });
        
        // Reserve button click
        buttonReserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createReservation();
            }
        });
    }
    
    // Show start date picker
    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editTextStartDate.setText(dateFormat.format(startCalendar.getTime()));
                
                // Reset end date if it's before start date
                if (endCalendar.before(startCalendar)) {
                    editTextEndDate.setText("");
                }
            },
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date based on availability
        long minDateMillis;
        if (!isCostumeAvailable && nextAvailableDate != null && !nextAvailableDate.isEmpty()) {
            try {
                Calendar minDate = Calendar.getInstance();
                minDate.setTime(dateFormat.parse(nextAvailableDate));
                minDateMillis = minDate.getTimeInMillis();
            } catch (Exception e) {
                // If parsing fails, use today
                minDateMillis = System.currentTimeMillis() - 1000;
            }
        } else {
            // Use today as minimum if costume is available
            minDateMillis = System.currentTimeMillis() - 1000;
        }
        
        datePickerDialog.getDatePicker().setMinDate(minDateMillis);
        datePickerDialog.show();
    }
    
    // Show end date picker
    private void showEndDatePicker() {
        if (editTextStartDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select start date first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editTextEndDate.setText(dateFormat.format(endCalendar.getTime()));
            },
            endCalendar.get(Calendar.YEAR),
            endCalendar.get(Calendar.MONTH),
            endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to start date
        datePickerDialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
        datePickerDialog.show();
    }
    
    // Create reservation method
    private void createReservation() {
        String startDate = editTextStartDate.getText().toString().trim();
        String endDate = editTextEndDate.getText().toString().trim();
        
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate dates
        try {
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.setTime(dateFormat.parse(startDate));
            end.setTime(dateFormat.parse(endDate));
            
            // Check if start date is after next available date (if costume is not available)
            if (!isCostumeAvailable && nextAvailableDate != null && !nextAvailableDate.isEmpty()) {
                Calendar nextAvailable = Calendar.getInstance();
                nextAvailable.setTime(dateFormat.parse(nextAvailableDate));
                nextAvailable.set(Calendar.HOUR_OF_DAY, 0);
                nextAvailable.set(Calendar.MINUTE, 0);
                nextAvailable.set(Calendar.SECOND, 0);
                nextAvailable.set(Calendar.MILLISECOND, 0);
                
                start.set(Calendar.HOUR_OF_DAY, 0);
                start.set(Calendar.MINUTE, 0);
                start.set(Calendar.SECOND, 0);
                start.set(Calendar.MILLISECOND, 0);
                
                if (start.before(nextAvailable) || start.equals(nextAvailable)) {
                    Toast.makeText(this, "Start date must be after " + nextAvailableDate + " (when costume becomes available)", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            
            // Check if end date is after start date
            if (!end.after(start)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check minimum rental period (1 day)
            long daysDiff = (end.getTimeInMillis() - start.getTimeInMillis()) / (1000 * 60 * 60 * 24);
            if (daysDiff < 1) {
                Toast.makeText(this, "Reservation must be for at least 1 day", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check maximum rental period (30 days)
            if (daysDiff > 30) {
                Toast.makeText(this, "Reservation cannot exceed 30 days", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("costume_id", costumeId);
        reservationData.put("start_date", startDate);
        reservationData.put("end_date", endDate);
        
        String tokenValue = SharedPrefManager.getInstance(this).getToken();
        if (tokenValue == null || tokenValue.isEmpty()) {
            Toast.makeText(this, "Not logged in. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String token = "Bearer " + tokenValue;
        
        // Debug logging
        android.util.Log.d("ReservationActivity", "Creating reservation:");
        android.util.Log.d("ReservationActivity", "Costume ID: " + costumeId);
        android.util.Log.d("ReservationActivity", "Start Date: " + startDate);
        android.util.Log.d("ReservationActivity", "End Date: " + endDate);
        android.util.Log.d("ReservationActivity", "Token: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
        
        // Show loading
        buttonReserve.setEnabled(false);
        buttonReserve.setText("Creating...");
        
        Call<ApiResponse<Reservation>> call = apiService.createReservation(token, reservationData);
        call.enqueue(new Callback<ApiResponse<Reservation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Reservation>> call, Response<ApiResponse<Reservation>> response) {
                buttonReserve.setEnabled(true);
                buttonReserve.setText("Reserve");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Reservation> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(ReservationActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        
                        // Show notification for created reservation
                        if (apiResponse.getData() != null) {
                            notificationHelper.showStatusChangeNotification(apiResponse.getData());
                        }
                        
                        finish();
                    } else {
                        String errorMsg = apiResponse.getMessage();
                        if (errorMsg != null && !errorMsg.isEmpty()) {
                            Toast.makeText(ReservationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ReservationActivity.this, "Failed to create reservation", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    // Parse error response properly
                    String errorMessage = "Failed to create reservation (Code: " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            // Read the error body as string
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
                                    } else if (jsonObject.has("errors")) {
                                        // Laravel validation errors format
                                        com.google.gson.JsonObject errors = jsonObject.getAsJsonObject("errors");
                                        StringBuilder validationErrors = new StringBuilder();
                                        for (String key : errors.keySet()) {
                                            com.google.gson.JsonArray fieldErrors = errors.getAsJsonArray(key);
                                            for (int i = 0; i < fieldErrors.size(); i++) {
                                                if (validationErrors.length() > 0) {
                                                    validationErrors.append("\n");
                                                }
                                                validationErrors.append(fieldErrors.get(i).getAsString());
                                            }
                                        }
                                        if (validationErrors.length() > 0) {
                                            errorMessage = validationErrors.toString();
                                        }
                                    }
                                } catch (Exception e2) {
                                    // If parsing fails, show raw error
                                    errorMessage = "Error: " + errorJson.toString().substring(0, Math.min(100, errorJson.length()));
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ReservationActivity", "Error parsing response: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Show error message
                    Toast.makeText(ReservationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    android.util.Log.e("ReservationActivity", "Reservation failed: " + errorMessage);
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<Reservation>> call, Throwable t) {
                buttonReserve.setEnabled(true);
                buttonReserve.setText("Reserve");
                Toast.makeText(ReservationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
