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
import java.util.List;

public class DaySlotsCalculation {

    private int nb_work_h;
    private boolean[] free_day;
    public int[] freedaylist;
    private float wake_up;
    private int sport_routine;
    private int offset;
    private int nbWorkDay;
    private Context context;
    private Profile userProfile = new Profile();
    public ArrayList<ArrayList<timeSlot.currentTask>> slots_generated;
    public ArrayList<ArrayList<timeSlot.currentTask>> futur_slots_generated;
    public ArrayList<timeSlot.currentTask> daily_slots_generated;
    private int nbSportTimeSlots = 0;
    private int weekSport;
    private float weekWork;

    public DaySlotsCalculation(Context context) {

        weekSport = userProfile.lateSportSlot;
        weekWork = userProfile.lateWorkSlot;

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
        futur_slots_generated = new ArrayList<>();
    }

    public DaySlotsCalculation(Profile userProfile, boolean freeday, boolean nextfreeday, int day, boolean convertInPastDay) {

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
        daily_slots_generated = new ArrayList<>();


        daily_init();
        if (!freeday) {
            setDailyMorningRoutine();
        }
        if (!freeday || !nextfreeday){
            setDailyNight(freeday, nextfreeday);
        }

        if (convertInPastDay){
            daily_compare(userProfile.futurAgenda.get(day), userProfile.canceled_slots.get(day));
        }
        else {
            daily_compare(userProfile.agenda.get(day), userProfile.canceled_slots.get(day));
        }
    }


