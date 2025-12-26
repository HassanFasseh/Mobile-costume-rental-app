# Test Data - Costume Availability System

## Users Created

### Admin
- **Email**: admin@test.com
- **Password**: password123
- **Role**: admin

### Clients
1. **Client Test**
   - Email: client@test.com
   - Password: password123

2. **John Doe**
   - Email: john@test.com
   - Password: password123

3. **Jane Smith**
   - Email: jane@test.com
   - Password: password123

## Costumes Created

1. **Pirate Costume** (Size: M, Price: $25)
2. **Superhero Costume** (Size: L, Price: $30)
3. **Witch Costume** (Size: S, Price: $20)
4. **Vampire Costume** (Size: M, Price: $28)
5. **Zombie Costume** (Size: XL, Price: $35)
6. **Fairy Costume** (Size: S, Price: $22)

## Test Scenarios

### Scenario 1: Currently Booked Costume
**Costume**: Pirate Costume (ID: 1)
- **Status**: ❌ Not Available
- **Current Reservation**: From 3 days ago to 2 days from now (APPROVED)
- **Next Reservation**: From 5 days from now to 7 days from now (APPROVED)
- **Expected**: Should show "Not Available Until: [date after first reservation ends]"
- **Note**: Has consecutive reservations, so should show date after the second reservation ends

### Scenario 2: Available Now, Future Reservation
**Costume**: Superhero Costume (ID: 2)
- **Status**: ✅ Available Now
- **Future Reservation**: Starts in 5 days (APPROVED)
- **Reservation Starting Today**: From today to 2 days from now (APPROVED)
- **Expected**: Should show "Not Available" because reservation starts today

### Scenario 3: Consecutive Reservations
**Costume**: Witch Costume (ID: 3)
- **Status**: ✅ Available Now (but will be booked soon)
- **Reservation 1**: From 1 day from now to 3 days from now (APPROVED)
- **Reservation 2**: From 4 days from now to 6 days from now (APPROVED)
- **Expected**: Should show as available now, but will show next available date after all reservations

### Scenario 4: Pending Reservation (Should Still Be Available)
**Costume**: Vampire Costume (ID: 4)
- **Status**: ✅ Available Now
- **Reservation**: From 2 days from now to 4 days from now (PENDING)
- **Expected**: Should show as available because pending reservations don't block availability

### Scenario 5: Rejected Reservation (Should Still Be Available)
**Costume**: Zombie Costume (ID: 5)
- **Status**: ✅ Available Now
- **Reservation**: From 1 day from now to 3 days from now (REJECTED)
- **Expected**: Should show as available because rejected reservations don't block availability

### Scenario 6: Fully Available
**Costume**: Fairy Costume (ID: 6)
- **Status**: ✅ Available Now
- **Reservations**: None
- **Expected**: Should show "Available Now" with green text

## Testing Instructions

1. **Login as Client**:
   - Email: client@test.com
   - Password: password123

2. **View Costumes**:
   - Navigate to "View Costumes" in the app
   - You should see all 6 costumes with their availability status

3. **Expected Results**:
   - **Pirate Costume**: Red text "✗ Not Available Until: [date]", Reserve button disabled
   - **Superhero Costume**: Red text "✗ Not Available Until: [date]", Reserve button disabled
   - **Witch Costume**: Green text "✓ Available Now", Reserve button enabled
   - **Vampire Costume**: Green text "✓ Available Now", Reserve button enabled (pending doesn't block)
   - **Zombie Costume**: Green text "✓ Available Now", Reserve button enabled (rejected doesn't block)
   - **Fairy Costume**: Green text "✓ Available Now", Reserve button enabled

4. **Test Reserve Button**:
   - Try clicking "Reserve" on available costumes - should work
   - Try clicking "Reserve" on unavailable costumes - button should be disabled/grayed out

5. **Test Admin Approval**:
   - Login as admin (admin@test.com / password123)
   - Approve the pending reservation for Vampire Costume
   - Logout and login as client again
   - Vampire Costume should now show as unavailable until the reservation end date

## Notes

- Only **APPROVED** reservations affect availability
- **PENDING** and **REJECTED** reservations do NOT block availability
- The system calculates the next available date considering consecutive reservations
- Dates are calculated from today, so the availability will change as days pass

