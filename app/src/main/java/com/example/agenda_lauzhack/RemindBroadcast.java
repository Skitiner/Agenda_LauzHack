package com.example.agenda_lauzhack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class RemindBroadcast extends BroadcastReceiver {
    private String CHANNEL_ID = "";
    private int notificationId = 0;
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.rabbit_foreground)
                .setContentTitle("NEW ACTIVITY !")
                .setContentText("Go Check in the application")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Go Check in the application"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

}
