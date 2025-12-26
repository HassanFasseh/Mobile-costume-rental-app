<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Costume extends Model
{
    use HasFactory;

    protected $fillable = [
        'name',
        'size',
        'price',
        'image',
    ];

    // Get costume reservations
    public function reservations()
    {
        return $this->hasMany(Reservation::class);
    }
}
