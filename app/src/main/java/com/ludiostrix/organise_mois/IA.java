package com.ludiostrix.organise_mois;

import android.service.autofill.AutofillService;

import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IA {
    protected Map<String,Integer> Task = new HashMap<>();
    protected ArrayList<ArrayList<ArrayList<Integer>>> Day;
    private ArrayList<ArrayList<Integer>> Hour;
    private ArrayList<Integer> dayWeight;
    private ArrayList<Integer> nightWeight;
    private ArrayList<Integer> bonusScore = new ArrayList<Integer>();
    private ArrayList<ArrayList<Integer>> freeSlot = new ArrayList<ArrayList<Integer>>();
    private ArrayList<ArrayList<Integer>> workSlot = new ArrayList<ArrayList<Integer>>();
    private Profile userProfile;
    public ArrayList<ArrayList<timeSlot.currentTask>> calculatedAgenda;

    private ArrayList<Integer> preSelectedDay = new ArrayList<>();
    private ArrayList<Integer> selectedDay = new ArrayList<>();
    int nbSlotSportHourPerDay;
    int nbSlotSupp;


    public IA(Profile userprofile, int[] freeDayList, int work, int sport){
        this.Task.put("Sport",0);
        this.Task.put("Work",1);           //Task.get("Sport")

        this.userProfile = userprofile;

        initBonusScore();
        initWeight();

        setSport(sport);

        //searchFreeSlot(agenda, freeDayList);

    }


    private void searchFreeSlot(ArrayList<ArrayList<timeSlot.currentTask>> agenda, int[] freeDayList) {
        int nbWorkDay = freeDayList.length;
        for (int j = 0; j < nbWorkDay; j++) {
            ArrayList<Integer> freeDaySlot = new ArrayList<Integer>();
            for (int i = 0; i < agenda.get(freeDayList[j]).size(); i++) {
                if (agenda.get(freeDayList[j]).get(i) == timeSlot.currentTask.FREE){
                    freeDaySlot.add(i);
                }
            }
            this.freeSlot.add(freeDaySlot);
        }

    }

    private void initBonusScore() {
        this.bonusScore.add(-10);
        this.bonusScore.add(-5);
        this.bonusScore.add(-3);
        this.bonusScore.add(0);
        this.bonusScore.add(1);
        this.bonusScore.add(2);
        this.bonusScore.add(3);
        this.bonusScore.add(3);
        this.bonusScore.add(3);
        this.bonusScore.add(0);
        this.bonusScore.add(-2);
        this.bonusScore.add(-2);
        this.bonusScore.add(-5);
        this.bonusScore.add(-10);
    }

    private void initWeight(){
        this.dayWeight = new ArrayList<Integer>();
        this.dayWeight.add(6);
        this.dayWeight.add(6);
        this.nightWeight = new ArrayList<Integer>();
        this.nightWeight.add(4);
        this.nightWeight.add(4);
        this.Hour = new ArrayList<ArrayList<Integer>>();
        for (int i = 0 ; i < 4*24; i++){
            if (i < 7 || i > 21){
                this.Hour.add(this.nightWeight);
            }
            else {
                this.Hour.add(this.dayWeight);
            }
        }
        this.Day = new ArrayList<ArrayList<ArrayList<Integer>>>();
        for (int i = 0 ; i < 7; i++){
            this.Day.add(this.Hour);
        }

        // int test = this.Hour.get(12).get(Task.get("Sport"));
    }
    private void setSport(int sport){
        preSelectDay();

        verifySelection(sport);




    }

    private void verifySelection(int sport){
        selectedDay = preSelectedDay;
        for (int i : selectedDay){
            nbSlotSportHourPerDay = sport/selectedDay.size();
            nbSlotSupp = sport%selectedDay.size();

            ArrayList<Integer> alreadySport = new ArrayList<>(isAlreadySport(i));
            int nbSportHour;
            boolean slotFound = false;

            for (nbSportHour = nbSlotSportHourPerDay + 1; nbSportHour >= nbSlotSportHourPerDay; nbSportHour--) {

                if (alreadySport != null) {
                    for (int j = 0; j < alreadySport.size(); j++) {
                        int start = alreadySport.get(j);
                        int stop = alreadySport.get(j);
                        int k;
                        for (k = j + 1; k < alreadySport.size(); k++) {
                            if (start + k != alreadySport.get(k)) {
                                stop = alreadySport.get(k - 1);
                                break;
                            }
                        }

                        if (isFreeSlotAround(i, start, stop, nbSportHour)) {
                            slotFound = true;
                            break;
                        }
                        j = j + (k - 1);
                    }

                    if (!slotFound) {
                        if (isFreeSlot(i, nbSportHour)) {
                            slotFound = true;
                        }
                    }
                } else {
                    if (isFreeSlot(i, nbSportHour)) {
                        slotFound = true;
                    }
                }
            }
            if (!slotFound) {
                selectedDay.remove(i);
            } else if (nbSportHour == nbSlotSportHourPerDay + 1) {
                nbSlotSupp -= 1;
                break;
            }
        }
    }

    private boolean isFreeSlotAround(int searchDay, int start, int stop, int nbSlot) {
        boolean isFree = true;
        int count = 0;
        for (int i = 0; i < nbSlot; i++){
            if (userProfile.agenda.get(searchDay).get(stop + i) == timeSlot.currentTask.FREE){
                count++;
            }
            else
                break;
        }
        for (int i = 0; i < nbSlot; i++){
            if (userProfile.agenda.get(searchDay).get(start -1) == timeSlot.currentTask.FREE){
                count++;
            }
            else
                break;
        }
        if (count < nbSlot){
            isFree = false;
        }

        return isFree;
    }

    private boolean isFreeSlot (int searchDay, int nbSlot){
        boolean isFree = false;
        int count = 0;
        int countMax = 0;
        for (int i = 0; i < userProfile.agenda.get(searchDay).size(); i++){
            if (count > nbSlot || (count > (nbSlot/2 + nbSlot%2) && countMax > nbSlot/2) ||
                                        (countMax > (nbSlot/2 + nbSlot%2) && count > nbSlot/2) ) {
                isFree = true;
                break;
            }
            else if (userProfile.agenda.get(searchDay).get(i) == timeSlot.currentTask.FREE) {
                count++;
            } else {
                if (count > countMax) {
                    countMax = count;
                }
                count = 0;
            }
        }
        return isFree;
    }

    private void preSelectDay(){
        ArrayList<Integer> meanPerDay = new ArrayList<>();
        double meanMax = 0;
        double bestDaySelection = 0.05;

        for (int i = 0; i < userProfile.agenda.size(); i++){
            meanPerDay.add(Integer.valueOf(0));
            for (int j = 0; j < userProfile.agenda.get(i).size(); j++){
                meanPerDay.set(i, meanPerDay.get(i) + this.Day.get(j).get(i).get(Task.get("Sport")));
            }
            meanPerDay.set(i, meanPerDay.get(i)/userProfile.agenda.get(i).size());
            if (meanMax < meanPerDay.get(i)){
                meanMax = meanPerDay.get(i);
            }
        }

        for (int i =0; i < meanPerDay.size(); i++){
            if (meanPerDay.get(i) > meanMax - bestDaySelection){
                preSelectedDay.add(i);
            }
        }
    }

    // Search if there is already some sport during the day
    // return all the timeslot where there is Sport
    private ArrayList<Integer> isAlreadySport(int dayToSearch){
        ArrayList<Integer> sportSlot = new ArrayList<>();

        for (int i = 0; i < userProfile.agenda.get(dayToSearch).size(); i++){
            if (userProfile.agenda.get(dayToSearch).get(i) == timeSlot.currentTask.NEWEVENT){
                //cherche le nom de l'evenement
                String eventName = "";
                if (i%4 == 0){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_1;
                } else if(i%4 == 1){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_2;
                } else if(i%4 == 2){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_3;
                } else if(i%4 == 3){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_4;
                }
                // vérifie si l'événement est du sport
                for (int j = 0; j < userProfile.savedEvent.size(); j++){
                    if (userProfile.savedEvent.get(j).name.equals(eventName)){
                        if (userProfile.savedEvent.get(j).sport){
                            sportSlot.add(i);
                        }
                        break;
                    }
                }
            }
        }

        return sportSlot;
    }

    private class freeLocation{
        int position;
        double score;
    }
}


