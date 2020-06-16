package com.ludiostrix.organise_mois;

import java.util.ArrayList;
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


    public IA(ArrayList<ArrayList<timeSlot.currentTask>> agenda, int[] freeDayList, int work, int sport){
        this.Task.put("Sport",0);
        this.Task.put("Work",1);           //Task.get("Sport")

        initBonusScore();
        initWeight();
        searchFreeSlot(agenda, freeDayList);

        initIAWorkSlot();

    }

    private void initIAWorkSlot(){

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
}
