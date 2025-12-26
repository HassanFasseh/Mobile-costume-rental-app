package com.costumerental.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.costumerental.app.AdminReservationsActivity;
import com.costumerental.app.ClientDashboardActivity;
import com.costumerental.app.models.Reservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Helper class for managing notifications
public class NotificationHelper {
    
    private static final String CHANNEL_ID = "reservation_notifications";
    private static final String CHANNEL_NAME = "Reservation Notifications";
    private static final int NOTIFICATION_ID_DEADLINE = 1001;
    private static final int NOTIFICATION_ID_STATUS = 1002;
    
    private Context context;
    private NotificationManager notificationManager;
    
    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }
    
    // Create notification channel for Android 8.0+
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for reservation deadlines and status updates");
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    // Check for upcoming reservation deadlines and show notifications
    public void checkUpcomingDeadlines(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }
        
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        for (Reservation reservation : reservations) {
            // Only check approved reservations
            if (reservation.getStatus() == null || !reservation.getStatus().equals("approved")) {
                continue;
            }
            
            try {
                Date endDate = dateFormat.parse(reservation.getEnd_date());
                if (endDate == null) continue;
                
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(endDate);
                endCalendar.set(Calendar.HOUR_OF_DAY, 0);
                endCalendar.set(Calendar.MINUTE, 0);
                endCalendar.set(Calendar.SECOND, 0);
                endCalendar.set(Calendar.MILLISECOND, 0);
                
                // Calculate days until deadline
                long diffInMillis = endCalendar.getTimeInMillis() - today.getTimeInMillis();
                long daysUntilDeadline = diffInMillis / (1000 * 60 * 60 * 24);
                
                // Show notification if deadline is within 3 days
                if (daysUntilDeadline >= 0 && daysUntilDeadline <= 3) {
                    String costumeName = reservation.getCostume() != null 
                        ? reservation.getCostume().getName() 
                        : "Your costume";
                    
                    String message;
                    if (daysUntilDeadline == 0) {
                        message = "Your reservation for " + costumeName + " ends TODAY! Please return it.";
                    } else if (daysUntilDeadline == 1) {
                        message = "Your reservation for " + costumeName + " ends TOMORROW!";
                    } else {
                        message = "Your reservation for " + costumeName + " ends in " + daysUntilDeadline + " days.";
                    }
                    
                    showDeadlineNotification(costumeName, message, reservation.getId());
                }
            } catch (ParseException e) {
                // Skip invalid dates
                continue;
            }
        }
    }
    
    // Show notification for upcoming deadline
    private void showDeadlineNotification(String title, String message, int reservationId) {
        Intent intent = new Intent(context, ClientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            reservationId,
            intent,
            flags
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID_DEADLINE + reservationId, builder.build());
    }
    
    // Show notification when reservation status changes
    public void showStatusChangeNotification(Reservation reservation) {
        if (reservation == null) return;
        
        String costumeName = reservation.getCostume() != null 
            ? reservation.getCostume().getName() 
            : "Your costume";
        
        String status = reservation.getStatus() != null ? reservation.getStatus() : "pending";
        String statusText = status.substring(0, 1).toUpperCase() + status.substring(1);
        
        String title = "Reservation " + statusText;
        String message = "Your reservation for " + costumeName + " has been " + status + ".";
        
        Intent intent = new Intent(context, ClientDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            reservation.getId(),
            intent,
            flags
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID_STATUS + reservation.getId(), builder.build());
    }
    
    // Show notification for admin when new reservation is created
    public void showNewReservationNotification(Reservation reservation) {
        if (reservation == null) return;
        
        String costumeName = reservation.getCostume() != null 
            ? reservation.getCostume().getName() 
            : "Costume #" + reservation.getCostume_id();
        
        String clientName = reservation.getUser() != null 
            ? reservation.getUser().getName() 
            : "Client #" + reservation.getUser_id();
        
        String title = "New Reservation Request";
        String message = clientName + " requested to reserve " + costumeName + 
                        " from " + reservation.getStart_date() + " to " + reservation.getEnd_date();
        
        Intent intent = new Intent(context, AdminReservationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            reservation.getId() + 10000, // Use different ID to avoid conflicts
            intent,
            flags
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for admin notifications
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(NOTIFICATION_ID_DEADLINE + reservation.getId() + 10000, builder.build());
    }
    
    // Show notification for admin when multiple new reservations are pending
    public void showMultipleReservationsNotification(int count) {
        String title = "New Reservation Requests";
        String message = "You have " + count + " new pending reservation(s) waiting for approval.";
        
        Intent intent = new Intent(context, AdminReservationsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            99999, // Fixed ID for multiple reservations notification
            intent,
            flags
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        notificationManager.notify(99999, builder.build());
    }
}

