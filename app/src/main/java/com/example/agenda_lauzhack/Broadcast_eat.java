package com.example.agenda_lauzhack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Broadcast_eat  extends BroadcastReceiver {

    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 149;

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
        PendingIntent click = PendingIntent.getActivity(context, 9, click_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent startAct = PendingIntent.getBroadcast(context, 10, start_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent postpone = PendingIntent.getBroadcast(context, 11, postpone_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent cancel = PendingIntent.getBroadcast(context, 12, cancel_intent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setContentTitle("EAT time :p")
                .setContentText("Go Check in the application")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Go Check in the application"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(click)
                .addAction(R.drawable.cheetah_background, "Start", startAct)
                .addAction(R.drawable.rabbit_background, "In 15 min", postpone)
                .addAction(R.drawable.rabbit_background, "Cancel", cancel);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
