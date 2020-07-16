package com.ludiostrix.organise_mois;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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

/*

Classe utilisée lors du report d'une activité.

 */

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

        while ((dailyTasks.get(endI) == dailyTasks.get(startI) || (dailyTasks.get(endI) != timeSlot.currentTask.FREE &&
                dailyTasks.get(endI) != timeSlot.currentTask.SPORT && dailyTasks.get(endI) != timeSlot.currentTask.SPORT_CATCH_UP &&
                dailyTasks.get(endI) != timeSlot.currentTask.WORK && dailyTasks.get(endI) != timeSlot.currentTask.WORK_CATCH_UP))
                && endI < (dailyTasks.size() - 1)) {
            endI++;
        }

        // si le endI est le dernier du jours il est supprimé
        if (userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.NEWEVENT) {
            for (newEvent event : userProfile.savedEvent) {
                if (event.name.equals(userProfile.newEventAgenda.get(converted_indice).get(endI))) {
                    if (event.sport) {
                        userProfile.lateSportSlot++;
                    } else if (event.work) {
                        userProfile.lateWorkSlot++;
                    }
                }
            }
        } else if (userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.WORK ||
                userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.WORK_FIX) {
            userProfile.lateWorkSlot++;
        } else if (userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.SPORT) {
            userProfile.lateSportSlot++;
        }
        else if (userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.WORK_CATCH_UP) {
            userProfile.workCatchUp++;
        } else if (userProfile.agenda.get(converted_indice).get(endI) == timeSlot.currentTask.SPORT_CATCH_UP) {
            userProfile.sportCatchUp++;
        }

        for(int i = startI; i < (endI + 1); i++) {
            userProfile.agenda.get(converted_indice).set(i, dailyTasks.get(i-1));
            userProfile.newEventAgenda.get(converted_indice).set(i, newEvent.get(i-1));
        }


        //le StartI est prolongé
        if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.NEWEVENT) {
            for (newEvent event : userProfile.savedEvent) {
                if (event.name.equals(userProfile.newEventAgenda.get(converted_indice).get(startI))) {
                    if (event.sport) {
                        userProfile.lateSportSlot--;
                    } else if (event.work) {
                        userProfile.lateWorkSlot--;
                    }
                }
            }
        } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK ||
                userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK_FIX) {
            userProfile.lateWorkSlot--;
        } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.SPORT) {
            userProfile.lateSportSlot--;
        }
        else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.WORK_CATCH_UP) {
            userProfile.workCatchUp--;
        } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.currentTask.SPORT_CATCH_UP) {
            userProfile.sportCatchUp--;
        }

        updateDay();
        saveToFile();

        plan();                   // si jour actuelle, ne pas update ce qui est déja passé
        setAlarmOfTheDay(context);
    }

    private void plan(){
        int currentDay = 0;
        int dayOffset = 0;
        do {
            int nbWorkDay = 0;
            for (int j = 0; j < userProfile.freeDay.length; j++) {
                if (!userProfile.freeDay[j]) {
                    nbWorkDay++;
                }
            }
            int[] freedaylist = new int[nbWorkDay];
            int n = 0;
            for (int j = 0; j < userProfile.freeDay.length; j++) {
                if (!userProfile.freeDay[j]) {
                    freedaylist[n] = j;
                    n++;
                }
            }

            boolean freeday = true;
            boolean nextfreeday = true;
            boolean pastfreeday = true;
            for (int k = 0; k < freedaylist.length; k++) {
                if (freedaylist[k] == (currentDay + conversionDayIndice() + dayOffset) % 7) {
                    freeday = false;
                }
                if (freedaylist[k] == (currentDay + conversionDayIndice() + 1 + dayOffset) % 7) {
                    nextfreeday = false;
                }
                if (freedaylist[k] == (currentDay + conversionDayIndice() + 1 + 7 + dayOffset) % 7) {
                    pastfreeday = false;
                }
            }

            int val = (currentDay + convertedIndice() + dayOffset)%7; // à tester
            int dayCalcul = val;

            AgendaInitialisation agendaInitialisation = new AgendaInitialisation(userProfile, freeday, nextfreeday, pastfreeday, val,false);

            DayPlan Agent = new DayPlan(userProfile.weight, userProfile.canceled_slots.get(val), userProfile.sportDayRank,
                    userProfile.lastConnection, userProfile.settingDay, agendaInitialisation.daily_slots_generated,
                    userProfile.agenda.get(val), userProfile.newEventAgenda.get(val), dayCalcul, userProfile.savedEvent,
                    freeday, Integer.parseInt(userProfile.optWorkTime), userProfile.lateWorkSlot, userProfile.workCatchUp,
                    userProfile.sportRoutine, userProfile.lateSportSlot, userProfile.sportCatchUp, userProfile.agendaInit, false, false);
            Agent.planDay();

            userProfile.sportDayRank = Agent.rank;
            userProfile.agenda.set(val, Agent.dailyAgenda);
            userProfile.lateSportSlot = Agent.sportSlot;
            userProfile.lateWorkSlot = Agent.workSlot;
            userProfile.workCatchUp = Agent.workSlotCatchUp;
            userProfile.sportCatchUp = Agent.sportCatchUp;

            userProfile.updateFullAgenda(val);

            dayOffset++;
        } while ((userProfile.lateWorkSlot > 0 || userProfile.lateWorkSlot < -8 || userProfile.lateSportSlot != 0 ||
                userProfile.workCatchUp > 0 || userProfile.sportCatchUp > 0) && dayOffset < 7);
        saveToFile();
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
