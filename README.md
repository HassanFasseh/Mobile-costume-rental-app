# Simple Costume Rental App

A simple university project for managing costume rentals with Android app and Laravel backend.

## Project Structure

```
Projet_mobile/
├── backend/          # Laravel API
└── android/          # Android Studio project
```

## Technologies

- **Backend**: Laravel 11, MySQL, Laravel Sanctum
- **Android**: Java, Retrofit, SQLite
- **Database**: MySQL (online), SQLite (local)

## Backend Setup (Laravel)

### Prerequisites
- PHP 8.1+
- Composer
- MySQL
- Laravel 11

### Installation Steps

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies:
```bash
composer install
```

3. Copy environment file:
```bash
cp .env.example .env
```

4. Generate application key:
```bash
php artisan key:generate
```

5. Configure database in `.env`:
```env
DB_CONNECTION=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_DATABASE=costume_rental
DB_USERNAME=root
DB_PASSWORD=your_password
```

6. Create MySQL database:
```sql
CREATE DATABASE costume_rental;
```

7. Run migrations:
```bash
php artisan migrate
```

8. Seed database:
```bash
php artisan db:seed
```

9. Install Laravel Sanctum:
```bash
php artisan vendor:publish --provider="Laravel\Sanctum\SanctumServiceProvider"
php artisan migrate
```

10. Start the server:
```bash
php artisan serve
```

The API will be available at: `http://localhost:8000/api/`

### Default Users

- **Admin**: 
  - Email: `admin@test.com`
  - Password: `password123`

- **Client**: 
  - Email: `client@test.com`
  - Password: `password123`

### API Endpoints

**Public:**
- `POST /api/login` - Login
- `POST /api/register` - Register (client only)

**Protected (require Bearer token):**
- `GET /api/costumes` - Get all costumes
- `POST /api/costumes` - Add costume (admin only)
- `DELETE /api/costumes/{id}` - Delete costume (admin only)
- `POST /api/reservations` - Create reservation (client)
- `GET /api/reservations/my` - Get my reservations (client)
- `GET /api/reservations` - Get all reservations (admin only)

## Android App Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK 24+
- Java 8+

### Installation Steps

1. Open Android Studio

2. Open the project:
   - File → Open → Select `android` folder

3. Update API URL:
   - Open `android/app/src/main/java/com/costumerental/app/api/ApiClient.java`
   - Change `BASE_URL` to your Laravel API URL:
     - For emulator: `http://10.0.2.2:8000/api/`
     - For physical device: `http://YOUR_COMPUTER_IP:8000/api/`

4. Sync Gradle:
   - Click "Sync Now" when prompted
   - Or: File → Sync Project with Gradle Files

5. Run the app:
   - Connect device or start emulator
   - Click Run button (green play icon)

### App Features

**Login Screen:**
- Login with email/password
- Register new client account

**Admin Dashboard:**
- View all costumes
- Add new costume
- Delete costume
- View all reservations

**Client Dashboard:**
- View all costumes
- Reserve a costume
- View my reservations

**Offline Mode:**
- When online: Loads costumes from API and saves to SQLite
- When offline: Loads costumes from SQLite local database

## Database Schema

### MySQL (Laravel)

**users:**
- id, name, email, password, role, timestamps

**costumes:**
- id, name, size, price, image, timestamps

**reservations:**
- id, costume_id, user_id, start_date, end_date, timestamps

### SQLite (Android)

**costumes:**
- id, name, size, price, image

## Testing

### Test Admin Login
1. Email: `admin@test.com`
2. Password: `password123`
3. Should redirect to Admin Dashboard

### Test Client Login
1. Email: `client@test.com`
2. Password: `password123`
3. Should redirect to Client Dashboard

### Test Offline Mode
1. Turn off WiFi/Mobile data
2. Open app
3. View costumes - should load from SQLite

## Troubleshooting

### Laravel Issues

**CORS Error:**
- Check `config/cors.php` is configured correctly
- Ensure `allowed_origins` includes your app's origin

**Database Connection:**
- Verify MySQL is running
- Check `.env` database credentials
- Ensure database exists

**Sanctum Token:**
- Verify Sanctum is installed and migrated
- Check token is sent in Authorization header: `Bearer {token}`

### Android Issues

**API Connection Failed:**
- Check API URL in `ApiClient.java`
- For emulator: Use `10.0.2.2` instead of `localhost`
- For physical device: Use your computer's IP address
- Ensure Laravel server is running
- Check AndroidManifest.xml has INTERNET permission

**Build Errors:**
- Sync Gradle: File → Sync Project with Gradle Files
- Clean project: Build → Clean Project
- Rebuild: Build → Rebuild Project

**App Crashes:**
- Check Logcat for error messages
- Verify all dependencies are installed
- Ensure minimum SDK is 24+

## Project Notes

- This is a **beginner-level** project
- Code is kept **simple and understandable**
- Uses **Activities only** (no Fragments)
- **SQLiteOpenHelper** for local database (not Room)
- **Retrofit** for API calls
- **Simple Toast messages** for errors
- Code is **commented** for learning

## Authors

Created by 2-3 beginner students for university project.

## License

Educational project - Free to use and modify.


<img width="412" height="860" alt="image" src="https://github.com/user-attachments/assets/2d61bd7f-38c9-464e-ad12-7d7b4f1a27c4" />
<img width="417" height="855" alt="image" src="https://github.com/user-attachments/assets/0035f5a3-20a2-48b5-8f49-73fe372d2693" />
<img width="420" height="861" alt="image" src="https://github.com/user-attachments/assets/5cf7d96b-d89e-4b71-90e7-102996628020" />
<img width="414" height="857" alt="image" src="https://github.com/user-attachments/assets/37fea462-b49f-4356-b837-4010a3990884" />
<img width="424" height="857" alt="image" src="https://github.com/user-attachments/assets/9a33b393-c830-4caf-ab53-9e31356c74d0" />
<img width="411" height="861" alt="image" src="https://github.com/user-attachments/assets/f9aa565d-5b5e-49fd-b243-75f606475a08" />
<img width="410" height="852" alt="image" src="https://github.com/user-attachments/assets/86e1b0f3-7f53-45d5-a6c6-0d4db806db76" />







