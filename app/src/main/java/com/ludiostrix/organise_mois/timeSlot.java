package com.ludiostrix.organise_mois;

import java.io.Serializable;

public class timeSlot implements Serializable {
    public enum currentTask {SPORT, WORK, EAT, FREE, WORK_FIX, MORNING_ROUTINE, SLEEP, PAUSE, NEWEVENT}

    protected int time;
    protected currentTask task_1;
    protected currentTask task_2;
    protected currentTask task_3;
    protected currentTask task_4;

    protected String new_task_1;
    protected String new_task_2;
    protected String new_task_3;
    protected String new_task_4;
}
