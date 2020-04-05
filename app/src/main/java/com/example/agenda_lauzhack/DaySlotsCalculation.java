package com.example.agenda_lauzhack;

import android.widget.Toast;

import java.util.ArrayList;

public class DaySlotsCalculation {

    private int nb_work_h;
    private int sport_routine;
    private ArrayList<ArrayList<timeSlot.currentTask>> slots_generated;

    public DaySlotsCalculation(Profile user) {
        nb_work_h = Integer.parseInt(user.nbWorkHours);
        sport_routine = user.sportRoutine;
        slots_generated = new ArrayList<>();
    }

    public ArrayList<ArrayList<timeSlot.currentTask>> slotCalculation(ArrayList<ArrayList<timeSlot.currentTask>> week_slots) {

        slots_generated = week_slots;

        for (int i = 0; i < 7; i++) {
            slots_generated.get(i).set(0, timeSlot.currentTask.SPORT);
        }
        return slots_generated;
    }
}
