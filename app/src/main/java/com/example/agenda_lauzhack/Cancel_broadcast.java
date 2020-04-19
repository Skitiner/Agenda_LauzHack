package com.example.agenda_lauzhack;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class Cancel_broadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("ID", 153);
        NotificationManagerCompat.from(context).cancel(id);
        Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
    }
}
