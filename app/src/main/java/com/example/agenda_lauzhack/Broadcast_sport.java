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
    private String CHANNEL_ID = "";
    private int notificationId = 0;
    Profile userProfile = new Profile();

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent start = new Intent(context, AgendaActivity.class);
        start.putExtra("POPUP", true);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, start, 0);

        readFromFile(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Sport time !")
                .setContentText("Go Check in the application")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Go Check in the application"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.cheetah_background, "Start", pendingIntent)
                .addAction(R.drawable.rabbit_background, "In 15 min", pendingIntent)
                .addAction(R.drawable.rabbit_background, "Cancel", pendingIntent);

        switch (userProfile.sportRoutine) {
            case 0:
                builder.setSmallIcon(R.mipmap.sloth_foreground);
                break;
            case 1:
                builder.setSmallIcon(R.mipmap.rabbit_foreground);
                break;
            case 2:
                builder.setSmallIcon(R.mipmap.cheetah_foreground);
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
