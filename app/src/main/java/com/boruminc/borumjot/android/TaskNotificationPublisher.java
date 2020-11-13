package com.boruminc.borumjot.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaskNotificationPublisher extends BroadcastReceiver {
    final String PACKAGE_NAME = "com.boruminc.borumjot.android";
    final String CATEGORY_NAME = "Tasks";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra("notification");
        int notificationId = intent.getIntExtra("notification_id", 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    PACKAGE_NAME,
                    CATEGORY_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                notificationManager.notify(notificationId, notification);
            }
        }

    }
}
