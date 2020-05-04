package com.ludiostrix.organise_mois;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Broadcast_wake_up  extends BroadcastReceiver {

    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 151;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent start = new Intent(context, AgendaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 7, start, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setContentTitle(context.getString(R.string.wakeup_notif))
                .setContentText(context.getString(R.string.check_notif))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.check_notif)))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.cheetah_background, context.getString(R.string.awake_notif), pendingIntent);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
