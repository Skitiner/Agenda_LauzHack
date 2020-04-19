package com.example.agenda_lauzhack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Broadcast_sleep  extends BroadcastReceiver {

    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 150;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent start_intent = new Intent(context, Start_broadcast.class);
        start_intent.putExtra("ID", notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 8, start_intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setContentTitle("Time to sleep")
                .setContentText("If you want to sleep 8h you should go to bed now.")
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("If you want to sleep 8h you should go to bed now."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.cheetah_background, "Ok", pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
