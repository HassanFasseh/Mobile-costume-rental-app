package com.costumerental.app.api;

import com.costumerental.app.models.ApiResponse;
import com.costumerental.app.models.Costume;
import com.costumerental.app.models.Reservation;
import com.costumerental.app.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

// API service interface for Retrofit
public interface ApiService {
    
    // Auth endpoints
    @POST("login")
    Call<ApiResponse<User>> login(@Body Map<String, String> credentials);
    
    @POST("register")
    Call<ApiResponse<User>> register(@Body Map<String, String> userData);
    
    // Costume endpoints
    @GET("costumes")
    Call<ApiResponse<List<Costume>>> getCostumes(@Header("Authorization") String token);
    
    @POST("costumes")
    Call<ApiResponse<Costume>> addCostume(@Header("Authorization") String token, @Body Map<String, Object> costumeData);
    
    @DELETE("costumes/{id}")
    Call<ApiResponse<Void>> deleteCostume(@Header("Authorization") String token, @Path("id") int id);
    
    // Reservation endpoints
    @POST("reservations")
    Call<ApiResponse<Reservation>> createReservation(@Header("Authorization") String token, @Body Map<String, Object> reservationData);
    
    @GET("reservations/my")
    Call<ApiResponse<List<Reservation>>> getMyReservations(@Header("Authorization") String token);
    
    @GET("reservations")
    Call<ApiResponse<List<Reservation>>> getAllReservations(@Header("Authorization") String token);
    
    @POST("reservations/{id}/approve")
    Call<ApiResponse<Reservation>> approveReservation(@Header("Authorization") String token, @Path("id") int id);
    
    @POST("reservations/{id}/reject")
    Call<ApiResponse<Reservation>> rejectReservation(@Header("Authorization") String token, @Path("id") int id);
}
