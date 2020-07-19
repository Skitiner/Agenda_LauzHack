package com.ludiostrix.organise_mois;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


/*

Notification de travail.

 */

public class Broadcast_work extends BroadcastReceiver {
    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 153;
    private static int life_time = 5*60000;

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
                .setSmallIcon(R.mipmap.organise_work)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.organisemois))
                .setContentTitle(context.getString(R.string.workTime))
                .setContentText(context.getString(R.string.check_notif))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.check_notif)))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(click)
                .setAutoCancel(true)
                .setTimeoutAfter(life_time)
                .addAction(R.drawable.cheetah_background, context.getString(R.string.start), startAct)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.minutes), postpone)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.cancel), cancel);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

}
