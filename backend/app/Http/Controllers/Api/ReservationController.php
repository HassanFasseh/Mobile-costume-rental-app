<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Reservation;
use App\Models\Costume;

class ReservationController extends Controller
{
    // Create reservation (client only)
    public function store(Request $request)
    {
        try {
            $validated = $request->validate([
                'costume_id' => 'required|exists:costumes,id',
                'start_date' => 'required|date|after_or_equal:today',
                'end_date' => 'required|date|after:start_date',
            ], [
                'costume_id.required' => 'Costume ID is required',
                'costume_id.exists' => 'Costume not found',
                'start_date.required' => 'Start date is required',
                'start_date.date' => 'Start date must be a valid date',
                'start_date.after_or_equal' => 'Start date must be today or later',
                'end_date.required' => 'End date is required',
                'end_date.date' => 'End date must be a valid date',
                'end_date.after' => 'End date must be after start date',
            ]);
        } catch (\Illuminate\Validation\ValidationException $e) {
            // Return custom JSON format for validation errors
            $errors = $e->errors();
            $firstError = collect($errors)->flatten()->first();
            
            return response()->json([
                'success' => false,
                'message' => $firstError ?: 'Validation failed',
                'errors' => $errors,
            ], 422);
        }

        $startDate = $request->start_date;
        $endDate = $request->end_date;
        $costumeId = $request->costume_id;

        // Check if costume exists
        $costume = Costume::find($costumeId);
        if (!$costume) {
            return response()->json([
                'success' => false,
                'message' => 'Costume not found',
            ], 404);
        }

        // Check for date conflicts - only check approved reservations
        $conflictingReservation = Reservation::where('costume_id', $costumeId)
            ->where('status', 'approved')
            ->where(function ($query) use ($startDate, $endDate) {
                $query->whereBetween('start_date', [$startDate, $endDate])
                    ->orWhereBetween('end_date', [$startDate, $endDate])
                    ->orWhere(function ($q) use ($startDate, $endDate) {
                        $q->where('start_date', '<=', $startDate)
                          ->where('end_date', '>=', $endDate);
                    });
            })
            ->orderBy('end_date', 'asc')
            ->first();

        if ($conflictingReservation) {
            // Calculate next available date
            $nextAvailableDate = date('Y-m-d', strtotime($conflictingReservation->end_date . ' +1 day'));
            
            // Check if there are consecutive reservations
            $nextReservation = Reservation::where('costume_id', $costumeId)
                ->where('status', 'approved')
                ->where('start_date', '<=', $nextAvailableDate)
                ->where('end_date', '>=', $nextAvailableDate)
                ->orderBy('end_date', 'asc')
                ->first();
            
            if ($nextReservation) {
                $nextAvailableDate = date('Y-m-d', strtotime($nextReservation->end_date . ' +1 day'));
            }
            
            return response()->json([
                'success' => false,
                'message' => 'Costume is already reserved from ' . $conflictingReservation->start_date . ' to ' . $conflictingReservation->end_date . '. Next available date: ' . $nextAvailableDate,
            ], 409);
        }

        // Calculate rental days
        $start = new \DateTime($startDate);
        $end = new \DateTime($endDate);
        $days = $start->diff($end)->days + 1;

        // Minimum rental period check (at least 1 day)
        if ($days < 1) {
            return response()->json([
                'success' => false,
                'message' => 'Reservation must be for at least 1 day',
            ], 400);
        }

        // Maximum rental period check (30 days)
        if ($days > 30) {
            return response()->json([
                'success' => false,
                'message' => 'Reservation cannot exceed 30 days',
            ], 400);
        }

        // Create reservation with pending status
        $reservation = Reservation::create([
            'costume_id' => $costumeId,
            'user_id' => $request->user()->id,
            'start_date' => $startDate,
            'end_date' => $endDate,
            'status' => 'pending',
        ]);

        $reservation->load('costume');

        return response()->json([
            'success' => true,
            'message' => 'Reservation created successfully for ' . $days . ' day(s). Waiting for admin approval.',
            'data' => $reservation,
        ], 201);
    }

    // Get reservations by user
    public function myReservations(Request $request)
    {
        $reservations = Reservation::where('user_id', $request->user()->id)
            ->with('costume')
            ->get();

        return response()->json([
            'success' => true,
            'data' => $reservations,
        ]);
    }

    // Get all reservations (admin only)
    public function index(Request $request)
    {
        $reservations = Reservation::with(['costume', 'user'])->get();

        return response()->json([
            'success' => true,
            'data' => $reservations,
        ]);
    }

    // Approve reservation (admin only)
    public function approve(Request $request, $id)
    {
        $reservation = Reservation::find($id);
        
        if (!$reservation) {
            return response()->json([
                'success' => false,
                'message' => 'Reservation not found',
            ], 404);
        }

        // Check if there are any conflicts with approved reservations
        $conflictingReservation = Reservation::where('costume_id', $reservation->costume_id)
            ->where('id', '!=', $id)
            ->where('status', 'approved')
            ->where(function ($query) use ($reservation) {
                $query->whereBetween('start_date', [$reservation->start_date, $reservation->end_date])
                    ->orWhereBetween('end_date', [$reservation->start_date, $reservation->end_date])
                    ->orWhere(function ($q) use ($reservation) {
                        $q->where('start_date', '<=', $reservation->start_date)
                          ->where('end_date', '>=', $reservation->end_date);
                    });
            })
            ->first();

        if ($conflictingReservation) {
            return response()->json([
                'success' => false,
                'message' => 'Cannot approve: Costume is already reserved from ' . $conflictingReservation->start_date . ' to ' . $conflictingReservation->end_date,
            ], 409);
        }

        $reservation->status = 'approved';
        $reservation->save();
        $reservation->load(['costume', 'user']);

        return response()->json([
            'success' => true,
            'message' => 'Reservation approved successfully',
            'data' => $reservation,
        ]);
    }

    // Reject reservation (admin only)
    public function reject(Request $request, $id)
    {
        $reservation = Reservation::find($id);
        
        if (!$reservation) {
            return response()->json([
                'success' => false,
                'message' => 'Reservation not found',
            ], 404);
        }

        $reservation->status = 'rejected';
        $reservation->save();
        $reservation->load(['costume', 'user']);

        return response()->json([
            'success' => true,
            'message' => 'Reservation rejected successfully',
            'data' => $reservation,
        ]);
    }

    // Check if costume is available for given dates
    public function checkAvailability(Request $request)
    {
        $request->validate([
            'costume_id' => 'required|exists:costumes,id',
            'start_date' => 'required|date',
            'end_date' => 'required|date|after:start_date',
        ]);

        $startDate = $request->start_date;
        $endDate = $request->end_date;
        $costumeId = $request->costume_id;

        // Check for conflicts
        $conflictingReservation = Reservation::where('costume_id', $costumeId)
            ->where(function ($query) use ($startDate, $endDate) {
                $query->whereBetween('start_date', [$startDate, $endDate])
                    ->orWhereBetween('end_date', [$startDate, $endDate])
                    ->orWhere(function ($q) use ($startDate, $endDate) {
                        $q->where('start_date', '<=', $startDate)
                          ->where('end_date', '>=', $endDate);
                    });
            })
            ->first();

        $isAvailable = $conflictingReservation === null;

        return response()->json([
            'success' => true,
            'available' => $isAvailable,
            'message' => $isAvailable 
                ? 'Costume is available for these dates' 
                : 'Costume is already reserved from ' . $conflictingReservation->start_date . ' to ' . $conflictingReservation->end_date,
        ]);
    }
}

