package com.example.agenda_lauzhack;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Broadcast_break  extends BroadcastReceiver {

    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 148;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent click_intent = new Intent(context, AgendaActivity.class);
        click_intent.putExtra("POPUP", false);

        Intent postpone_intent = new Intent(context, Postpone_broadcast.class);
        postpone_intent.putExtra("ID", notificationId);

        Intent start_intent = new Intent(context, Start_broadcast.class);
        start_intent.putExtra("ID", notificationId);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent click = PendingIntent.getActivity(context, 0, click_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent postpone = PendingIntent.getBroadcast(context, 15, postpone_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent start = PendingIntent.getBroadcast(context, 16, start_intent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setContentTitle(context.getString(R.string.do_break_notif))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(click)
                .addAction(R.drawable.cheetah_background, "Ok", start)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.minutes), postpone);


        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}
