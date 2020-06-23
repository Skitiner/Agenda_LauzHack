package com.ludiostrix.organise_mois;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

public class Cancel_broadcast extends BroadcastReceiver {

    private Context context;
    private Profile userProfile = new Profile();
    private static int NB_SLOT_MAX_CANCELED = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        int id = intent.getIntExtra("ID", 153);
        NotificationManagerCompat.from(context).cancel(id);

        cancelTask();
    }

    private void cancelTask() {

        readFromFile();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int startI = calendar.get(Calendar.HOUR_OF_DAY)*4 + calendar.get(Calendar.MINUTE)/15;
        int converted_indice = convertedIndice();

        for (int i = startI; i < startI + NB_SLOT_MAX_CANCELED; i++) {

            if(userProfile.agenda.get(converted_indice).get(i) != userProfile.agenda.get(converted_indice).get(startI))
                break;

            userProfile.canceled_slots.get(converted_indice).set(i, Boolean.TRUE);


            //update weight
            if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK &&
                    userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) > 0){
                userProfile.weight.get(converted_indice).get(i).set(userProfile.Task.get("Work"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) - 1);
            }
            if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.SPORT &&
                    userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) > 0){
                userProfile.weight.get(converted_indice).get(i).set(userProfile.Task.get("Sport"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) - 1);
            }
        }

        saveToFile();

        MainActivity.setAlarmOfTheDay(context);
    }

    private int convertedIndice() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        return offset%7;
    }

    private void saveToFile(){
        try {
            File file = new File(context.getFilesDir(), userProfile.FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            userProfile.Save(bufferedWriter);

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
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
}
