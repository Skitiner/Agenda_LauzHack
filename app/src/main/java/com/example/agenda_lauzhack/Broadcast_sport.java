package com.example.agenda_lauzhack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class Broadcast_sport extends BroadcastReceiver {
    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 152;
    Profile userProfile = new Profile();

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
        PendingIntent click = PendingIntent.getActivity(context, 3, click_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent startAct = PendingIntent.getBroadcast(context, 4, start_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent postpone = PendingIntent.getBroadcast(context,5, postpone_intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent cancel = PendingIntent.getBroadcast(context, 6, cancel_intent, PendingIntent.FLAG_CANCEL_CURRENT);

        readFromFile(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organise_sport)
                .setContentTitle("Sport time !")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(click)
                .setAutoCancel(true)
                .addAction(R.drawable.cheetah_background, context.getString(R.string.start), startAct)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.minutes), postpone)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.cancel), cancel);

        switch (userProfile.sportRoutine) {
            case 0:
                builder.setContentText(context.getString(R.string.notif_sport_easy));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notif_sport_easy)));
                break;
            case 1:
                builder.setContentText(context.getString(R.string.notif_sport_hard));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notif_sport_hard)));
                break;
            case 2:
                builder.setContentText(context.getString(R.string.notif_sport_hard));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.notif_sport_hard)));
                break;
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }

    private void readFromFile(Context context) {
        try {
            Context ctx = context;
            FileInputStream fileInputStream = ctx.openFileInput(userProfile.FileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineData = bufferedReader.readLine();

            userProfile.decode(lineData);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
