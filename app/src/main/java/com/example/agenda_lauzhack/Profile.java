package com.example.agenda_lauzhack;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class Profile implements Serializable {
    private static final String TAG = "Profile";

    protected boolean licenceAccepted;
    protected String nbWorkHours;
    protected boolean[] freeDay;
    protected String wakeUp;
    protected int sportRoutine;
    protected boolean calculation;
    protected String FileName;
    protected ArrayList<ArrayList<timeSlot.currentTask>> agenda;
    protected int k;
    protected String current;
    protected ArrayList<String> agenda_back;

    public Profile(){
        agenda_back = new ArrayList<>();
        this.licenceAccepted = false;
        this.nbWorkHours = "42";
        this.freeDay = new boolean[] {false, false, false, false, false, false, false};
        this.wakeUp = "8";
        this.sportRoutine = 1;
        calculation = true;
        this.FileName = "userProfile.txt";
        initAgenda();
        k = 0;
        current = new String();
    }

    private void initAgenda() {
        agenda = new ArrayList<>();
        timeSlot.currentTask task;
        for(int j = 0; j < 7; j++ ) {
            ArrayList<timeSlot.currentTask> tasks = new ArrayList<>();
            for (int i = 0; i < 24; i++) {

                task = timeSlot.currentTask.FREE;

                timeSlot slot = new timeSlot();
                slot.time = i;
                slot.task_1 = task;
                slot.task_2 = task;
                slot.task_3 = task;
                slot.task_4 = task;

                tasks.add(slot.task_1);
                tasks.add(slot.task_2);
                tasks.add(slot.task_3);
                tasks.add(slot.task_4);
            }
            agenda.add(tasks);
        }
    }

    public void Save(BufferedWriter bufferedWriter){
        try {
            bufferedWriter.write(String.valueOf(this.licenceAccepted));
            bufferedWriter.write("/");
            bufferedWriter.write(this.nbWorkHours);
            bufferedWriter.write("/");
            for(int i=0; i < this.freeDay.length; i ++) {
                if (this.freeDay[i]){
                    bufferedWriter.write('1');
                }
                else {
                    bufferedWriter.write('0');
                }
            }
            bufferedWriter.write("/");
            bufferedWriter.write(this.wakeUp);
            bufferedWriter.write("/");
            bufferedWriter.write(String.valueOf(this.sportRoutine));
            bufferedWriter.write("/");
            bufferedWriter.write(agenda.toString());
            bufferedWriter.write("/");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode(String lineData) {
        String lA="";
        String nW="";
        Character[] fD= new Character[] {'0','0','0','0','0','0','0'};
        String wU="";
        agenda_back = new ArrayList<>();
        String sR="";
        int j = 0;
        int n = 0;

        for (int i=0;i<lineData.length();i++) {
            if (lineData.charAt(i) !='/'){
                switch (j){
                    case 0 : lA += lineData.charAt(i);
                             break;
                    case 1 : nW += lineData.charAt(i);
                             break;
                    case 2 : fD[n] = lineData.charAt(i);
                             n++;
                             break;
                    case 3 : wU += lineData.charAt(i);
                             break;
                    case 4 : sR += lineData.charAt(i);
                             break;
                    case 5 :
                            getAgenda(lineData.charAt(i));
                            break;

                }
            }
            else {
                j++;
            }
        }

        writeInAgenda();

        this.licenceAccepted = Boolean.parseBoolean(lA);
        this.nbWorkHours = nW;
        for(int i=0; i < fD.length; i ++) {
            if (fD[i] == '1') {
                this.freeDay[i] = true;
            }
            else {
                this.freeDay[i] = false;
            }
        }
        this.wakeUp = wU;
        this.sportRoutine = Integer.parseInt(sR);

    }

    private void writeInAgenda() {
        for(int i = 0; i < agenda_back.size(); i++) {
            int j = i/96;
            int k = i%96;

            if ("SPORT".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.SPORT);

            } else if ("WORK".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.WORK);

            } else if ("EAT".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.EAT);

            } else if ("FREE".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.FREE);

            } else if ("WORK_FIX".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.WORK_FIX);

            } else if ("MORNING_ROUTINE".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.MORNING_ROUTINE);

            } else if ("SLEEP".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.SLEEP);

            } else if ("PAUSE".equals(agenda_back.get(i))) {
                agenda.get(j).set(k, timeSlot.currentTask.PAUSE);
            }
        }

        Log.w("AGENDA_BACK", agenda_back.toString());
        Log.w("AGENDA", agenda.toString());
    }

    private void getAgenda(char charAt) {
        if(charAt == '[' || charAt == ' ' || charAt == ']')
            return;
        else if(charAt == ',') {
            agenda_back.add(current);
            current = new String();
            k++;
        }
        else
            current = current + charAt;

    }
}
