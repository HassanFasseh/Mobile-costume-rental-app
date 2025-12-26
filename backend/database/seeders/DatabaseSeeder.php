<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use App\Models\User;
use App\Models\Costume;
use App\Models\Reservation;
use Carbon\Carbon;

class DatabaseSeeder extends Seeder
{
    public function run(): void
    {
        // Create admin user
        $admin = User::create([
            'name' => 'Admin',
            'email' => 'admin@test.com',
            'password' => Hash::make('password123'),
            'role' => 'admin',
        ]);

        // Create sample clients
        $client1 = User::create([
            'name' => 'Client Test',
            'email' => 'client@test.com',
            'password' => Hash::make('password123'),
            'role' => 'client',
        ]);

        $client2 = User::create([
            'name' => 'John Doe',
            'email' => 'john@test.com',
            'password' => Hash::make('password123'),
            'role' => 'client',
        ]);

        $client3 = User::create([
            'name' => 'Jane Smith',
            'email' => 'jane@test.com',
            'password' => Hash::make('password123'),
            'role' => 'client',
        ]);

        // Create costumes for testing availability
        $costume1 = Costume::create([
            'name' => 'Pirate Costume',
            'size' => 'M',
            'price' => 25.00,
            'image' => 'pirate.jpg',
        ]);

        $costume2 = Costume::create([
            'name' => 'Superhero Costume',
            'size' => 'L',
            'price' => 30.00,
            'image' => 'superhero.jpg',
        ]);

        $costume3 = Costume::create([
            'name' => 'Witch Costume',
            'size' => 'S',
            'price' => 20.00,
            'image' => 'witch.jpg',
        ]);

        $costume4 = Costume::create([
            'name' => 'Vampire Costume',
            'size' => 'M',
            'price' => 28.00,
            'image' => 'vampire.jpg',
        ]);

        $costume5 = Costume::create([
            'name' => 'Zombie Costume',
            'size' => 'XL',
            'price' => 35.00,
            'image' => 'zombie.jpg',
        ]);

        $costume6 = Costume::create([
            'name' => 'Fairy Costume',
            'size' => 'S',
            'price' => 22.00,
            'image' => 'fairy.jpg',
        ]);

        $today = Carbon::today();

        // Costume 1: Currently booked (reservation from 3 days ago to 2 days from now)
        Reservation::create([
            'costume_id' => $costume1->id,
            'user_id' => $client1->id,
            'start_date' => $today->copy()->subDays(3)->toDateString(),
            'end_date' => $today->copy()->addDays(2)->toDateString(),
            'status' => 'approved',
        ]);

        // Costume 2: Available now, but has a future reservation (starts in 5 days)
        Reservation::create([
            'costume_id' => $costume2->id,
            'user_id' => $client2->id,
            'start_date' => $today->copy()->addDays(5)->toDateString(),
            'end_date' => $today->copy()->addDays(8)->toDateString(),
            'status' => 'approved',
        ]);

        // Costume 3: Has consecutive reservations (multiple bookings)
        Reservation::create([
            'costume_id' => $costume3->id,
            'user_id' => $client1->id,
            'start_date' => $today->copy()->addDays(1)->toDateString(),
            'end_date' => $today->copy()->addDays(3)->toDateString(),
            'status' => 'approved',
        ]);
        Reservation::create([
            'costume_id' => $costume3->id,
            'user_id' => $client2->id,
            'start_date' => $today->copy()->addDays(4)->toDateString(),
            'end_date' => $today->copy()->addDays(6)->toDateString(),
            'status' => 'approved',
        ]);

        // Costume 4: Has a pending reservation (should still show as available)
        Reservation::create([
            'costume_id' => $costume4->id,
            'user_id' => $client3->id,
            'start_date' => $today->copy()->addDays(2)->toDateString(),
            'end_date' => $today->copy()->addDays(4)->toDateString(),
            'status' => 'pending',
        ]);

        // Costume 5: Has a rejected reservation (should still show as available)
        Reservation::create([
            'costume_id' => $costume5->id,
            'user_id' => $client1->id,
            'start_date' => $today->copy()->addDays(1)->toDateString(),
            'end_date' => $today->copy()->addDays(3)->toDateString(),
            'status' => 'rejected',
        ]);

        // Costume 6: Available (no reservations) - this one should show as available

        // Additional test: Costume 1 also has a future reservation after current one ends
        Reservation::create([
            'costume_id' => $costume1->id,
            'user_id' => $client2->id,
            'start_date' => $today->copy()->addDays(5)->toDateString(),
            'end_date' => $today->copy()->addDays(7)->toDateString(),
            'status' => 'approved',
        ]);

        // Additional test: Costume 2 has a reservation starting today
        Reservation::create([
            'costume_id' => $costume2->id,
            'user_id' => $client3->id,
            'start_date' => $today->toDateString(),
            'end_date' => $today->copy()->addDays(2)->toDateString(),
            'status' => 'approved',
        ]);
    }
}
