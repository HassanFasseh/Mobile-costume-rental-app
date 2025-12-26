package com.costumerental.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.costumerental.app.models.User;

// SharedPreferences manager for storing user session
public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "costume_rental_pref";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    
    private static SharedPrefManager instance;
    private Context context;
    
    private SharedPrefManager(Context context) {
        this.context = context;
    }
    
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }
    
    // Save user session
    public void saveUser(User user, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.apply();
    }
    
    // Get auth token
    public String getToken() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }
    
    // Get user role
    public String getUserRole() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ROLE, null);
    }
    
    // Check if user is logged in
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null) != null;
    }
    
    // Save reservation status
    public void saveReservationStatus(int reservationId, String status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("reservation_status_" + reservationId, status);
        editor.apply();
    }
    
    // Get reservation status
    public String getReservationStatus(int reservationId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString("reservation_status_" + reservationId, null);
    }
    
    // Save seen reservation IDs for admin (to track which reservations admin has seen)
    public void saveSeenReservationIds(java.util.Set<Integer> reservationIds) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        java.util.Set<String> stringSet = new java.util.HashSet<>();
        for (Integer id : reservationIds) {
            stringSet.add(String.valueOf(id));
        }
        editor.putStringSet("admin_seen_reservations", stringSet);
        editor.apply();
    }
    
    // Get seen reservation IDs for admin
    public java.util.Set<Integer> getSeenReservationIds() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        java.util.Set<String> stringSet = sharedPreferences.getStringSet("admin_seen_reservations", new java.util.HashSet<String>());
        java.util.Set<Integer> intSet = new java.util.HashSet<>();
        for (String id : stringSet) {
            try {
                intSet.add(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                // Skip invalid IDs
            }
        }
        return intSet;
    }
    
    // Add a reservation ID to seen list
    public void markReservationAsSeen(int reservationId) {
        java.util.Set<Integer> seenIds = getSeenReservationIds();
        seenIds.add(reservationId);
        saveSeenReservationIds(seenIds);
    }
    
    // Logout
    public void logout() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
