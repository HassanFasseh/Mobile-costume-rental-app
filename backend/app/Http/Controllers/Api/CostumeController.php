<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Costume;
use App\Models\Reservation;
use Carbon\Carbon;

class CostumeController extends Controller
{
    // Get all costumes
    public function index()
    {
        $costumes = Costume::all();
        
        // Add availability information for each costume
        $costumesWithAvailability = $costumes->map(function ($costume) {
            $availability = $this->getCostumeAvailability($costume->id);
            return [
                'id' => $costume->id,
                'name' => $costume->name,
                'size' => $costume->size,
                'price' => $costume->price,
                'image' => $costume->image,
                'is_available' => $availability['is_available'],
                'next_available_date' => $availability['next_available_date'],
            ];
        });
        
        return response()->json([
            'success' => true,
            'data' => $costumesWithAvailability,
        ]);
    }
    
    // Get costume availability
    private function getCostumeAvailability($costumeId)
    {
        $today = Carbon::today();
        
        // Find all approved reservations that are active or future
        $activeReservations = Reservation::where('costume_id', $costumeId)
            ->where('status', 'approved')
            ->where('end_date', '>=', $today->toDateString())
            ->orderBy('start_date', 'asc')
            ->get();
        
        if ($activeReservations->isEmpty()) {
            // No active reservations, costume is available
            return [
                'is_available' => true,
                'next_available_date' => null,
            ];
        }
        
        // Check if costume is currently booked (reservation that includes today)
        foreach ($activeReservations as $reservation) {
            $startDate = Carbon::parse($reservation->start_date);
            $endDate = Carbon::parse($reservation->end_date);
            
            if ($startDate->lte($today) && $endDate->gte($today)) {
                // Costume is currently booked, find the next available date
                // Check if there are consecutive reservations
                $nextAvailable = $this->findNextAvailableDate($activeReservations, $endDate);
                return [
                    'is_available' => false,
                    'next_available_date' => $nextAvailable->toDateString(),
                ];
            }
        }
        
        // Costume is available now, but check if there's a reservation starting soon
        $nextReservation = $activeReservations->first();
        $nextStartDate = Carbon::parse($nextReservation->start_date);
        
        if ($nextStartDate->eq($today)) {
            // Reservation starts today, costume will be unavailable
            $nextAvailable = $this->findNextAvailableDate($activeReservations, Carbon::parse($nextReservation->end_date));
            return [
                'is_available' => false,
                'next_available_date' => $nextAvailable->toDateString(),
            ];
        }
        
        // Costume is available now
        return [
            'is_available' => true,
            'next_available_date' => null,
        ];
    }
    
    // Find the next available date after a given date, considering consecutive reservations
    private function findNextAvailableDate($reservations, $afterDate)
    {
        $nextAvailable = Carbon::parse($afterDate)->addDay();
        
        // Check if there's a reservation starting immediately after
        foreach ($reservations as $reservation) {
            $startDate = Carbon::parse($reservation->start_date);
            $endDate = Carbon::parse($reservation->end_date);
            
            // If reservation starts on or before the next available date
            if ($startDate->lte($nextAvailable) && $endDate->gte($nextAvailable)) {
                // There's a gap or overlap, move to after this reservation
                $nextAvailable = Carbon::parse($endDate)->addDay();
            } else if ($startDate->eq($nextAvailable)) {
                // Reservation starts exactly when costume becomes available
                $nextAvailable = Carbon::parse($endDate)->addDay();
            }
        }
        
        return $nextAvailable;
    }

    // Add costume (admin only)
    public function store(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'size' => 'required|string',
            'price' => 'required|numeric',
            'image' => 'nullable|string',
        ]);

        $costume = Costume::create([
            'name' => $request->name,
            'size' => $request->size,
            'price' => $request->price,
            'image' => $request->image ?? 'default.jpg',
        ]);

        return response()->json([
            'success' => true,
            'message' => 'Costume added successfully',
            'data' => $costume,
        ], 201);
    }

    // Delete costume (admin only)
    public function destroy($id)
    {
        $costume = Costume::find($id);
        
        if (!$costume) {
            return response()->json([
                'success' => false,
                'message' => 'Costume not found',
            ], 404);
        }

        $costume->delete();

        return response()->json([
            'success' => true,
            'message' => 'Costume deleted successfully',
        ]);
    }
}

