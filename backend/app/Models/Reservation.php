<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Reservation extends Model
{
    use HasFactory;

    protected $fillable = [
        'costume_id',
        'user_id',
        'start_date',
        'end_date',
        'status',
    ];

    // Get the costume
    public function costume()
    {
        return $this->belongsTo(Costume::class);
    }

    // Get the user
    public function user()
    {
        return $this->belongsTo(User::class);
    }
}
