<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\CostumeController;
use App\Http\Controllers\Api\ReservationController;

// Public routes
Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);

// Protected routes (require authentication)
Route::middleware('auth:sanctum')->group(function () {
    // Costumes
    Route::get('/costumes', [CostumeController::class, 'index']);
    Route::post('/costumes', [CostumeController::class, 'store'])->middleware('admin');
    Route::delete('/costumes/{id}', [CostumeController::class, 'destroy'])->middleware('admin');
    
    // Reservations
    Route::post('/reservations', [ReservationController::class, 'store']);
    Route::get('/reservations/my', [ReservationController::class, 'myReservations']);
    Route::get('/reservations', [ReservationController::class, 'index'])->middleware('admin');
    Route::post('/reservations/{id}/approve', [ReservationController::class, 'approve'])->middleware('admin');
    Route::post('/reservations/{id}/reject', [ReservationController::class, 'reject'])->middleware('admin');
});
