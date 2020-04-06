package com.example.agenda_lauzhack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SymbolTable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";
    Profile userProfile = new Profile();
    private String CHANNEL_ID = "";
    private int notificationId = 0;
    private ArrayList<PendingIntent> intentArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);

        readFromFile();

        setAlarmOfTheDay();

        if (userProfile == null){
            userProfile = new Profile();
        }

        if (!userProfile.licenceAccepted){
            Intent intent = new Intent(MainActivity.this, popupActivity.class);
            intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
            startActivity(intent);
        }
    }

    private void setAlarmOfTheDay() {
        AlarmManager mgrAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        ArrayList<PendingIntent> intentArray = new ArrayList<PendingIntent>();

        Calendar calendar = Calendar.getInstance();
        timeSlot.currentTask task = userProfile.agenda.get(0).get(0);

        int time = 0;
        int k = 0;
        for(int i = 0; i < (userProfile.agenda).size(); i++){
            for(int j = 0; j < (userProfile.agenda.get(i)).size(); j++) {
                k++;
                time = j/4;
                if(userProfile.agenda.get(i).get(j) != task) {
                    task = userProfile.agenda.get(i).get(j);
                    Intent intentForService = new Intent(this, RemindBroadcast.class);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, k,
                            intentForService, 0);
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    calendar.add(Calendar.DAY_OF_MONTH, i);
                    calendar.add(Calendar.HOUR_OF_DAY, time);
                    calendar.set(Calendar.MINUTE, 15*(j%4));
                    Log.w("DAY", " " + calendar.get(Calendar.DAY_OF_MONTH));
                    Log.w("HOUR", " " + calendar.get(Calendar.HOUR_OF_DAY));
                    Log.w("MINUTE", " " + calendar.get(Calendar.MINUTE));

                    mgrAlarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    intentArray.add(pendingIntent);
                }


            }
        }


    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public void readFromFile() {
        try {
            Context ctx = getApplicationContext();
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

    public void goToAgendaActivity(View view) {
        Intent agenda = new Intent(this, AgendaActivity.class);
        userProfile.calculation = true;
        agenda.putExtra(USER_PROFILE, userProfile);
        startActivity(agenda);
    }
  
    public void clickedProfileButtonXmlCallback(View view) {

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);

    }

}
