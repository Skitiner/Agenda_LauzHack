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

import static com.ludiostrix.organise_mois.MainActivity.setAlarmOfTheDay;

public class Postpone_broadcast extends BroadcastReceiver {

    private Context context;
    private Profile userProfile = new Profile();

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        int id = intent.getIntExtra("ID", 0);
        NotificationManagerCompat.from(context).cancel(id);

        postponeTask();
    }

    private void postponeTask() {
        readFromFile();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int startI = calendar.get(Calendar.HOUR_OF_DAY)*4 + calendar.get(Calendar.MINUTE)/15;
        int converted_indice = convertedIndice();

        if(startI != 0){
            startI--;
        }

        ArrayList<timeSlot.currentTask> dailyTasks = new ArrayList<>(userProfile.agenda.get(converted_indice));
        List<String> newEvent = new ArrayList<>(userProfile.newEventAgenda.get(converted_indice));

        // Determinate the time boudaries of the block to postpone
        int endI = startI;

        while ((dailyTasks.get(endI) == dailyTasks.get(startI) || dailyTasks.get(endI) != timeSlot.currentTask.FREE || dailyTasks.get(endI) != timeSlot.currentTask.PAUSE) && endI < (dailyTasks.size() - 1)) {
            endI++;
        }

        for(int i = startI; i < (endI + 1); i++) {
            userProfile.agenda.get(converted_indice).set(i, dailyTasks.get(i-1));
            userProfile.newEventAgenda.get(converted_indice).set(i, newEvent.get(i-1));
        }
        updateDay();
        saveToFile();
        setAlarmOfTheDay(context);
    }

    private void updateDay() {
        int convertedIndice = convertedIndice();
        int position;
        for (int i = 0; i < 96; i += 4) {
            position = i / 4;
            userProfile.fullAgenda.get(convertedIndice).get(position).task_1 = userProfile.agenda.get(convertedIndice).get(i);
            userProfile.fullAgenda.get(convertedIndice).get(position).task_2 = userProfile.agenda.get(convertedIndice).get(i + 1);
            userProfile.fullAgenda.get(convertedIndice).get(position).task_3 = userProfile.agenda.get(convertedIndice).get(i + 2);
            userProfile.fullAgenda.get(convertedIndice).get(position).task_4 = userProfile.agenda.get(convertedIndice).get(i + 3);
            userProfile.fullAgenda.get(convertedIndice).get(position).new_task_1 = userProfile.newEventAgenda.get(convertedIndice).get(i);
            userProfile.fullAgenda.get(convertedIndice).get(position).new_task_2 = userProfile.newEventAgenda.get(convertedIndice).get(i + 1);
            userProfile.fullAgenda.get(convertedIndice).get(position).new_task_3 = userProfile.newEventAgenda.get(convertedIndice).get(i + 2);
            userProfile.fullAgenda.get(convertedIndice).get(position).new_task_4 = userProfile.newEventAgenda.get(convertedIndice).get(i + 3);
        }
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
