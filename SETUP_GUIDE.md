# Quick Setup Guide

## Backend Setup (5 minutes)

1. **Install Composer dependencies:**
   ```bash
   cd backend
   composer install
   ```

2. **Setup environment:**
   ```bash
   cp .env.example .env
   php artisan key:generate
   ```

3. **Configure database in `.env`:**
   ```env
   DB_DATABASE=costume_rental
   DB_USERNAME=root
   DB_PASSWORD=your_password
   ```

4. **Create database:**
   ```sql
   CREATE DATABASE costume_rental;
   ```

5. **Run migrations and seed:**
   ```bash
   php artisan migrate
   php artisan db:seed
   ```

6. **Start server:**
   ```bash
   php artisan serve
   ```

## Android Setup (5 minutes)

1. **Open Android Studio**

2. **Open project:**
   - File → Open → Select `android` folder

3. **Update API URL:**
   - Open: `android/app/src/main/java/com/costumerental/app/api/ApiClient.java`
   - Change `BASE_URL`:
     - Emulator: `http://10.0.2.2:8000/api/`
     - Physical device: `http://YOUR_IP:8000/api/`

4. **Sync and Run:**
   - Click "Sync Now"
   - Run the app

## Test Credentials

**Admin:**
- Email: `admin@test.com`
- Password: `password123`

**Client:**
- Email: `client@test.com`
- Password: `password123`

## Important Notes

- Make sure Laravel server is running before testing Android app
- For physical device testing, use your computer's IP address (not localhost)
- Check firewall allows connections on port 8000
- Ensure MySQL is running
