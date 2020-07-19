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


/*

Cette classe est utilisée quand l'utilisateur annule un événement.
Elle met à jour le score du travail et du sport si nécessaire et replanifie le planning du jour.

 */

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

            if (startI + NB_SLOT_MAX_CANCELED < 96) {
                if (userProfile.agenda.get(converted_indice).get(i) != userProfile.agenda.get(converted_indice).get(startI))
                    break;

                userProfile.canceled_slots.get(converted_indice).set(i, Boolean.TRUE);


                //update weight
                if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.WORK &&
                        userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Work")) > 0) {
                    userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Work"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) - 1);
                    //userProfile.lateWorkSlot++;
                    userProfile.workCatchUp++;
                } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.WORK_CATCH_UP &&
                        userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Work")) > 0) {
                    userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Work"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Work")) - 1);
                } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.SPORT &&
                        userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Sport")) > 0) {
                    userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Sport"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) - 1);
                    //userProfile.lateSportSlot++;
                    userProfile.sportCatchUp++;
                } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.SPORT_CATCH_UP &&
                        userProfile.weight.get(conversionDayIndice()).get(i).get(userProfile.Task.get("Sport")) > 0) {
                    userProfile.weight.get(conversionDayIndice()).get(i).set(userProfile.Task.get("Sport"), userProfile.weight.get(converted_indice).get(i).get(userProfile.Task.get("Sport")) - 1);
                } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.WORK_FIX) {
                    //userProfile.lateWorkSlot++;
                    userProfile.workCatchUp++;
                } else if (userProfile.agenda.get(converted_indice).get(startI) == timeSlot.CurrentTask.NEWEVENT) {
                    for (newEvent event : userProfile.savedEvent) {
                        if (event.name.equals(userProfile.newEventAgenda.get(converted_indice).get(startI))) {
                            if (event.sport) {
                                //userProfile.lateSportSlot++;
                                userProfile.sportCatchUp++;
                            } else if (event.work) {
                                //userProfile.lateWorkSlot++;
                                userProfile.workCatchUp++;
                            }
                        }
                    }
                }
            }
            else break;
        }

        saveToFile();

        plan();

        MainActivity.setAlarmOfTheDay(context);
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
            /*int position;
            for (int j = 0; j < 96; j += 4) {
                position = j / 4;
                userProfile.fullAgenda.get(val).get(position).task_1 = userProfile.agenda.get(val).get(j);
                userProfile.fullAgenda.get(val).get(position).task_2 = userProfile.agenda.get(val).get(j + 1);
                userProfile.fullAgenda.get(val).get(position).task_3 = userProfile.agenda.get(val).get(j + 2);
                userProfile.fullAgenda.get(val).get(position).task_4 = userProfile.agenda.get(val).get(j + 3);
            }*/

            /*do {
                freeday = true;
                for (int k = 0; k < freedaylist.length; k++) {
                    if (freedaylist[k] == (conversionDayIndice() + dayOffset + date_offset) % 7) { // (daySinceLastConnection / 7) * 7) make it >= 0
                        freeday = false;
                    }
                }
                dayOffset++;
            } while (!freeday);*/
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
