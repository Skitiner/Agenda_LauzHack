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
import android.widget.Switch;
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
    private static Profile userProfile = new Profile();
    private String CHANNEL_ID = "CHANNEL_ID";
    public static ArrayList<PendingIntent> intentArray;
    private static AlarmManager mgrAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_main);

        mgrAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        setAlarmOfTheDay(this);

        planningCalculation();

        if (userProfile == null){
            userProfile = new Profile();
        }

        if (!userProfile.licenceAccepted){
            Intent intent = new Intent(MainActivity.this, popupActivity.class);
            intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
            startActivity(intent);
        }
    }

    private void planningCalculation() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = (int)(365*year_offset + 0.25*(year_offset+4)) + actual_day - setting_day;

        if(offset >= 7) {
            DaySlotsCalculation daySlotsCalculation = new DaySlotsCalculation(getApplicationContext());
            daySlotsCalculation.slotCalculation();
        }
    }

    private static void removeAlarms() {
        for(PendingIntent intent : intentArray) {
            mgrAlarm.cancel(intent);
        }

        intentArray.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        setAlarmOfTheDay(this);
    }

    protected static void setAlarmOfTheDay(Context context) {
        readFromFile(context);

        if(intentArray != null)
            removeAlarms();
        else
            intentArray = new ArrayList<PendingIntent>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // ******* DEBUG MODE *****
        /*Intent intentForService = new Intent(context, Broadcast_work.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                intentForService, PendingIntent.FLAG_CANCEL_CURRENT);

        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        calendar.add(Calendar.SECOND, 4);

        mgrAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);*/

        // ******* RELEASE MODE *****
        int hour = 0;
        int minutes = 0;
        int k = 0;
        timeSlot.currentTask task = userProfile.agenda.get(0).get(0);
        Boolean canceled = userProfile.canceled_slots.get(0).get(0);

        for(int i = 0; i < (userProfile.agenda).size(); i++){
            for(int j = 0; j < (userProfile.agenda.get(i)).size(); j++) {
                k++;
                hour = j/4;
                minutes = 15*(j%4);
                Log.w("CANC", canceled + " " + userProfile.canceled_slots.get(i).get(j));
                if(userProfile.agenda.get(i).get(j) != task || userProfile.canceled_slots.get(i).get(j) != canceled) {

                    // No difference between WORK and WORK_FIX for the notifications
                    if((task == timeSlot.currentTask.WORK || task == timeSlot.currentTask.WORK_FIX)
                            && (userProfile.agenda.get(i).get(j) == timeSlot.currentTask.WORK || userProfile.agenda.get(i).get(j) == timeSlot.currentTask.WORK_FIX)
                            && userProfile.canceled_slots.get(i).get(j) == canceled) {
                        task = userProfile.agenda.get(i).get(j);
                        continue;
                    }
                    canceled = userProfile.canceled_slots.get(i).get(j);
                    task = userProfile.agenda.get(i).get(j);
                    Intent intentForService;

                    switch (task) {
                        case WORK:
                            intentForService = new Intent(context, Broadcast_work.class);
                            break;
                        case WORK_FIX:
                            intentForService = new Intent(context, Broadcast_work.class);
                            break;
                        case SPORT:
                            intentForService = new Intent(context, Broadcast_sport.class);
                            break;
                        case EAT:
                            intentForService = new Intent(context, Broadcast_eat.class);
                            break;
                        case SLEEP:
                            intentForService = new Intent(context, Broadcast_sleep.class);
                            break;
                        case MORNING_ROUTINE:
                            intentForService = new Intent(context, Broadcast_wake_up.class);
                            break;
                        case FREE:
                            intentForService = new Intent(context, Broadcast_break.class);
                            break;
                        default:
                            intentForService = new Intent(context, RemindBroadcast.class);
                            break;
                    }



                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, k,
                            intentForService, PendingIntent.FLAG_CANCEL_CURRENT);

                    calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    // Condition to not set notification before the actual time
                    if(i == 0 && !(hour > calendar.get(Calendar.HOUR_OF_DAY) || (hour == calendar.get(Calendar.HOUR_OF_DAY) && minutes >= calendar.get(Calendar.MINUTE)))) {
                        continue;
                    }

                    calendar.add(Calendar.DAY_OF_MONTH, i);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minutes);
                    calendar.set(Calendar.SECOND, 0);

                    Log.w("DAY", " " + calendar.get(Calendar.DAY_OF_MONTH));
                    Log.w("HOUR", " " + calendar.get(Calendar.HOUR_OF_DAY));
                    Log.w("MINUTE", " " + calendar.get(Calendar.MINUTE));

                    mgrAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public static void readFromFile(Context context) {
        try {
            FileInputStream fileInputStream = context.openFileInput(userProfile.FileName);
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
        agenda.putExtra(USER_PROFILE, userProfile);
        startActivity(agenda);
    }
  
    public void clickedProfileButtonXmlCallback(View view) {

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);

    }

}