    public int slotCalculation() {

        readFromFile();
        init();
        //countWeekWorkAndSport();          /l'idée était ne ne pas réinit le workslot et sportslot à zero mais c'est un peu plus compliqué que ca
        userProfile.remove_canceled_days();
        setMorningRoutine();
        setNight();

        int nbFixedWork = compare(userProfile.agenda);
        futurCompare();

        //initSportVariable();

        //List<IA> calculatedWeek;

        /*int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));*/


        userProfile.lateWorkSlot = Float.valueOf(0);
        userProfile.lateSportSlot = 0;
        for (int i = 0; i < slots_generated.size(); i++) {
            int val = i % 7; //(i + offset)%7;

            int nbWorkDay = 0;
            for (int j = 0; j < userProfile.freeDay.length; j++){
                if (!userProfile.freeDay[j]){
                    nbWorkDay++;
                }
            }
            boolean freeday = true;
            for (int  j = 0; j < freedaylist.length; j++) {
                //if (freedaylist[j] == (i + conversionDayIndice()%7)){
                if (freedaylist[j] == i){
                    freeday = false;
                }
            }

            if (i!=0) {         // aujourdhui, dépendemment de l'heure on initie diférement pour ne pas déja avoir du retard
                if (!freeday) {
                    this.userProfile.lateWorkSlot += Float.parseFloat(userProfile.nbWorkHours) / (float) nbWorkDay;
                }
                if (userProfile.sportRoutine == 2) {
                    userProfile.lateSportSlot += 4;
                } else {
                    userProfile.lateSportSlot += 2;
                }
            }
            else {
                int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if (currentHour < 20 && wake_up < currentHour){
                    if (!freeday) {
                        this.userProfile.lateWorkSlot += ((Float.parseFloat(userProfile.nbWorkHours) / (float) nbWorkDay)
                                * (Float.valueOf(20)-(float)currentHour)/(Float.valueOf(20)-(wake_up+1)));       // coeff < 1 réprensentant le pourcentage d'heure de la journée
                    }
                    if (userProfile.sportRoutine == 2) {
                        userProfile.lateSportSlot += 4;
                    } else {
                        userProfile.lateSportSlot += 2;
                    }
                }
                else if(currentHour < 20){
                    if (!freeday) {
                        this.userProfile.lateWorkSlot += Float.parseFloat(userProfile.nbWorkHours) / (float) nbWorkDay;
                    }
                    if (userProfile.sportRoutine == 2) {
                        userProfile.lateSportSlot += 4;
                    } else {
                        userProfile.lateSportSlot += 2;
                    }
                }
            }

            IA Agent = new IA(userProfile.weight, userProfile.canceled_slots.get(val), userProfile.sportDayRank,
                    userProfile.lastConnection, userProfile.settingDay, slots_generated.get(val),
                    userProfile.agenda.get(val), userProfile.newEventAgenda.get(val), val,
                    userProfile.savedEvent, freeday, Integer.parseInt(userProfile.optWorkTime),
                    userProfile.lateWorkSlot, userProfile.sportRoutine, userProfile.lateSportSlot, userProfile.agendaInit);
            userProfile.agendaInit = false;
            Agent.planDay();
            userProfile.agenda.set(val, Agent.dailyAgenda);
            userProfile.lateSportSlot = Agent.sportSlot;
            userProfile.lateWorkSlot = Agent.workSlot;

            int position;
            for(int j = 0; j < 96; j +=4 ) {
                position = j/4;
                userProfile.fullAgenda.get(val).get(position).task_1 = userProfile.agenda.get(val).get(j);
                userProfile.fullAgenda.get(val).get(position).task_2 = userProfile.agenda.get(val).get(j+1);
                userProfile.fullAgenda.get(val).get(position).task_3 = userProfile.agenda.get(val).get(j+2);
                userProfile.fullAgenda.get(val).get(position).task_4 = userProfile.agenda.get(val).get(j+3);
            }

            for(int j = 0; j < 96; j +=4 ) {
                position = j/4;
                userProfile.futurFullAgenda.get(val).get(position).task_1 = futur_slots_generated.get(val).get(j);
                userProfile.futurFullAgenda.get(val).get(position).task_2 = futur_slots_generated.get(val).get(j+1);
                userProfile.futurFullAgenda.get(val).get(position).task_3 = futur_slots_generated.get(val).get(j+2);
                userProfile.futurFullAgenda.get(val).get(position).task_4 = futur_slots_generated.get(val).get(j+3);
            }

            saveToFile();
        }


        MainActivity.setAlarmOfTheDay(context);
        /*int OK = 0;
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
            //userProfile.agenda = slots_generated;
            //userProfile.agenda.set(0, Agent.dailyAgenda);
            int pos;
            for (int indice = 0; indice < userProfile.fullAgenda.size(); indice++) {
                for (int i = 0; i < 96; i += 4) {
                    pos = i / 4;
                    userProfile.fullAgenda.get(indice).get(pos).task_1 = userProfile.agenda.get(indice).get(i);
                    userProfile.fullAgenda.get(indice).get(pos).task_2 = userProfile.agenda.get(indice).get(i + 1);
                    userProfile.fullAgenda.get(indice).get(pos).task_3 = userProfile.agenda.get(indice).get(i + 2);
                    userProfile.fullAgenda.get(indice).get(pos).task_4 = userProfile.agenda.get(indice).get(i + 3);
                }
            }
        }
        return OK;*/
        return 0;
    }

    private void countWeekWorkAndSport(){
        for (int i = 0; i < userProfile.agenda.size();i++){
            for (int j = 0; j < userProfile.agenda.get(i).size(); j++){
                if (!userProfile.canceled_slots.get(i).get(j)) {
                    if (userProfile.agenda.get(i).get(j) == timeSlot.currentTask.WORK_FIX ||
                            userProfile.agenda.get(i).get(j) == timeSlot.currentTask.WORK) {
                        weekWork++;
                    } else if (userProfile.agenda.get(i).get(j) == timeSlot.currentTask.SPORT) {
                        weekSport++;
                    } else if (userProfile.agenda.get(i).get(j) == timeSlot.currentTask.NEWEVENT) {
                        for (newEvent event : userProfile.savedEvent) {
                            if (event.sport) {
                                weekSport++;
                            } else if (event.work) {
                                weekWork++;
                            }
                        }
                    }
                }
            }
        }
    }
  
