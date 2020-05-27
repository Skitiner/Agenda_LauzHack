package com.ludiostrix.organise_mois;

import android.content.Context;

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

public class DaySlotsCalculation {

    private int nb_work_h;
    private boolean[] free_day;
    private int[] freedaylist;
    private float wake_up;
    private int sport_routine;
    private int offset;
    private int nbWorkDay;
    private Context context;
    private Profile userProfile = new Profile();
    private ArrayList<ArrayList<timeSlot.currentTask>> slots_generated;
    private IA Agent = new IA();

    public DaySlotsCalculation(Context context) {

        this.context = context;
        readFromFile();

        nb_work_h = Integer.parseInt(userProfile.nbWorkHours);
        free_day = userProfile.freeDay;
        nbWorkDay=0;
        offset = conversionDayIndice();
        for (int i=0 ; i < free_day.length ; i++){
            if (!free_day[i]) {
                nbWorkDay++;
            }
        }
        freedaylist = new int[nbWorkDay];
        int n = 0;
        for (int i=0 ; i < free_day.length ; i++){
            if (!free_day[i]) {
                freedaylist[n] = (i + (7-offset))%7;
                n++;
            }
        }
        wake_up = Float.parseFloat(userProfile.wakeUp);
        sport_routine = userProfile.sportRoutine;
        slots_generated = new ArrayList<>();
    }

    public int slotCalculation() {

        readFromFile();
        init();
        remove_canceled_days();
        setMorningRoutine();
        setNight();

        int nbFixedWork = compare(userProfile.agenda);

        int OK = 0;
        if (!(nbFixedWork > Integer.valueOf(userProfile.nbWorkHours))) {
            Boolean Sport = setSport();
            Boolean Work = setWork(nbFixedWork);

            if (!Sport){
                OK = 1;
            }
            else if (!Work){
                OK = 1;
            }
        }
        else {
            OK = 2;
        }

        if(OK == 0){
            userProfile.agenda = slots_generated;
            userProfile.settingDay = Calendar.getInstance();
            userProfile.settingDay.setTimeInMillis(System.currentTimeMillis());

            saveToFile();
            MainActivity.setAlarmOfTheDay(context);
        }
        return OK;
    }

    private void remove_canceled_days() {
        for (int i = 0; i < 7; i++) {
            for(int j = 0; j < 96; j++) {
                userProfile.canceled_slots.get(i).set(j, Boolean.FALSE);
            }
        }
    }
  
