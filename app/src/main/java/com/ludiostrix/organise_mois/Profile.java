package com.ludiostrix.organise_mois;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Profile implements Serializable {
    private static final String TAG = "Profile";

    protected boolean licenceAccepted;
    protected String nbWorkHours;
    protected String optWorkTime;
    protected boolean[] freeDay;
    protected String wakeUp;
    protected int sportRoutine;
    protected int lateSportSlot;
    protected int lateWorkSlot;
    protected boolean calculation;
    protected String FileName;
    protected ArrayList<ArrayList<timeSlot.currentTask>> agenda;
    protected ArrayList<ArrayList<timeSlot>> fullAgenda;
    protected int pastAgendaSize;
    protected List<List<timeSlot>> pastAgenda;
    protected List<List<Boolean>> canceled_slots;
    protected int k;
    protected String current;
    protected String cancel_current;
    protected String event_current;
    protected List<String> agenda_back;
    protected List<String> past_agenda_back;
    protected List<String> cancel_back;
    protected List<String> event_back;
    protected Calendar settingDay;
    protected Calendar lastConnection;
    public List<newEvent> savedEvent;
    public ArrayList<timeSlot> freeWeekDay;

    public Profile(){
        agenda_back = new ArrayList<>();
        cancel_back = new ArrayList<>();
        event_back = new ArrayList<>();
        this.licenceAccepted = false;
        this.nbWorkHours = "168";
        this.optWorkTime = "6";
        this.freeDay = new boolean[] {false, false, false, false, false, false, false};
        this.wakeUp = "8.0";
        this.sportRoutine = 1;
        this.lateSportSlot = 0;
        this.lateWorkSlot = 0;
        calculation = false;
        this.FileName = "userProfile.txt";
        initAgenda();
        initFullAgenda();
        settingDay = Calendar.getInstance();
        settingDay.setTimeInMillis(System.currentTimeMillis());
        lastConnection = Calendar.getInstance();
        lastConnection.setTimeInMillis(System.currentTimeMillis());
        k = 0;
        current = new String();
        cancel_current = new String();
        event_current = new String();

        this.savedEvent = new ArrayList<>();

        this.freeWeekDay = new ArrayList<>();

        timeSlot.currentTask task;

        for (int i = 0; i < 24; i++) {

            task = timeSlot.currentTask.FREE;
            timeSlot slot = new timeSlot();
            slot.time = i;
            slot.task_1 = task;
            slot.task_2 = task;
            slot.task_3 = task;
            slot.task_4 = task;

            this.freeWeekDay.add(slot);

        }
        this.pastAgendaSize = 0;
    }

    private void initFullAgenda() {
        fullAgenda = new ArrayList<>();
        pastAgenda = new ArrayList<>();

        timeSlot.currentTask task;

        for(int j = 0; j < 7; j++ ) {
            ArrayList<timeSlot> mySlots = new ArrayList<>();
            for (int i = 0; i < 24; i++) {

                task = timeSlot.currentTask.FREE;
                timeSlot slot = new timeSlot();
                slot.time = i;
                slot.task_1 = task;
                slot.task_2 = task;
                slot.task_3 = task;
                slot.task_4 = task;

                mySlots.add(slot);

            }
            fullAgenda.add(mySlots);
        }

    }

    private void initAgenda() {
        agenda = new ArrayList<>();
        canceled_slots = new ArrayList<>();

        timeSlot.currentTask task;
        for(int j = 0; j < 7; j++ ) {
            ArrayList<timeSlot.currentTask> tasks = new ArrayList<>();
            List<Boolean> cancel = new ArrayList<>();

            for (int i = 0; i < 96; i++) {
                task = timeSlot.currentTask.FREE;
                tasks.add(task);
                cancel.add(Boolean.FALSE);
            }
            agenda.add(tasks);
            canceled_slots.add(cancel);
        }

    }

    public void Save(BufferedWriter bufferedWriter){
        try {
            bufferedWriter.write(String.valueOf(this.licenceAccepted));
            bufferedWriter.write("/");
            bufferedWriter.write(this.nbWorkHours);
            bufferedWriter.write("/");
            bufferedWriter.write(this.optWorkTime);
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
            bufferedWriter.write(String.valueOf(this.lateSportSlot));
            bufferedWriter.write("/");
            bufferedWriter.write(String.valueOf(this.lateWorkSlot));
            bufferedWriter.write("/");
            bufferedWriter.write(String.valueOf(settingDay.getTimeInMillis()));
            bufferedWriter.write("/");
            bufferedWriter.write(String.valueOf(lastConnection.getTimeInMillis()));
            bufferedWriter.write("/");
            bufferedWriter.write(String.valueOf(pastAgenda.size()));
            bufferedWriter.write("/");
            for (int i = 0; i < pastAgenda.size() ; i++){
                for (int j = 0; j < pastAgenda.get(i).size(); j++){
                    if (pastAgenda.get(i).get(j).task_1 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(pastAgenda.get(i).get(j).new_task_1);
                    }
                    else {
                        bufferedWriter.write(pastAgenda.get(i).get(j).task_1.toString());
                    }
                    bufferedWriter.write(",");
                    if (pastAgenda.get(i).get(j).task_2 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(pastAgenda.get(i).get(j).new_task_2);
                    }
                    else {
                        bufferedWriter.write(pastAgenda.get(i).get(j).task_2.toString());
                    }
                    bufferedWriter.write(",");
                    if (pastAgenda.get(i).get(j).task_3 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(pastAgenda.get(i).get(j).new_task_3);
                    }
                    else {
                        bufferedWriter.write(pastAgenda.get(i).get(j).task_3.toString());
                    }
                    bufferedWriter.write(",");
                    if (pastAgenda.get(i).get(j).task_4 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(pastAgenda.get(i).get(j).new_task_4);
                    }
                    else {
                        bufferedWriter.write(pastAgenda.get(i).get(j).task_4.toString());
                    }
                    bufferedWriter.write(",");
                }
            }
            bufferedWriter.write("/");
            for (int i = 0; i < fullAgenda.size() ; i++){
                for (int j = 0; j < fullAgenda.get(i).size(); j++){
                    if (fullAgenda.get(i).get(j).task_1 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(fullAgenda.get(i).get(j).new_task_1);
                    }
                    else {
                        bufferedWriter.write(fullAgenda.get(i).get(j).task_1.toString());
                    }
                    bufferedWriter.write(",");
                    if (fullAgenda.get(i).get(j).task_2 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(fullAgenda.get(i).get(j).new_task_2);
                    }
                    else {
                        bufferedWriter.write(fullAgenda.get(i).get(j).task_2.toString());
                    }
                    bufferedWriter.write(",");
                    if (fullAgenda.get(i).get(j).task_3 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(fullAgenda.get(i).get(j).new_task_3);
                    }
                    else {
                        bufferedWriter.write(fullAgenda.get(i).get(j).task_3.toString());
                    }
                    bufferedWriter.write(",");
                    if (fullAgenda.get(i).get(j).task_4 == timeSlot.currentTask.NEWEVENT){
                        bufferedWriter.write(fullAgenda.get(i).get(j).new_task_4);
                    }
                    else {
                        bufferedWriter.write(fullAgenda.get(i).get(j).task_4.toString());
                    }
                    bufferedWriter.write(",");
                }
            }
            bufferedWriter.write("/");
            bufferedWriter.write(canceled_slots.toString());
            bufferedWriter.write("/");
            if (savedEvent != null) {
                for (newEvent e : savedEvent) {
                    bufferedWriter.write(e.name);
                    bufferedWriter.write(",");
                    bufferedWriter.write(e.color.toString());
                    bufferedWriter.write(",");
                    if (e.work) {
                        bufferedWriter.write("1");
                    }
                    else {
                        bufferedWriter.write("0");
                    }
                    bufferedWriter.write(",");
                    if (e.sport) {
                        bufferedWriter.write("1");
                    }
                    else {
                        bufferedWriter.write("0");
                    }
                    bufferedWriter.write(",");
                }
                bufferedWriter.write("/");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.w("CANCELED SAVE", canceled_slots.toString());
    }

    public void decode(String lineData) {
        String lA="";
        String nW="";
        String oW="";
        Character[] fD= new Character[] {'0','0','0','0','0','0','0'};
        String wU="";
        past_agenda_back = new ArrayList<>();
        agenda_back = new ArrayList<>();
        cancel_back = new ArrayList<>();
        event_back = new ArrayList<>();
        String sR="";
        String lSS="";
        String lWS="";
        String timeMillis = "";
        String lastConnexionTimeMillis = "";
        String pastAS = "";

        int j = 0;
        int n = 0;

        for (int i=0;i<lineData.length();i++) {
            if (lineData.charAt(i) !='/'){
                switch (j){
                    case 0 : lA += lineData.charAt(i);
                             break;
                    case 1 : nW += lineData.charAt(i);
                             break;
                    case 2 : oW += lineData.charAt(i);
                            break;
                    case 3 : fD[n] = lineData.charAt(i);
                             n++;
                             break;
                    case 4 : wU += lineData.charAt(i);
                             break;
                    case 5 : sR += lineData.charAt(i);
                             break;
                    case 6 : lSS += lineData.charAt(i);
                            break;
                    case 7 : lWS += lineData.charAt(i);
                            break;
                    case 8 : timeMillis += lineData.charAt(i);
                            break;
                    case 9 : lastConnexionTimeMillis += lineData.charAt(i);
                            break;
                    case 10 : pastAS += lineData.charAt(i);
                            break;
                    case 11 : getPastAgenda(lineData.charAt(i));
                            break;
                    case 12 : getAgenda(lineData.charAt(i));
                            break;
                    case 13 : getCanceled(lineData.charAt(i));
                            break;
                    case 14 : getSavedEvent(lineData.charAt(i));
                            break;

                }
            }
            else {
                j++;
            }
        }

        this.pastAgendaSize = Integer.parseInt(pastAS);
        writeInPastAgenda();
        writeInAgenda();
        writeCanceled();
        writeSavedEvent();

        this.licenceAccepted = Boolean.parseBoolean(lA);
        this.nbWorkHours = nW;
        this.optWorkTime = oW;
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
        this.lateSportSlot = Integer.parseInt(lSS);
        this.lateWorkSlot = Integer.parseInt(lWS);
        this.settingDay = Calendar.getInstance();               //pas nécessaire?
        this.settingDay.setTimeInMillis(Long.parseLong(timeMillis));
        this.lastConnection.setTimeInMillis(Long.parseLong(lastConnexionTimeMillis));

        Log.w("CANCELED READ", canceled_slots.toString());
    }

    public void convertInPastDay(){
        int setting_day = this.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        int daySinceLastConnection = actual_day - this.lastConnection.get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - this.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        offset = (offset-daySinceLastConnection) % 7;            //- daySinceLastConnection pour avoir le jour d'avant

        for (int i = 0; i < daySinceLastConnection; i++) {
            this.pastAgenda.add(this.fullAgenda.get((i + offset)%7));

            int nbFreeDay = 0;
            for (int j = 0; j < freeDay.length; j++){
                if (!freeDay[j]){
                    nbFreeDay++;
                }
            }
            int nbWorkDay=0;
            for (int j=0 ; j < freeDay.length ; j++){
                if (!freeDay[j]) {
                    nbWorkDay++;
                }
            }
            int[] freedaylist = new int[nbWorkDay];
            int n = 0;
            for (int j=0 ; j < this.freeDay.length ; j++){
                if (!freeDay[j]) {
                    freedaylist[n] = (j + (7-conversionDayIndice()))%7;
                    n++;
                }
            }
            boolean freeday = true;
            for (int k = 0; k < freedaylist.length; k++) {
                if (freedaylist[k] == i){
                    freeday = false;
                }
            }
            if (!freeday) {
                this.lateWorkSlot += Integer.parseInt(nbWorkHours) / nbFreeDay;
            }

            if(sportRoutine == 2){
                this.lateSportSlot += 4;
            }
            else{
                this.lateSportSlot += 2;
            }

            DaySlotsCalculation plan = new DaySlotsCalculation(this);

            IA Agent = new IA(plan.slots_generated.get((i + offset)%7), this.fullAgenda.get((i + offset)%7), (i + offset)%7,
                    this.savedEvent, freeday, Integer.parseInt(this.optWorkTime),
                    this.lateWorkSlot, this.sportRoutine, this.lateSportSlot);
            Agent.planDay();
            this.agenda.set((i + offset)%7, Agent.dailyAgenda);
            this.lateSportSlot = Agent.sportSlot;
            this.lateWorkSlot = Agent.workSlot;

            int position;
            for(int j = 0; j < 96; j +=4 ) {
                position = j/4;
                this.fullAgenda.get((i+offset)%7).get(position).task_1 =  this.agenda.get((i+offset)%7).get(j);
                this.fullAgenda.get((i+offset)%7).get(position).task_2 =  this.agenda.get((i+offset)%7).get(j+1);
                this.fullAgenda.get((i+offset)%7).get(position).task_3 =  this.agenda.get((i+offset)%7).get(j+2);
                this.fullAgenda.get((i+offset)%7).get(position).task_4 =  this.agenda.get((i+offset)%7).get(j+3);
            }

        }
        this.lastConnection = Calendar.getInstance();
        this.lastConnection.setTimeInMillis(System.currentTimeMillis());
    }

    private int conversionDayIndice() {
        int offset = 0;
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.MONDAY:
                offset = 0;
                break;
            case Calendar.TUESDAY:
                offset = 1;
                break;
            case Calendar.WEDNESDAY:
                offset = 2;
                break;
            case Calendar.THURSDAY:
                offset = 3;
                break;
            case Calendar.FRIDAY:
                offset = 4;
                break;
            case Calendar.SATURDAY:
                offset = 5;
                break;
            case Calendar.SUNDAY:
                offset = 6;
                break;

        }

        return offset;
    }

    private void getSavedEvent(char charAt) {
        if(charAt == ',') {
            event_back.add(event_current);
            event_current = new String();
        }
        else
            event_current = event_current + charAt;

    }

    private void writeSavedEvent() {
        savedEvent.clear();
        newEvent e = new newEvent();
        for (int i = 0; i < event_back.size(); i++) {
            if (i % 4 == 0) {
                e.name = event_back.get(i);
            } else if (i % 4 == 1){
                e.color = Integer.parseInt(event_back.get(i));
            } else if (i % 4 == 2){
                if (event_back.get(i).equals("1")) {
                    e.work = true;
                }
                else {
                    e.work = false;
                }
            } else if (i % 4 == 3){
                if (event_back.get(i).equals("1")) {
                    e.sport = true;
                }
                else {
                    e.sport = false;
                }
                savedEvent.add(e);
                e = new newEvent();
            }
        }
    }

    private void getCanceled(char charAt) {
        if(charAt == '[' || charAt == ' ' || charAt == ']')
            return;
        else if(charAt == ',') {
            cancel_back.add(cancel_current);
            cancel_current = new String();
            k++;
        }
        else
            cancel_current = cancel_current + charAt;
    }


    private void writeCanceled() {
        for(int i = 0; i < cancel_back.size(); i++) {
            int j = i/96;
            int k = i%96;

            if ("true".equals(cancel_back.get(i))) {
                canceled_slots.get(j).set(k, Boolean.TRUE);

            } else if ("false".equals(cancel_back.get(i))) {
                canceled_slots.get(j).set(k, Boolean.FALSE);
            }
        }
    }

    private void writeInPastAgenda() {

        pastAgenda = new ArrayList<>();

        timeSlot.currentTask task;

        for(int j = 0; j < this.pastAgendaSize; j++ ) {
            List<timeSlot> mySlots = new ArrayList<>();
            for (int i = 0; i < 24; i++) {

                task = timeSlot.currentTask.FREE;
                timeSlot slot = new timeSlot();
                slot.time = i;
                slot.task_1 = task;
                slot.task_2 = task;
                slot.task_3 = task;
                slot.task_4 = task;

                mySlots.add(slot);

            }
            this.pastAgenda.add(mySlots);
        }

        timeSlot.currentTask instantagenda;

        for(int i = 0; i < past_agenda_back.size(); i++) {
            int j = i/96;
            int k = i%96;

            if ("SPORT".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.SPORT;

            } else if ("WORK".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.WORK;

            } else if ("EAT".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.EAT;

            } else if ("FREE".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.FREE;

            } else if ("WORK_FIX".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.WORK_FIX;

            } else if ("MORNING_ROUTINE".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.MORNING_ROUTINE;

            } else if ("SLEEP".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.SLEEP;

            } else if ("PAUSE".equals(past_agenda_back.get(i))) {
                instantagenda = timeSlot.currentTask.PAUSE;
            }
            else{
                instantagenda =  timeSlot.currentTask.NEWEVENT;
                if(k%4 == 0) {
                    pastAgenda.get(j).get((int) k / 4).new_task_1 = past_agenda_back.get(i);
                }
                else if(k%4 == 1) {
                    pastAgenda.get(j).get((int) k / 4).new_task_2 = past_agenda_back.get(i);
                }
                else if(k%4 == 2) {
                    pastAgenda.get(j).get((int) k / 4).new_task_3 = past_agenda_back.get(i);
                }
                else if(k%4 == 3) {
                    pastAgenda.get(j).get((int) k / 4).new_task_4 = past_agenda_back.get(i);
                }
            }
            if(k%4 == 0) {
                pastAgenda.get(j).get((int) k / 4).task_1 = instantagenda;
            }
            else if(k%4 == 1) {
                pastAgenda.get(j).get((int) k / 4).task_2 = instantagenda;
            }
            else if(k%4 == 2) {
                pastAgenda.get(j).get((int) k / 4).task_3 = instantagenda;
            }
            else if(k%4 == 3) {
                pastAgenda.get(j).get((int) k / 4).task_4 = instantagenda;
            }
        }

        Log.w("AGENDA_BACK", past_agenda_back.toString());
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
            else{
                agenda.get(j).set(k, timeSlot.currentTask.NEWEVENT);
                if(k%4 == 0) {
                    fullAgenda.get(j).get((int) k / 4).new_task_1 = agenda_back.get(i);
                }
                else if(k%4 == 1) {
                    fullAgenda.get(j).get((int) k / 4).new_task_2 = agenda_back.get(i);
                }
                else if(k%4 == 2) {
                    fullAgenda.get(j).get((int) k / 4).new_task_3 = agenda_back.get(i);
                }
                else if(k%4 == 3) {
                    fullAgenda.get(j).get((int) k / 4).new_task_4 = agenda_back.get(i);
                }
            }
            if(k%4 == 0) {
                fullAgenda.get(j).get((int) k / 4).task_1 = agenda.get(j).get(k);
            }
            else if(k%4 == 1) {
                fullAgenda.get(j).get((int) k / 4).task_2 = agenda.get(j).get(k);
            }
            else if(k%4 == 2) {
                fullAgenda.get(j).get((int) k / 4).task_3 = agenda.get(j).get(k);
            }
            else if(k%4 == 3) {
                fullAgenda.get(j).get((int) k / 4).task_4 = agenda.get(j).get(k);
            }
        }

        Log.w("AGENDA_BACK", agenda_back.toString());
        Log.w("AGENDA", agenda.toString());
    }

    private void getPastAgenda(char charAt) {
        if(charAt == '[' || charAt == ' ' || charAt == ']')
            return;
        else if(charAt == ',') {
            past_agenda_back.add(current);
            current = new String();
            k++;
        }
        else
            current = current + charAt;

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

    public boolean stringWorkTimeToSlot(String time){
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
            int intHourt=0;
            hourt = Float.parseFloat(hour);
            mint = Float.parseFloat(min);
            ok = true;
            if (hourt > 80 || hourt < 0){
                ok = false;
            }
            if (mint >= 60 || mint < 0){
                ok = false;
            }
            else {
                hourt = hourt + roundFifty(mint/60);
                hourt = hourt*4;
                intHourt = (int)hourt;
            }
            if(ok){
                this.nbWorkHours = String.valueOf(intHourt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok=false;
        }
        return ok;
    }

    public boolean stringOptWorkTimeToSlot(String time){
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
            int intHourt=0;
            hourt = Float.parseFloat(hour);
            mint = Float.parseFloat(min);
            ok = true;
            if (hourt > 4 || hourt < 0){
                ok = false;
            }
            if (mint >= 60 || mint < 0){
                ok = false;
            }
            else {
                hourt = hourt + roundFifty(mint/60);
                hourt = hourt*4;
                intHourt = (int)hourt;
            }
            if(ok){
                this.optWorkTime = String.valueOf(intHourt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok=false;
        }
        return ok;
    }

    public String slotStringToTimeString(String slotTime){
        String slot = "";
        String hour = "";
        String min = "";

        for (int i = 0; i < slotTime.length(); i++){
            slot += slotTime.charAt(i);
        }

        int nbSlot = Integer.parseInt(slot);
        min = String.valueOf((nbSlot % 4)*15);
        hour = String.valueOf((nbSlot - ((nbSlot % 4)))/4);
        hour += ':';
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
