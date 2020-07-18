package com.ludiostrix.organise_mois;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*

Cette classe n'est pas utilisée dans la version actuelle du code

 */

public class StatPerDay implements Serializable {
    int work;
    float workDone;
    int sport;
    int sportDone;

    void Stat (){
        work = 0;
        workDone = Float.valueOf(0);
        sport = 0;
        sportDone = 0;
    }

    void updateStat(ArrayList<timeSlot.CurrentTask> agenda, ArrayList<String> newEventAgenda, List<newEvent> savedEvent){
        int workTimeSlot = 0;
        int sportTimeSlot = 0;

        for (int i = 0; i < agenda.size(); i++){
            if (agenda.get(i) == timeSlot.CurrentTask.WORK || agenda.get(i) == timeSlot.CurrentTask.WORK_FIX){
                workTimeSlot++;
            }
            else if (agenda.get(i) == timeSlot.CurrentTask.SPORT){
                sportTimeSlot++;
            }
            else if (agenda.get(i) == timeSlot.CurrentTask.NEWEVENT){
                for (newEvent event : savedEvent){
                    if (event.work){
                        workTimeSlot++;
                    }
                    else if (event.sport){
                        sportTimeSlot++;
                    }
                }
            }
        }
        workDone = workTimeSlot;
        sportDone = sportTimeSlot;
    }

}
