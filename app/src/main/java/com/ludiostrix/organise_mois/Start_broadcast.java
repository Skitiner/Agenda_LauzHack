package com.ludiostrix.organise_mois;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class Start_broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("ID", 0);
        NotificationManagerCompat.from(context).cancel(id);
    }
}