    public Boolean setWork(int nbFixedWork){

        int nbWorkTimeSlots;

        nbWorkTimeSlots = nb_work_h - nbFixedWork;

        if (nbWorkTimeSlots == 0){
            return true;
        }
        else {
            int nbWorkSlotsPerDay = nbWorkTimeSlots / nbWorkDay;
            int lostWorkTime = nbWorkTimeSlots % nbWorkDay;

            int[] memi = new int[nbWorkDay + 1];

            int nbMoreWorkSlotPerDay = nbWorkSlotsPerDay;

            while (nbMoreWorkSlotPerDay > 0) {
                memi = SearchWorkTime(nbMoreWorkSlotPerDay);
                int nbpause = 0;

                if (!ismemiComplete(memi)) {
                    //Toast.makeText(this.context, R.string.bad_parameters, Toast.LENGTH_SHORT).show();
                    break;
                } else {
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

            if (lostWorkTime > 0 && ismemiComplete(memi)) {
                memi = SearchWorkTime(1);
                for (int i = 0; i < lostWorkTime; i++) {
                    if (memi[i] == -1) {
                        // TODO: 05.04.2020 no possibilities found
                    } else {
                        slots_generated.get(freedaylist[i]).set(memi[i], timeSlot.currentTask.WORK);
                    }
                }
            }

            return ismemiComplete(memi);
        }
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
  
    public void initSportVariable(){
        switch (sport_routine){
            case 0:
            case 1: nbSportTimeSlots = 14;
                break;
            case 2: nbSportTimeSlots = 28;
                break;
        }

    }

    public Boolean setSport(){

        int nbSportSlotsPerDay;
        if (nbWorkDay > 5){
            nbSportSlotsPerDay = nbSportTimeSlots/5;
        }
        else {
            nbSportSlotsPerDay = nbSportTimeSlots / nbWorkDay;
        }

        switch (sport_routine){
            case 0:
            case 1:
                if(nbSportSlotsPerDay > 7){
                    nbSportSlotsPerDay = 7;
                }
                break;
            case 2:
                if(nbSportSlotsPerDay > 8){
                    nbSportSlotsPerDay = 8;
                }
                break;
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
            if(memi[i]==-1){
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
            memi[k] = -1;
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

    private void setDailyNight(boolean freeday, boolean nextfreeday){
        for (int j = 0; j < 32; j++) {
            if ((wake_up*4)-j-1 < 0) {
                if (!nextfreeday) {
                    this.daily_slots_generated.set(daily_slots_generated.size() + Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                }
            }
            else if (!freeday) {
                this.daily_slots_generated.set(Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
            }
        }
    }

    private void setDailyMorningRoutine() {
        for (int j = 0; j < 4; j++) {
            this.daily_slots_generated.set(Math.round(wake_up * 4) + j, timeSlot.currentTask.MORNING_ROUTINE);
        }
    }

    private void setNight(){
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 32; j++) {
                for (int k = 0; k < freedaylist.length; k ++){
                    if ((wake_up*4)-j-1 < 0) {
                        if (freedaylist[(k+1) % freedaylist.length] == (i + 1) % 7) {
                            this.slots_generated.get(i).set(slots_generated.get(0).size() + Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                            this.futur_slots_generated.get(i).set(futur_slots_generated.get(0).size() + Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                        }
                    }
                    else if (i == freedaylist[k]){
                        this.slots_generated.get(freedaylist[k]).set(Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                        this.futur_slots_generated.get(freedaylist[k]).set(Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                    }

                }
                /*if ((wake_up*4)-j-1 < 0) {
                    if (freedaylist[(i+1) % freedaylist.length] == (freedaylist[i] + 1) % 7) {
                        this.slots_generated.get(freedaylist[i]).set(slots_generated.get(0).size() + Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                    }
                }
                else {
                    this.slots_generated.get(freedaylist[i]).set(Math.round(wake_up * 4) - j - 1, timeSlot.currentTask.SLEEP);
                }*/
            }
        }
    }

    private void setMorningRoutine() {
        for (int i = 0; i < freedaylist.length; i++) {
            for (int j = 0; j < 4; j++) {
                this.slots_generated.get(freedaylist[i]).set(Math.round(wake_up * 4) + j, timeSlot.currentTask.MORNING_ROUTINE);
                this.futur_slots_generated.get(freedaylist[i]).set(Math.round(wake_up*4) + j, timeSlot.currentTask.MORNING_ROUTINE);
            }
        }
    }

    private void daily_compare(ArrayList<timeSlot.currentTask> day_slots, List<Boolean> day_canceledSlots){
        for (int j = 0; j < day_slots.size(); j++) {
            if (day_slots.get(j) == timeSlot.currentTask.WORK_FIX || day_slots.get(j) == timeSlot.currentTask.EAT ||
                    day_slots.get(j) == timeSlot.currentTask.NEWEVENT || day_canceledSlots.get(j)){
                this.daily_slots_generated.set(j,day_slots.get(j));
            }
        }
    }


    private void daily_init(){
        timeSlot.currentTask task;

        for (int i = 0; i < 96; i++) {

            task = timeSlot.currentTask.FREE;
            this.daily_slots_generated.add(task);
        }
    }

    private int compare(ArrayList<ArrayList<timeSlot.currentTask>> week_slots){
        int nbFixedWork = 0;
        for (int i = 0; i < free_day.length; i++) {
            for (int j = 0; j < week_slots.get(0).size(); j++) {
                if (week_slots.get(i).get(j) == timeSlot.currentTask.WORK_FIX || week_slots.get(i).get(j) == timeSlot.currentTask.EAT || week_slots.get(i).get(j) == timeSlot.currentTask.NEWEVENT){
                    this.slots_generated.get(i).set(j,week_slots.get(i).get(j));
                    if (week_slots.get(i).get(j) == timeSlot.currentTask.WORK_FIX){
                        nbFixedWork++;
                    }
                }
            }
        }
        return nbFixedWork;
    }

    private void futurCompare(){
        for (int i = 0; i < free_day.length; i++) {
            for (int j = 0; j < userProfile.futurAgenda.get(0).size(); j++) {
                if (userProfile.futurAgenda.get(i).get(j) == timeSlot.currentTask.WORK_FIX || userProfile.futurAgenda.get(i).get(j) == timeSlot.currentTask.EAT ||
                        userProfile.futurAgenda.get(i).get(j) == timeSlot.currentTask.NEWEVENT){
                    this.futur_slots_generated.get(i).set(j,userProfile.futurAgenda.get(i).get(j));

                }
            }
        }
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
        for(int j = 0; j < 7; j++ ) {
            ArrayList<timeSlot.currentTask> tasks = new ArrayList<>();
            for (int i = 0; i < 96; i++) {

                task = timeSlot.currentTask.FREE;
                tasks.add(task);

            }
            this.futur_slots_generated.add(tasks);
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

            updateFullAgenda();

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

    public void updateFullAgenda(){
        int position;
        for(int j = 0; j < 7; j++) {
            for (int i = 0; i < 96; i += 4) {
                position = i / 4;
                userProfile.fullAgenda.get(j).get(position).task_1 = userProfile.agenda.get(j).get(i);
                userProfile.fullAgenda.get(j).get(position).task_2 = userProfile.agenda.get(j).get(i + 1);
                userProfile.fullAgenda.get(j).get(position).task_3 = userProfile.agenda.get(j).get(i + 2);
                userProfile.fullAgenda.get(j).get(position).task_4 = userProfile.agenda.get(j).get(i + 3);
            }
        }
    }
}
