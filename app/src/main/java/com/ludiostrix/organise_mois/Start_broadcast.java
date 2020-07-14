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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*

Cette classe est utilisée quand l'utilisateur accepte une notification.
Elle met à jour le score du travail et du sport si nécessaire.

 */

public class Start_broadcast extends BroadcastReceiver {

    private Context context;
    private Profile userProfile = new Profile();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        int id = intent.getIntExtra("ID", 0);
        NotificationManagerCompat.from(context).cancel(id);

        updateWeight();
    }

    private void updateWeight() {

        readFromFile();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int startI = calendar.get(Calendar.HOUR_OF_DAY)*4 + calendar.get(Calendar.MINUTE)/15;
        int converted_indice = convertedIndice();

        ArrayList<timeSlot.currentTask> dailyTasks = new ArrayList<>(userProfile.agenda.get(converted_indice));
        List<String> newEvent = new ArrayList<>(userProfile.newEventAgenda.get(converted_indice));

        // Determinate the time boudaries of the block to postpone
        int endI = startI;

        while ((dailyTasks.get(endI) == dailyTasks.get(startI) && endI < (dailyTasks.size() - 1))) {
            endI++;
        }

        for(int i = startI; i < (endI + 1); i++) {
            //update weight
            if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK &&
                    userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Work")) < 9){
                userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Work"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) + 1);
            }
            else if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK_CATCH_UP &&
                    userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Work")) > 0){
                userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Work"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) - 1);
            }
            else if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.SPORT &&
                    userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Sport")) < 9){
                userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Sport"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) + 1);
            }
            else if(userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.SPORT_CATCH_UP &&
                    userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Sport")) > 0){
                userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Sport"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) - 1);
            }
        }
        saveToFile();
    }

    private int convertedIndice() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        return offset%7;
    }

    public static int conversionDayIndice() {
        int offset = 0;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                offset = 0;
                break;
            case Calendar.TUESDAY:
                offset = 1;
                break;
            case Calendar.WEDNESDAY:
                offset = 2;
                break;
            case Calendar.THURSDAY:
                offset = 3;
                break;
            case Calendar.FRIDAY:
                offset = 4;
                break;
            case Calendar.SATURDAY:
                offset = 5;
                break;
            case Calendar.SUNDAY:
                offset = 6;
                break;

        }

        return offset;
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