    public Boolean setWork(int nbFixedWork){

        int nbWorkTimeSlots = 0;

        nbWorkTimeSlots = nb_work_h - nbFixedWork;

        int nbWorkSlotsPerDay = nbWorkTimeSlots / nbWorkDay;
        int lostWorkTime = nbWorkTimeSlots % nbWorkDay;

        int[] memi = new int[nbWorkDay+1];
        int nbMoreWorkSlotPerDay = nbWorkSlotsPerDay;

        while (nbMoreWorkSlotPerDay > 0) {
            memi = SearchWorkTime(nbMoreWorkSlotPerDay);
            int nbpause = 0;

            if(!ismemiComplete(memi)){
                //Toast.makeText(this.context, R.string.bad_parameters, Toast.LENGTH_SHORT).show();
                break;
            }
            else {
                for (int i = 0; i < nbWorkDay; i++) {
                    if (memi[nbWorkDay] < 9) {
                        for (int j = 0; j < memi[nbWorkDay]; j++) {
                            slots_generated.get(freedaylist[i]).set(memi[i] + j, timeSlot.currentTask.WORK);
                        }
                    } else {
                        for (int k = 2; k < 12; k++) {
                            if (memi[nbWorkDay] < 8 * k) {
                                for (int j = 0; j < memi[nbWorkDay]; j++) {
                                    if ((j % (memi[nbWorkDay] / k) == (memi[nbWorkDay] / k) - 1) && memi[nbWorkDay] > j + 2) {
                                        slots_generated.get(freedaylist[i]).set(memi[i] + j, timeSlot.currentTask.PAUSE);
                                        nbpause++;
                                    } else {
                                        slots_generated.get(freedaylist[i]).set(memi[i] + j, timeSlot.currentTask.WORK);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                memi[nbWorkDay] = memi[nbWorkDay] - nbpause / nbWorkDay;
                nbMoreWorkSlotPerDay = nbMoreWorkSlotPerDay - memi[nbWorkDay];
            }
        }

        if (lostWorkTime>0 && ismemiComplete(memi)) {
            memi = SearchWorkTime(1);
            for (int i = 0; i < lostWorkTime; i++) {
                if (memi[i] == 0) {
                    // TODO: 05.04.2020 no possibilities found
                } else {
                    slots_generated.get(freedaylist[i]).set(memi[i], timeSlot.currentTask.WORK);
                }
            }
        }

        return ismemiComplete(memi);
    }

    private int[] SearchWorkTime(int nbWorkSlotsPerDay){

        boolean memiComplete = false;
        int[] memi;
        int [] memip = new int[nbWorkDay+1];

        for (int j = nbWorkSlotsPerDay; j > 0; j--) {
            memi = FreeTime(6*4, 21 * 4, j, true);
            for (int i = 0; i < nbWorkDay; i++) {
                memip[i] = memi[i];
            }
            memip[nbWorkDay] = j;
            memiComplete = ismemiComplete(memi);
            if (memiComplete){
                break;
            }
            else{
                memip[nbWorkDay] = 0;
            }
        }
        if (!memiComplete) {
            for (int j = nbWorkSlotsPerDay; j > 0; j--) {
                memi = FreeTime(0, (24 * 4)-1, j, true);
                for (int i = 0; i < nbWorkDay; i++) {
                    memip[i] = memi[i];
                }
                memip[nbWorkDay] = j;
                memiComplete = ismemiComplete(memi);
                if (memiComplete) {
                    break;
                } else {
                    memip[nbWorkDay] = 0;
                }
            }
        }

        return memip;
    }

    public Boolean setSport(){
        int nbSportTimeSlots = 0;
        switch (sport_routine){
            case 0:
            case 1: nbSportTimeSlots = 10;
                break;
            case 2: nbSportTimeSlots = 20;
                break;
        }
        int nbSportSlotsPerDay;
        if (nbWorkDay > 5){
            nbSportSlotsPerDay = nbSportTimeSlots/5;
        }
        else {
            nbSportSlotsPerDay = nbSportTimeSlots / nbWorkDay;
        }

        int[] memi;

        memi = SearchSportTime(nbSportSlotsPerDay);

        if(!ismemiComplete(memi)){
            //Toast.makeText(this.context, R.string.bad_parameters, Toast.LENGTH_SHORT).show();
        }
        else {
            for (int i = 0; i < nbWorkDay; i++) {
                for (int j = 0; j < nbSportSlotsPerDay; j++) {
                    slots_generated.get(freedaylist[i]).set(memi[i] + j, timeSlot.currentTask.SPORT);
                }
            }
        }
        return ismemiComplete(memi);
    }

    private int[] SearchSportTime(int nbSportSlotsPerDay){

        boolean memiComplete = false;
        int[] memi;

        memi = FreeTime(15*4, 19*4, nbSportSlotsPerDay, false);
        memiComplete = ismemiComplete(memi);

        if (!memiComplete){
            memi = FreeTime(8*4, 13*4, nbSportSlotsPerDay, true);
            memiComplete = ismemiComplete(memi);
        }

        if (!memiComplete){
            memi = FreeTime(19*4, 23*4, nbSportSlotsPerDay, true);
            memiComplete = ismemiComplete(memi);
        }

        if (!memiComplete){
            memi = FreeTime(0, 23*4, nbSportSlotsPerDay, true);
        }

        return memi;
    }

    private boolean ismemiComplete(int[] memi){
        boolean memiComplete = true;
        for (int i = 0; i < nbWorkDay; i++){
            if(memi[i]==0){
                memiComplete = false;
            }
        }
        return memiComplete;
    }
    // give a timeSlot, a timeSlot space and a boolean, 0 = search descent, 1 = search ascend
    //return a time slot if it found one, null however
    private int[] FreeTime(int timeSlotMin, int timeSlotMax, int timeSpace, boolean ascend){
        int freeTimeSlotCounter = 0;
        int[] memi = new int[nbWorkDay];
        for (int k = 0; k < nbWorkDay; k++){
            memi[k] = 0;
        }
        if (ascend){
            for (int j = 0; j < nbWorkDay; j++) {
                freeTimeSlotCounter=0;
                for (int i = timeSlotMin; i <= timeSlotMax; i++) {
                    if (slots_generated.get(freedaylist[j]).get(i) == timeSlot.currentTask.FREE){
                        freeTimeSlotCounter ++;
                        if (freeTimeSlotCounter >= timeSpace){
                            memi[j] = i - timeSpace + 1;
                            break;
                        }
                    }
                    else{
                        freeTimeSlotCounter = 0;
                    }
                }
            }
        }
        else {
            for (int j = 0; j < nbWorkDay; j++) {
                freeTimeSlotCounter=0;
                for (int i = timeSlotMax; i >= timeSlotMin; i--) {
                    if (slots_generated.get(freedaylist[j]).get(i) == timeSlot.currentTask.FREE){
                        freeTimeSlotCounter ++;
                        if (freeTimeSlotCounter >= timeSpace){
                            memi[j] = i;
                            break;
                        }
                    }
                    else{
                        freeTimeSlotCounter = 0;
                    }
                }
            }
        }
        return memi;
    }

    private void setNight(){
        for (int i = 0; i < freedaylist.length; i++) {
            for (int j = 0; j < 32; j++) {
                if ((wake_up*4)-j-1 < 0) {
                    this.slots_generated.get((freedaylist[i]-1+7)%7).set(slots_generated.get(0).size() + Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                }
                else {
                    this.slots_generated.get(freedaylist[i]).set(Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                }
            }
        }
    }

    private void setMorningRoutine() {
        for (int i = 0; i < freedaylist.length; i++) {
            for (int j = 0; j < 4; j++) {
                this.slots_generated.get(freedaylist[i]).set(Math.round(wake_up * 4) + j, timeSlot.currentTask.MORNING_ROUTINE);
            }
        }
    }

    private int compare(ArrayList<ArrayList<timeSlot.currentTask>> week_slots){
        int nbFixedWork = 0;
        for (int i = 0; i < free_day.length; i++) {
            for (int j = 0; j < week_slots.get(0).size(); j++) {
                if (week_slots.get(i).get(j) == timeSlot.currentTask.WORK_FIX || week_slots.get(i).get(j) == timeSlot.currentTask.EAT){
                    this.slots_generated.get(i).set(j,week_slots.get(i).get(j));
                    if (week_slots.get(i).get(j) == timeSlot.currentTask.WORK_FIX){
                        nbFixedWork++;
                    }
                }
            }
        }
        return nbFixedWork;
    }


    private void init(){
        timeSlot.currentTask task;

        for(int j = 0; j < 7; j++ ) {
            ArrayList<timeSlot.currentTask> tasks = new ArrayList<>();
            for (int i = 0; i < 96; i++) {

                task = timeSlot.currentTask.FREE;
                tasks.add(task);

            }
            this.slots_generated.add(tasks);
        }
    }
    private int conversionDayIndice() {
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
}
