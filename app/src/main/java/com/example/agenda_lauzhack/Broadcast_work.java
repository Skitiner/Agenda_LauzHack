package com.example.agenda_lauzhack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;
import static android.app.Notification.PRIORITY_HIGH;
import static android.app.Notification.PRIORITY_MAX;

public class Broadcast_work extends BroadcastReceiver {
    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 153;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent click_intent = new Intent(context, AgendaActivity.class);
        click_intent.putExtra("POPUP", true);

        Intent start_intent = new Intent(context, Start_broadcast.class);
        start_intent.putExtra("ID", notificationId);

        Intent postpone_intent = new Intent(context, Postpone_broadcast.class);
        postpone_intent.putExtra("ID", notificationId);

        Intent cancel_intent = new Intent(context, Cancel_broadcast.class);
        cancel_intent.putExtra("ID", notificationId);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent click = PendingIntent.getActivity(context, 0, click_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent startAct = PendingIntent.getBroadcast(context, 1, start_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent postpone = PendingIntent.getBroadcast(context, 1, postpone_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent cancel = PendingIntent.getBroadcast(context, 2, cancel_intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.organisemois))
                .setContentTitle("Time to work !")
                .setContentText("Go Check in the application")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Go Check in the application"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(click)
                .setAutoCancel(true)
                .addAction(R.drawable.cheetah_background, "Start", startAct)
                .addAction(R.drawable.rabbit_background, "In 15 min", postpone)
                .addAction(R.drawable.rabbit_background, "Cancel", cancel);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

}
