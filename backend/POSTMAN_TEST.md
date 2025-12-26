# Postman Test Guide for Reservations

## Test Reservation Creation

### 1. First, Login to Get Token

**POST** `http://localhost:8000/api/login`

Headers:
```
Content-Type: application/json
```

Body (JSON):
```json
{
    "email": "client@test.com",
    "password": "password123"
}
```

**Response should include:**
```json
{
    "success": true,
    "token": "1|xxxxxxxxxxxxx...",
    "user": {
        "id": 2,
        "name": "Client Test",
        "email": "client@test.com",
        "role": "client"
    }
}
```

**Copy the `token` value!**

---

### 2. Create Reservation

**POST** `http://localhost:8000/api/reservations`

Headers:
```
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE
```

Body (JSON):
```json
{
    "costume_id": 1,
    "start_date": "2025-12-25",
    "end_date": "2025-12-26"
}
```

**⚠️ IMPORTANT**: Use **today's date or a future date** for `start_date`. The API validates that:
- `start_date` must be **today or later** (cannot be in the past)
- `end_date` must be **after** `start_date`
- Rental period must be **1-30 days**

**Expected Response (201 Created):**
```json
{
    "success": true,
    "message": "Reservation created successfully for 2 day(s)",
    "data": {
        "id": 1,
        "costume_id": 1,
        "user_id": 2,
        "start_date": "2025-12-25",
        "end_date": "2025-12-26",
        "costume": {
            "id": 1,
            "name": "Pirate Costume",
            "size": "M",
            "price": "25.00",
            "image": "pirate.jpg"
        }
    }
}
```

---

## Common Errors

### 401 Unauthorized
- **Cause**: Missing or invalid token
- **Solution**: Make sure you include `Authorization: Bearer YOUR_TOKEN` header
- **Fix Applied**: Now returns JSON instead of trying to redirect

### 422 Validation Error
- **Cause**: Invalid input data
- **Common Examples**: 
  - ❌ **Past date**: `"start_date": "2024-12-26"` (if today is 2025-12-25)
  - ❌ **End before start**: `"start_date": "2025-12-26", "end_date": "2025-12-25"`
  - ❌ **Same dates**: `"start_date": "2025-12-25", "end_date": "2025-12-25"`
  - ❌ **Too long**: More than 30 days rental period
- **Response**: 
```json
{
    "success": false,
    "message": "Start date must be today or later",
    "errors": {
        "start_date": ["Start date must be today or later"]
    }
}
```
- **Solution**: Use today's date or a future date. Format: `YYYY-MM-DD` (e.g., `"2025-12-25"`)

### 409 Conflict
- **Cause**: Costume already reserved for those dates
- **Response**:
```json
{
    "success": false,
    "message": "Costume is already reserved from 2024-12-26 to 2024-12-28"
}
```

### 500 Server Error
- **Cause**: Server-side error (should be fixed now)
- **Check**: Laravel logs in `storage/logs/laravel.log`

