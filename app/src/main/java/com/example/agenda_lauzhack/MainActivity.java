package com.example.agenda_lauzhack;

import androidx.annotation.RequiresApi;
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
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
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
    private static ArrayList<PendingIntent> intentArray;
    private AlarmManager mgrAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);

        mgrAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        if(intentArray != null)
            removeAlarms();

        intentArray = new ArrayList<PendingIntent>();

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

    private void removeAlarms() {
        for(PendingIntent intent : intentArray) {
            mgrAlarm.cancel(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        readFromFile();
        setAlarmOfTheDay();
    }

    private void setAlarmOfTheDay() {

        Toast.makeText(this, "Notifications activated", Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        timeSlot.currentTask task = userProfile.agenda.get(0).get(0);

        Log.w("AGENDA_notif", userProfile.agenda.toString());


        int hour = 0;
        int minutes = 0;
        int k = 0;
        for(int i = 0; i < (userProfile.agenda).size(); i++){
            for(int j = 0; j < (userProfile.agenda.get(i)).size(); j++) {
                k++;
                hour = j/4;
                minutes = 15*(j%4);

                if(userProfile.agenda.get(i).get(j) != task) {
                    Log.w("Current", " " + userProfile.agenda.get(i).get(j));
                    Log.w("task", " " + task);

                    task = userProfile.agenda.get(i).get(j);

                    Intent intentForService = new Intent(this, RemindBroadcast.class);

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, k,
                            intentForService, PendingIntent.FLAG_CANCEL_CURRENT);

                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());


                    if(i == 0 && !(hour >= calendar.get(Calendar.HOUR_OF_DAY) && minutes >= calendar.get(Calendar.MINUTE)))
                        continue;

                    calendar.add(Calendar.DAY_OF_MONTH, i);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minutes);
                    calendar.set(Calendar.SECOND, 0);

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
        agenda.putExtra("CALCULCATION", userProfile.calculation);
        agenda.putExtra(USER_PROFILE, userProfile);
        startActivity(agenda);
    }
  
    public void clickedProfileButtonXmlCallback(View view) {

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);

    }

}
