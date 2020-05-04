package com.ludiostrix.organise_mois;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IA {
    protected Map<String,Integer> Task = new HashMap<>();
    protected ArrayList<ArrayList<ArrayList<Integer>>> Day;
    private ArrayList<ArrayList<Integer>> Hour;
    private ArrayList<Integer> Weight;

    public IA(){
        this.Task.put("Sport",0);
        this.Task.put("Work",1);           //Task.get("Sport")

        initArray();

    }

    private void initArray(){
        this.Weight = new ArrayList<Integer>();
        this.Weight.add(5);
        this.Weight.add(5);
        this.Hour = new ArrayList<ArrayList<Integer>>();
        for (int i = 0 ; i < 4*24; i++){
            this.Hour.add(this.Weight);
        }
        this.Day = new ArrayList<ArrayList<ArrayList<Integer>>>();
        for (int i = 0 ; i < 7; i++){
            this.Day.add(this.Hour);
        }
    }
}
