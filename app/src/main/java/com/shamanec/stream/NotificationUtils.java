package com.shamanec.stream;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;


public class NotificationUtils {

    public static final int NOTIFICATION_ID = 1991;
    private static final String NOTIFICATION_CHANNEL_ID = "com.shamanec.stream";
    private static final String NOTIFICATION_CHANNEL_NAME = "com.shamanec.stream";

    // The getNotification method creates the notification channel and creates a notification with a low importance level.
    // It returns a Pair object containing the notification ID and the created notification.
    public static Pair<Integer, Notification> getNotification(@NonNull Context context) {
        createNotificationChannel(context);
        Notification notification = createNotification(context);
        NotificationManager notificationManager
                = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
        return new Pair<>(NOTIFICATION_ID, notification);
    }

    // The createNotificationChannel method creates a notification channel if the device is running Android 8.0 or higher.
    // The channel has a name, ID, and low importance level. It also sets the lock screen visibility to private.
    @TargetApi(Build.VERSION_CODES.O)
    private static void createNotificationChannel(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    // The createNotification method creates the notification using a NotificationCompat.Builder.
    // It sets the notification icon, text, and other attributes such as priority and whether it should be shown continuously (ongoing).
    // The method returns the created Notification object.
    private static Notification createNotification(@NonNull Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.alert_box);
        builder.setContentText("GADS-Stream is recording this device's screen..");
        builder.setOngoing(true);
        builder.setCategory(Notification.CATEGORY_SERVICE);
        builder.setPriority(Notification.PRIORITY_LOW);
        builder.setShowWhen(true);
        return builder.build();
    }

}
