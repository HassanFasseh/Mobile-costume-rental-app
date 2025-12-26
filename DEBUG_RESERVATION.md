# Debugging Reservation Issues

## Common Issues and Solutions

### 1. Check Logcat for Errors
In Android Studio, open Logcat and filter by "ReservationActivity" to see detailed logs.

### 2. Verify API Connection
- Check if API URL is correct in `ApiClient.java`
- For emulator: `http://10.0.2.2:8000/api/`
- For physical device: Use your computer's IP address

### 3. Check Authentication Token
- Verify user is logged in
- Token should be stored in SharedPreferences
- Token format: `Bearer {token}`

### 4. Verify Date Format
- Dates should be in format: `YYYY-MM-DD` (e.g., `2024-12-26`)
- Date picker automatically formats dates correctly

### 5. Test API Endpoint Directly
Use Postman or curl to test:
```bash
curl -X POST http://localhost:8000/api/reservations \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "costume_id": 1,
    "start_date": "2024-12-26",
    "end_date": "2024-12-27"
  }'
```

### 6. Check Laravel Logs
Check `backend/storage/logs/laravel.log` for server-side errors.

### 7. Common Error Codes
- **401**: Unauthorized - Token missing or invalid
- **404**: Costume not found
- **409**: Date conflict - Costume already reserved
- **422**: Validation error - Check error message
- **500**: Server error - Check Laravel logs

