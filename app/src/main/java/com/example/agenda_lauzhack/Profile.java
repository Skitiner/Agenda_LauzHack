package com.example.agenda_lauzhack;

import java.io.Serializable;

public class Profile implements Serializable {
    private static final String TAG = "Profile";

    protected String nbWorkHours;
    protected boolean[] freeDay;
    protected String wakeUp;
    protected int sportRoutine;

    public Profile(){
        this.nbWorkHours = "42";
        this.freeDay = new boolean[] {false, false, false, false, false, false, false};
        this.wakeUp = "8";
        sportRoutine = 1;
    }

    public Profile(String nbWorkHours, String wakeUp) {
        this.nbWorkHours = nbWorkHours;
        this.wakeUp = wakeUp;
    }
}
