package com.example.agenda_lauzhack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class Start_broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("ID", 0);
        NotificationManagerCompat.from(context).cancel(id);
    }
}
