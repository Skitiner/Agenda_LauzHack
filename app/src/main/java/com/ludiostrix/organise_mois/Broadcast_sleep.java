package com.ludiostrix.organise_mois;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/*

Notification de dormir.

 */

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
                .setContentTitle(context.getString(R.string.time_sleep))
                .setContentText(context.getString(R.string.go_to_bed_notif))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.go_to_bed_notif)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.cheetah_background, "Ok", pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
