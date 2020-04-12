package com.example.agenda_lauzhack;

import android.util.Log;
import android.view.ViewDebug;

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
        this.wakeUp = "8.0";
        this.sportRoutine = 1;
        calculation = false;
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

    public boolean stringTimewakeUpToFloat(String time){
        boolean ok;
        boolean entier = true;
        String hour = "0";
        String min = "0";
        float hourt;
        float mint;

        for (int i = 0; i < time.length(); i++){
            if(time.charAt(i) != ':' && entier) {
                hour += time.charAt(i);
            }
            else if (time.charAt(i) != ':'){
                min += time.charAt(i);
            }
            else if (time.charAt(i) == ':'){
                entier = false;
            }
        }
        try {
            hourt = Float.parseFloat(hour);
            mint = Float.parseFloat(min);
            ok = true;
            if (hourt > 23 || hourt < 0){
                ok = false;
            }
            if (mint >= 60 || mint < 0){
                ok = false;
            }
            else {
                hourt = hourt + roundFifty(mint/60);
                while (hourt>=24){
                    hourt -=24;
                }
            }
            if(ok){
                this.wakeUp = String.valueOf(hourt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok=false;
        }
        return ok;
    }

    public String floatStringToTimeString(String floatTime){
        boolean entier = true;
        String hour = "";
        String min = "";

        for (int i = 0; i < floatTime.length(); i++){
            if(floatTime.charAt(i) != '.' && entier) {
                hour += floatTime.charAt(i);
            }
            else if (floatTime.charAt(i) != '.'){
                min += floatTime.charAt(i);
            }
            else if (floatTime.charAt(i) == '.'){
                hour += ':';
                entier = false;
            }
        }

        int mint = Integer.parseInt(min)*60;
        min = String.valueOf(mint);
        if (min.length() > 2){
            min = min.substring(0,2);
        }
        if (min.length()<2){
            while(min.length()<2){
                min += '0';
            }
        }
        hour += min;
        return hour;
    }
    private float roundFifty(float fmin){
        float min = 0;
        final float zero = 0;
        final float one = 1;
        final float two = 2;
        final float three = 3;
        final float four = 4;
        final float five = 5;
        final float seven = 7;
        final float height = 8;

        if (fmin >= zero && fmin < one/height){
            min = 0;
        }
        else if (fmin >= one/height && fmin < three/height){
            min = one/four;
        }
        else if (fmin >= three/height && fmin < five/height){
            min = one/two;
        }
        else if (fmin >= five/height && fmin < seven/height){
            min = three/four;
        }
        else if (fmin >= seven/height && fmin < 1){
            min = 1;
        }

        return min;
    }
}
