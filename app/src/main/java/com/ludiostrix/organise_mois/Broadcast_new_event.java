package com.ludiostrix.organise_mois;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class Broadcast_new_event extends BroadcastReceiver {
    private String CHANNEL_ID = "CHANNEL_ID";
    private int notificationId = 154;
    private static int life_time = 5*60000;

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

        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = (365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3)))%7;

        int slot_hour_indice = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int slot_min_indice = Calendar.getInstance().get(Calendar.MINUTE)/15;

        String event = "";

        switch (slot_min_indice) {
            case 0:
                event = userProfile.fullAgenda.get(offset).get(slot_hour_indice).new_task_1;
                break;
            case 1:
                event = userProfile.fullAgenda.get(offset).get(slot_hour_indice).new_task_2;
                break;
            case 2:
                event = userProfile.fullAgenda.get(offset).get(slot_hour_indice).new_task_3;
                break;
            case 3:
                event = userProfile.fullAgenda.get(offset).get(slot_hour_indice).new_task_4;
                break;
        }

        event = event + " !";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.organisemois)
                .setContentTitle(event)
                .setContentText("")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(""))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(click)
                .setAutoCancel(true)
                .setTimeoutAfter(life_time)
                .addAction(R.drawable.cheetah_background, context.getString(R.string.start), startAct)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.minutes), postpone)
                .addAction(R.drawable.rabbit_background, context.getString(R.string.cancel), cancel);

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
