package com.example.agenda_lauzhack;

import java.io.Serializable;

public class Profile implements Serializable {
    private static final String TAG = "Profile";

    protected boolean licenceAccepted;
    protected String nbWorkHours;
    protected boolean[] freeDay;
    protected String wakeUp;
    protected int sportRoutine;

    public Profile(){
        this.licenceAccepted = false;
        this.nbWorkHours = "42";
        this.freeDay = new boolean[] {false, false, false, false, false, false, false};
        this.wakeUp = "8";
        this.sportRoutine = 1;
    }

}
