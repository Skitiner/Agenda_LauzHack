package com.ludiostrix.organise_mois;

import java.io.Serializable;

/*

Cette classe représente la structure du fullAgenda.

 */

public class TimeSlot implements Serializable {
    public enum CurrentTask {SPORT, WORK, EAT, FREE, WORK_FIX, MORNING_ROUTINE, SLEEP, PAUSE, NEWEVENT, WORK_CATCH_UP, SPORT_CATCH_UP}

    protected int time;
    protected CurrentTask task_1;
    protected CurrentTask task_2;
    protected CurrentTask task_3;
    protected CurrentTask task_4;

    protected String new_task_1;
    protected String new_task_2;
    protected String new_task_3;
    protected String new_task_4;

    public TimeSlot(){

    }

    public TimeSlot(TimeSlot task){
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
