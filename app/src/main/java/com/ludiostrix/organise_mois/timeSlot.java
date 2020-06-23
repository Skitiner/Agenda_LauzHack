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

    public timeSlot(){

    }

    public timeSlot(timeSlot task){
        this.time = task.time;

        this.task_1 = task.task_1;
        this.task_2 = task.task_2;
        this.task_3 = task.task_3;
        this.task_4 = task.task_4;

        this.new_task_1 = task.new_task_1;
        this.new_task_2 = task.new_task_2;
        this.new_task_3 = task.new_task_3;
        this.new_task_4 = task.new_task_4;
    }
}
