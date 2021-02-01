package com.ludiostrix.organise_mois;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

Classe d'autocomplétion de l'agenda.

 */

public class DayPlan {
    protected Map<String,Integer> Task = new HashMap<>();
    protected List<List<List<Integer>>> Day;
    protected ArrayList<TimeSlot.CurrentTask> dailyAgenda;
    protected ArrayList<TimeSlot.CurrentTask> currentAgenda;
    private List<String> newEventAgenda;
    private int currentDay;
    private Calendar settingDay;
    //public List<List<TimeSlot.CurrentTask>> calculatedAgenda;
    private List<newEvent> savedEvent;
    private int workSizeOpti;
    float workSlot;
    int sportCategory;
    int sportSlot;
    int sportCatchUp;
    int workSlotCatchUp;
    public List<Integer> rank;
    public List<Boolean> canceledAgenda;
    private Calendar lastConnection;

    private boolean freeDay;

    private boolean init;
    private boolean firstinit;
    private boolean convertInPastDay;

    private List<Integer> lightSport = Arrays.asList(8, 8, 2);
    private List<Integer> intermediateSport = Arrays.asList(8, 8, 2);
    private List<Integer> intenseSport = Arrays.asList(8, 8, 6, 6, 4);


    public DayPlan(List<List<List<Integer>>> weight, List<Boolean> canceledSlots, List<Integer>weekRank, Calendar lastConnection, Calendar settingDay, ArrayList<TimeSlot.CurrentTask> day, ArrayList<TimeSlot.CurrentTask> currentAgenda, List<String> newEventDay, int dayNb, List<newEvent> savedevent, boolean freeday, int workSize, float wSlot, int workSlotCatchUp, int sCategory, int sSlot, int sportCatchUp, boolean firstinit, boolean convertInPastDay, boolean init){
        this.Task.put("Sport",0);
        this.Task.put("Work",1);           //Task.get("Sport")

        this.lastConnection = lastConnection;
        this.settingDay = settingDay;

        this.init = init;
        this.firstinit = firstinit;
        this.convertInPastDay = convertInPastDay;

        this.dailyAgenda = day;
        this.currentAgenda = currentAgenda;
        this.newEventAgenda = newEventDay;
        this.canceledAgenda = canceledSlots;

        //this.userProfile = userprofile;
        this.savedEvent = savedevent;

        //initWeight();

        this.Day = weight;

        this.currentDay = dayNb;
        this.sportSlot = sSlot + sportCatchUp;
        this.rank = weekRank;
        this.sportCategory = sCategory;
        this.freeDay = freeday;
        this.workSizeOpti = workSize;
        this.workSlot = wSlot;
        this.workSlotCatchUp = workSlotCatchUp;
        this.sportCatchUp = sportCatchUp;

        //searchFreeSlot(agenda, freeDayList);

    }

    // fonction à appeler pour remplir la journée
    public void planDay(){
        int slot = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 4 + Calendar.getInstance().get(Calendar.MINUTE) / 15 + 1;  // timeslot suivant l'actuel

        if(this.currentDay == convertedIndice()) {   //aujourdhui
            if (slot == 96){
                for (int i = 0; i < dailyAgenda.size(); i++){
                    if (firstinit){
                        dailyAgenda.set(i, TimeSlot.CurrentTask.PAUSE);
                    }
                    else if(currentAgenda.get(i) == TimeSlot.CurrentTask.FREE){
                        dailyAgenda.set(i, TimeSlot.CurrentTask.PAUSE);
                    }
                    else
                        dailyAgenda.set(i, currentAgenda.get(i));
                }

                if (!init) {
                    updateWorkSportToDo(slot);
                }
            }
            else {
                /*while (dailyAgenda.get(slot) == TimeSlot.CurrentTask.WORK && slot > 0) {
                    slot--;
                }*/
                while (dailyAgenda.get(slot) == TimeSlot.CurrentTask.WORK || dailyAgenda.get(slot) == TimeSlot.CurrentTask.SPORT ||
                        dailyAgenda.get(slot) == TimeSlot.CurrentTask.WORK_CATCH_UP ||
                        dailyAgenda.get(slot) == TimeSlot.CurrentTask.SPORT_CATCH_UP && slot < dailyAgenda.size() - 1) {
                    slot++;
                }
                if (slot < dailyAgenda.size() - 1 && (dailyAgenda.get(slot) == TimeSlot.CurrentTask.WORK || dailyAgenda.get(slot) == TimeSlot.CurrentTask.WORK_CATCH_UP)){
                    slot++;
                }

                if (!init) {
                    updateWorkSportToDo(slot);
                }
                for (int i = 0; i < slot; i++) {
                    if (firstinit){
                        dailyAgenda.set(i, TimeSlot.CurrentTask.PAUSE);
                    }
                    else if (currentAgenda.get(i) == TimeSlot.CurrentTask.FREE) {
                        dailyAgenda.set(i, TimeSlot.CurrentTask.PAUSE);
                    } else
                        dailyAgenda.set(i, currentAgenda.get(i));
                }
            }
        }
        else if (!init){
            updateWorkSportToDo(0);
        }

        if (slot < 96 || this.currentDay != 0) {
            setSport();
            if (!freeDay) {
                setWork();
            } else {
                searchFreeWorkSlot(); // enleve les heures de travail de la journée
            }
        }
    }

    // Met à jour la quantité de travail et de sport à faire avant de les replacer.
    private void updateWorkSportToDo(int slot){
        if (!convertInPastDay && !init) {
            this.sportSlot -= sportCatchUp;
            for (int i = slot; i < currentAgenda.size(); i++) {
                if (currentAgenda.get(i) == TimeSlot.CurrentTask.SPORT && !canceledAgenda.get(i)) {
                    sportSlot++;
                } else if ((currentAgenda.get(i) == TimeSlot.CurrentTask.WORK || currentAgenda.get(i) == TimeSlot.CurrentTask.WORK_FIX) && !canceledAgenda.get(i)) {
                    workSlot++;
                } else if (currentAgenda.get(i) == TimeSlot.CurrentTask.SPORT_CATCH_UP && !canceledAgenda.get(i)) {
                    sportCatchUp++;
                } else if ((currentAgenda.get(i) == TimeSlot.CurrentTask.WORK_CATCH_UP) && !canceledAgenda.get(i)) {
                    workSlotCatchUp++;
                } else if (currentAgenda.get(i) == TimeSlot.CurrentTask.NEWEVENT && !canceledAgenda.get(i)) {
                    for (newEvent event : savedEvent) {
                        if (newEventAgenda.get(i).equals(event.name)) {
                            if (event.sport) {
                                sportSlot++;
                            } else if (event.work) {
                                workSlot++;
                            }
                        }
                    }
                }
            }
            this.sportSlot += sportCatchUp;
        }
    }


/*

Section de placement des heures de travail et de sport.

 */

    private void setWork(){
        List<List<Integer>> freeGroup = searchFreeWorkSlot();

        List<List<Integer>> workGroupSize = new ArrayList<>();
        List<Integer> subGroupSize;
        for (int i = 0; i < freeGroup.size(); i ++){
            subGroupSize = new ArrayList<>();

            int size = freeGroup.get(i).size();
            while (size > 2*workSizeOpti){
                subGroupSize.add(workSizeOpti);
                size -= workSizeOpti;
                size--;                         //pause
            }

            if (size <= 1.5*workSizeOpti){
                subGroupSize.add(size);
                workGroupSize.add(subGroupSize);
            }
            else {
                size--;
                if ((size)%2 == 0){
                    subGroupSize.add(size/2);
                    subGroupSize.add(size/2);
                    workGroupSize.add(subGroupSize);
                }
                else{
                    subGroupSize.add(size/2 + 1);
                    subGroupSize.add(size/2);
                    workGroupSize.add(subGroupSize);
                }
            }

            Collections.sort(workGroupSize.get(i), Collections.<Integer>reverseOrder());

        }

        //List<List<List<Integer>>> workGroupPosition = new ArrayList<>();
        List<List<Integer>> groupPosition = new ArrayList<>();
        List<Integer> position;
        List<Integer> pausePosition = new ArrayList<>();

        int offset;
        for (int i = 0; i < workGroupSize.size(); i++){
            //groupPosition = new ArrayList<>();
            offset = 0;
            for (int j = 0; j < workGroupSize.get(i).size(); j++){
                int k;
                position = new ArrayList<>();
                for (k = 0; k < workGroupSize.get(i).get(j); k++){
                    position.add(freeGroup.get(i).get(k + offset));
                }
                groupPosition.add(position);
                offset = offset + k + 1;
                if (j != workGroupSize.get(i).size() - 1) {
                    pausePosition.add(freeGroup.get(i).get(offset - 1));
                }
            }
            //workGroupPosition.add(groupPosition);
        }

        List<Double> score = new ArrayList<>();

        for (int i = 0; i < groupPosition.size(); i++){
            score.add(Double.valueOf(0));
            for (int j = 0; j < groupPosition.get(i).size(); j++) {
                score.set(i, score.get(i) + Double.valueOf(Day.get((conversionDayIndice() + currentDay)%7).get(groupPosition.get(i).get(j)).get(Task.get("Work"))));
            }
            score.set(i, score.get(i)/groupPosition.get(i).size());
        }

        while (workSlot > 0 && score.size() > 0){
            for (int i = 0; i < score.size(); i++){
                if (Collections.max(score) == score.get(i)){
                    for (int j = 0; j < groupPosition.get(i).size(); j++) {
                        dailyAgenda.set(groupPosition.get(i).get(j), TimeSlot.CurrentTask.WORK);
                        workSlot--;
                    }
                    score.remove(i);
                    groupPosition.remove(i);
                }
            }
        }

        while (workSlotCatchUp > 0 && score.size() > 0){
            for (int i = 0; i < score.size(); i++){
                if (Collections.max(score) == score.get(i)){
                    for (int j = 0; j < groupPosition.get(i).size(); j++) {
                        dailyAgenda.set(groupPosition.get(i).get(j), TimeSlot.CurrentTask.WORK_CATCH_UP);
                        workSlotCatchUp--;
                        if (workSlotCatchUp <= 0){
                            break;
                        }
                    }
                    score.remove(i);
                    groupPosition.remove(i);
                }
            }
        }

        for (int i = 0; i < pausePosition.size(); i++){
            dailyAgenda.set(pausePosition.get(i), TimeSlot.CurrentTask.PAUSE);
        }

    }

    private List<List<Integer>> searchFreeWorkSlot() {
        List<String> workEvent = new ArrayList<>();

        for (int i = 0; i < savedEvent.size(); i++) {
            if (savedEvent.get(i).work){
                workEvent.add(savedEvent.get(i).name);
            }
        }
        List<Integer> workPosition = new ArrayList<>();

        /*for (int j = 0; j < newEventAgenda.size(); j++) {
            if (workEvent.size() != 0) {
                for (String eventName : workEvent) {
                    if (newEventAgenda.get(j).equals(eventName)){
                        workPosition.add(j);
                    }
                }
            }
        }*/

        for (int j = 0; j < dailyAgenda.size(); j++){
            if (dailyAgenda.get(j) == TimeSlot.CurrentTask.WORK_FIX){
                workPosition.add(j);
            }
            if (dailyAgenda.get(j) == TimeSlot.CurrentTask.NEWEVENT){
                if (workEvent.size() != 0) {
                    for (String eventName : workEvent) {
                        if (newEventAgenda.get(j).equals(eventName)){
                            workPosition.add(j);
                        }
                    }
                }
            }
        }


        List<List<Integer>> freeDaySlot = new ArrayList<>();
        List<Integer> freeGroupSlot = new ArrayList<>();

        for (int i = 0; i < dailyAgenda.size(); i++) {
            if (dailyAgenda.get(i) == TimeSlot.CurrentTask.FREE && workPosition.indexOf(i - 1) == -1 && workPosition.indexOf(i + 1) == -1 ){
                freeGroupSlot.add(i);
            }
            else {
                if (freeGroupSlot.size() != 0) {
                    freeDaySlot.add(freeGroupSlot);
                    freeGroupSlot = new ArrayList<>();
                }
            }
            if(i == dailyAgenda.size() - 1 && freeGroupSlot.size() != 0){
                freeDaySlot.add(freeGroupSlot);
            }
        }

        workSlot = workSlot - (float)workPosition.size();

        return freeDaySlot;
    }

    /*private void initBonusScore() {
        bonusScore = new ArrayList<>();
        for (int i = 0; i <= workSizeOpti; i++){
            this.bonusScore.add(i);
        }

        for(int i = workSizeOpti; i > workSizeOpti/2; i = i - 2){

        }
    }*/


    private void setSport() {
        if (this.lastConnection.get(Calendar.WEEK_OF_YEAR) != Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)){
            List<Double> score = scorePerDay();
            this.rank = rankDay(score);
        }

        List<Integer> maxSport = new ArrayList<>();
        if (sportCategory == 0){
            maxSport = lightSport;
        }
        else if (sportCategory == 1){
            maxSport = intermediateSport;
        }
        else if (sportCategory == 2){
            maxSport = intenseSport;
        }

        boolean placed = false;
        if (maxSport.size() > rank.get(currentDay)) {

            List<Integer> sportPosition = sportLocation();
            sportSlot = sportSlot-sportPosition.size();
            if (sportSlot > 1) {
                //search all freeSlot during one hour after the lunchtime to not add sport there if possible
                List<Integer> afterLunchtime = new ArrayList<>();

                for (int i = 0; i < dailyAgenda.size(); i ++){
                    if (dailyAgenda.get(i) == TimeSlot.CurrentTask.EAT || dailyAgenda.get(i) == TimeSlot.CurrentTask.MORNING_ROUTINE){
                        for(int j = 1; j < 5; j ++) {
                            if (i+j < dailyAgenda.size()) {
                                if (dailyAgenda.get(i + j) == TimeSlot.CurrentTask.FREE) {
                                    afterLunchtime.add(i + j);
                                }
                            }
                            else
                                break;
                        }
                    }
                }

                int afterLunchtimepenalty = 3;

                if (sportPosition.size() != 0) {
                    if (maxSport.get(rank.get(currentDay)) <= sportSlot) {
                        placed = placeSportAround(maxSport.get(rank.get(currentDay)) - sportPosition.size(), sportPosition,
                                afterLunchtime, afterLunchtimepenalty);
                        /*if (placed) {
                            sportSlot -= maxSport.get(rank.get(currentDay));
                        }*/
                    } else {
                        placed = placeSportAround(sportSlot - sportPosition.size(), sportPosition,
                                afterLunchtime, afterLunchtimepenalty);
                        /*if (placed) {
                            sportSlot = 0;
                        }*/
                    }
                    if (!placed) {
                        if (maxSport.get(rank.get(currentDay)) <= sportSlot) {
                            placed = placeSport(maxSport.get(rank.get(currentDay)) - sportPosition.size(),
                                    afterLunchtime, afterLunchtimepenalty);
                            /*if (placed) {
                                sportSlot -= maxSport.get(rank.get(currentDay));
                            }*/
                        } else {
                            placed = placeSport(sportSlot - sportPosition.size(),
                                    afterLunchtime, afterLunchtimepenalty);
                            /*if (placed) {
                                sportSlot = 0;
                            }*/
                        }
                    }
                } else {
                    if (maxSport.get(rank.get(currentDay)) <= sportSlot) {
                        placed = placeSport(maxSport.get(rank.get(currentDay)),
                                afterLunchtime, afterLunchtimepenalty);
                        /*if (placed) {
                            sportSlot -= maxSport.get(rank.get(currentDay));
                        }*/
                    } else {
                        placed = placeSport(sportSlot, afterLunchtime, afterLunchtimepenalty);
                        /*if (placed) {
                            sportSlot = 0;
                        }*/
                    }
                }
            }
        }
    }

    // Place le sport autour d'un événement sportif
    private boolean placeSportAround(int nbSportSlot, List<Integer> sportPosition, List<Integer> afterLunchtime, int afterLunchtimepenalty){
        boolean slotFound = false;

        List<Integer> position;
        List<List<Integer>> scoresPosition = new ArrayList<>();

        for (int j = 0; j < sportPosition.size(); j++) {
            int start = sportPosition.get(j);
            int stop = sportPosition.get(j);
            int following = 0;
            int k;
            for (k = j + 1; k < sportPosition.size(); k++) {
                following++;
                if (start + following != sportPosition.get(k)) {
                    stop = sportPosition.get(k - 1);
                    break;
                }
                else if (k == sportPosition.size()-1){
                    stop = sportPosition.get(k);
                }
            }

            for (int i = nbSportSlot; i >= 0; i--){
                position = new ArrayList<>();
                if (i != 0) {
                    if (dailyAgenda.get(start - i) == TimeSlot.CurrentTask.FREE && start - i >= 0) {
                        position.add(start - i);
                        int decalage = 1;
                        for (int l = 1; l < nbSportSlot; l++) {
                            if (start - i + l < start) {
                                if (dailyAgenda.get(start - i + l) == TimeSlot.CurrentTask.FREE) {
                                    position.add(start - i + l);
                                    decalage = position.size();
                                    if (l == nbSportSlot-1){
                                        scoresPosition.add(position);
                                    }
                                }
                                else
                                    break;
                            }
                            else {
                                if (stop + l - decalage + 1 < dailyAgenda.size()) {
                                    if (dailyAgenda.get(stop + l - decalage + 1) == TimeSlot.CurrentTask.FREE) {
                                        position.add(stop + l - decalage + 1);
                                        if (l == nbSportSlot - 1) {
                                            scoresPosition.add(position);
                                        }
                                    } else
                                        break;
                                }
                                else break;
                            }
                        }
                    }
                }
                else {
                    position.add(stop + 1);
                    for (int l = 2; l <= nbSportSlot; l++) {
                        if (stop + l < dailyAgenda.size()) {
                            if (dailyAgenda.get(stop + l) == TimeSlot.CurrentTask.FREE) {
                                position.add(stop + l);
                                if (l == nbSportSlot - 1) {
                                    scoresPosition.add(position);
                                }
                            } else
                                break;
                        }
                        else
                            break;
                    }
                }
            }

            j = k - 1;
        }

        if(scoresPosition.size() != 0) {
            List<Double> score = new ArrayList<>();
            for (int i = 0; i < scoresPosition.size(); i++) {
                score.add(Double.valueOf(0));
                for (int j = 0; j < scoresPosition.get(i).size(); j++) {
                    score.set(i, score.get(i) + this.Day.get((conversionDayIndice() + currentDay)%7).get(scoresPosition.get(i).get(j)).get(Task.get("Sport")));
                    if (afterLunchtime.indexOf(scoresPosition.get(i).get(j)) != -1){
                        score.set(i, score.get(i) - afterLunchtimepenalty);
                    }
                }
                score.set(i, score.get(i) / scoresPosition.get(i).size());
            }

            //choisi le dernier max (ajoute de préférence après le newevent)
            double maxScore = 0;
            int maxScoreIndice = 0;
            for (int i = 0; i < score.size(); i++) {
                if (score.get(i) >= maxScore) {
                    maxScore = score.get(i);
                    maxScoreIndice = i;
                }
            }
            sportSlot -= sportCatchUp;
            if (maxScoreIndice != -1) {
                for (int i = 0; i < scoresPosition.get(maxScoreIndice).size(); i++) {
                    if (i < scoresPosition.get(maxScoreIndice).size() - sportCatchUp) {
                        dailyAgenda.set(scoresPosition.get(maxScoreIndice).get(i), TimeSlot.CurrentTask.SPORT);
                        sportSlot--;
                    }
                    else{
                        dailyAgenda.set(scoresPosition.get(maxScoreIndice).get(i), TimeSlot.CurrentTask.SPORT_CATCH_UP);
                        sportCatchUp--;
                    }
                }
                slotFound = true;
            }
        }

        return slotFound;
    }


    //Place le sport si il n'a pas été possible de le placer autour d'un événement sportif
    private boolean placeSport(int nbSportSlot, List<Integer> afterLunchtime, int afterLunchtimepenalty){
        boolean slotFound = false;

        List<Integer> position;
        List<List<Integer>> scoresPosition = new ArrayList<>();

        if (nbSportSlot != 0) {
            for (int i = 0; i < dailyAgenda.size() - nbSportSlot + 1; i++) {
                position = new ArrayList<>();
                if (dailyAgenda.get(i) == TimeSlot.CurrentTask.FREE) {
                    position.add(i);
                    for (int j = i + 1; j < i + nbSportSlot; j++) {
                        if (j < dailyAgenda.size()) {
                            if (dailyAgenda.get(j) == TimeSlot.CurrentTask.FREE) {
                                position.add(j);
                                if (j == i + nbSportSlot - 1) {
                                    scoresPosition.add(position);
                                }
                            } else {
                                i = j;
                                break;
                            }
                        } else
                            break;
                    }
                }
            }
        }

        if(scoresPosition != null) {
            List<Double> score = new ArrayList<>();
            for (int i = 0; i < scoresPosition.size(); i++) {
                score.add(Double.valueOf(0));
                for (int j = 0; j < scoresPosition.get(i).size(); j++) {
                    score.set(i, score.get(i) + this.Day.get((conversionDayIndice() + currentDay)%7).get(scoresPosition.get(i).get(j)).get(Task.get("Sport")));
                    if (afterLunchtime.indexOf(scoresPosition.get(i).get(j)) != -1){
                        score.set(i, score.get(i) - afterLunchtimepenalty);
                    }
                }
                score.set(i, score.get(i) / scoresPosition.get(i).size());
            }

            //choisi de préférence un max entre 5h et 20h (le dernier)
            double maxScore = 0;
            int maxScoreIndice = -1;
            for (int i = 0; i < score.size(); i++) {
                if (score.get(i) >= maxScore && (score.get(i) > maxScore ||
                        (scoresPosition.get(i).get(0) < 20*4 + 1 && scoresPosition.get(i).get(0) >= 5*4) ||
                        //(scoresPosition.get(i).get(0) >= 20*4 && scoresPosition.get(maxScoreIndice).get(0) < 5*4) ||
                        maxScoreIndice == -1)) {
                    maxScore = score.get(i);
                    maxScoreIndice = i;
                }
            }

            sportSlot -= sportCatchUp;
            if (maxScoreIndice != -1) {
                for (int i = 0; i < scoresPosition.get(maxScoreIndice).size(); i++) {
                    if (i < scoresPosition.get(maxScoreIndice).size() - sportCatchUp) {
                        dailyAgenda.set(scoresPosition.get(maxScoreIndice).get(i), TimeSlot.CurrentTask.SPORT);
                        sportSlot--;
                    }
                    else{
                        dailyAgenda.set(scoresPosition.get(maxScoreIndice).get(i), TimeSlot.CurrentTask.SPORT_CATCH_UP);
                        sportCatchUp--;
                    }
                }
                slotFound = true;
            }
        }

        return slotFound;
    }

    // Search if there is already some sport during the day
    // return all the timeslot where there is Sport
    private List<Integer> sportLocation(){
        List<Integer> sportSlot = new ArrayList<>();
        List<String> sportEvent = new ArrayList<>();

        for (int i = 0; i < savedEvent.size(); i++) {
            if (savedEvent.get(i).sport){
                sportEvent.add(savedEvent.get(i).name);
            }
        }

        if (sportEvent.size() != 0) {
            for (int j = 0; j < newEventAgenda.size(); j++) {
                for (String eventName : sportEvent) {
                    if (newEventAgenda.get(j).equals(eventName)){
                        sportSlot.add(j);
                    }
                }
            }
        }

        return sportSlot;
    }


/*

Section des fonctions de clasement des jours de sport.

 */

    private List<Integer> rankDay(List<Double> score){

        List<Integer> rankPerDay = new ArrayList<>();

        for (int i = 0; i < score.size(); i++){
            int rank = 1;
            for (int j = 0; j < score.size(); j++){
                if (score.get(i) < score.get(j) && i != j){
                    rank++;
                }
            }
            rankPerDay.add(rank);
        }

        return rankPerDay;
    }

    private List<Double> scorePerDay(){

        List<Double> meanPerDay = new ArrayList<>();

        for (int i = 0; i < this.Day.size(); i++){
            meanPerDay.add(Double.valueOf(0));
            for (int j = 0; j < this.Day.get(i).size(); j++){
                meanPerDay.set(i, meanPerDay.get(i) + this.Day.get((conversionDayIndice() + i)%7).get(j).get(Task.get("Sport")));
            }
            meanPerDay.set(i, meanPerDay.get(i)/this.Day.get(i).size());
        }

        return meanPerDay;
    }


/*

Section des fonction de conversion du jour.

 */

    private int convertedIndice() {
        int setting_day = settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        return offset%7;
    }

    public static int conversionDayIndice() {
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

    private void initWeight(){
        List<List<Integer>> Hour;
        List<Integer> dayWeight;
        List<Integer> nightWeight;
        Day = new ArrayList<>();
        for (int j = 0 ; j < 7; j++) {
            Hour = new ArrayList<>();
            for (int i = 0; i < 4 * 24; i++) {
                if (i < 4 * 7 - 1 || i > 4 * 22 - 1) {
                    nightWeight = new ArrayList<>();
                    nightWeight.add(4);
                    nightWeight.add(4);
                    Hour.add(nightWeight);
                } else {
                    dayWeight = new ArrayList<>();
                    dayWeight.add(6);
                    dayWeight.add(6);
                    Hour.add(dayWeight);
                }
            }
            Day.add(Hour);
        }


        // int test = this.Hour.get(12).get(Task.get("Sport"));
    }


/*

Début du deuxième algorithme abandonné en cours route.

 */

    /*private void setSport(int sport){
        preSelectDay();

        verifySelection(sport);

        if (userProfile.sportRoutine == 2){
            if (selectedDay.size() < 4){
                for(int i = 0; i < )
            }
        }


    }

    private void verifySelection(int sport){
        selectedDay = preSelectedDay;
        List nbNewEventSportSlot = countNewEventSportHourPerDay();
        int nbNewEventSport = 0;
        for (int i = 0; i < nbNewEventSportSlot.size(); i++){
            nbNewEventSport += (int)nbNewEventSportSlot.get(i);
        }

        sport = sport - nbNewEventSport;

        boolean modif = true;
        int nbSlotSport;
        int nbSlotSupp;
        while (modif){
            nbSlotSport = sport/selectedDay.size();
            nbSlotSupp = sport%selectedDay.size();
            for (int i : selectedDay){

                nbSlotSport = nbSlotSport - (int)nbNewEventSportSlot.get(i);
            }

            for (int i : selectedDay){

                List<Integer> alreadySport = new ArrayList<>(isAlreadySport(i));
                int nbSportHour;
                boolean slotFound = false;

                if (nbSlotSupp != 0) {
                    for (nbSportHour = nbSlotSport + 1; nbSportHour >= nbSlotSport; nbSportHour--) {

                        if (alreadySport != null) {
                            for (int j = 0; j < alreadySport.size(); j++) {
                                int start = alreadySport.get(j);
                                int stop = alreadySport.get(j);
                                int k;
                                for (k = j + 1; k < alreadySport.size(); k++) {
                                    if (start + k != alreadySport.get(k)) {
                                        stop = alreadySport.get(k - 1);
                                        break;
                                    }
                                }

                                if (isFreeSlotAround(i, start, stop, nbSportHour - alreadySport.size())) {
                                    slotFound = true;
                                    break;
                                }
                                j = j + (k - 1);
                            }

                            if (!slotFound) {
                                if (isFreeSlot(i, nbSportHour)) {
                                    slotFound = true;
                                }
                            }
                        } else {
                            if (isFreeSlot(i, nbSportHour)) {
                                slotFound = true;
                            }
                        }
                    }
                }
                else {
                    nbSportHour = nbSlotSport;
                    if (alreadySport != null) {
                        for (int j = 0; j < alreadySport.size(); j++) {
                            int start = alreadySport.get(j);
                            int stop = alreadySport.get(j);
                            int k;
                            for (k = j + 1; k < alreadySport.size(); k++) {
                                if (start + k != alreadySport.get(k)) {
                                    stop = alreadySport.get(k - 1);
                                    break;
                                }
                            }

                            if (isFreeSlotAround(i, start, stop, nbSportHour - alreadySport.size())) {
                                slotFound = true;
                                break;
                            }
                            j = j + (k - 1);
                        }

                        if (!slotFound) {
                            if (isFreeSlot(i, nbSportHour)) {
                                slotFound = true;
                            }
                        }
                    } else {
                        if (isFreeSlot(i, nbSportHour)) {
                            slotFound = true;
                        }
                    }
                }

                if (!slotFound) {
                    selectedDay.remove(i);
                    modif = true;
                    break;
                } else if (nbSportHour == nbSlotSport + 1) {
                    nbSlotSupp -= 1;
                    modif = false;
                }
            }
        }
    }

    private List<Integer> countNewEventSportHourPerDay(){
        List<String> sportEvent = new ArrayList<>();
        List<Integer> nb = new ArrayList<>();

        for (int i = 0; i < userProfile.savedEvent.size(); i++) {
            if (userProfile.savedEvent.get(i).sport){
                sportEvent.add(userProfile.savedEvent.get(i).name);
            }
        }

        for(int i= 0; i <= userProfile.fullAgenda.size(); i++){
            nb.add(Integer.valueOf(0));
            for (int j = 0; j < userProfile.fullAgenda.get(i).size(); j++){
                for (String eventName : sportEvent) {
                    if (userProfile.fullAgenda.get(i).get(j).new_task_1 == eventName){
                        nb.set(i, nb.get(i) + 1);
                    }
                    if (userProfile.fullAgenda.get(i).get(j).new_task_2 == eventName){
                        nb.set(i, nb.get(i) + 1);
                    }
                    if (userProfile.fullAgenda.get(i).get(j).new_task_3 == eventName){
                        nb.set(i, nb.get(i) + 1);
                    }
                    if (userProfile.fullAgenda.get(i).get(j).new_task_4 == eventName){
                        nb.set(i, nb.get(i) + 1);
                    }
                }
            }
        }
        return nb;
    }

    private boolean isFreeSlotAround(int searchDay, int start, int stop, int nbSlot) {
        boolean isFree = true;
        int count = 0;
        for (int i = 0; i < nbSlot; i++){
            if (userProfile.agenda.get(searchDay).get(stop + i) == TimeSlot.CurrentTask.FREE){
                count++;
            }
            else
                break;
        }
        for (int i = 0; i < nbSlot; i++){
            if (userProfile.agenda.get(searchDay).get(start - i) == TimeSlot.CurrentTask.FREE){
                count++;
            }
            else
                break;
        }
        if (count < nbSlot){
            isFree = false;
        }

        return isFree;
    }

    private boolean isFreeSlot (int searchDay, int nbSlot){
        boolean isFree = false;
        int count = 0;
        int countMax = 0;
        for (int i = 0; i < userProfile.agenda.get(searchDay).size(); i++){
            if (count > nbSlot || (count > (nbSlot/2 + nbSlot%2) && countMax > nbSlot/2) ||
                                        (countMax > (nbSlot/2 + nbSlot%2) && count > nbSlot/2) ) {
                isFree = true;
                break;
            }
            else if (userProfile.agenda.get(searchDay).get(i) == TimeSlot.CurrentTask.FREE) {
                count++;
            } else {
                if (count > countMax) {
                    countMax = count;
                }
                count = 0;
            }
        }
        return isFree;
    }

    private void preSelectDay(){
        List<Integer> meanPerDay = new ArrayList<>();
        double meanMax = 0;
        double bestDaySelection = 0.05;

        for (int i = 0; i < userProfile.agenda.size(); i++){
            meanPerDay.add(Integer.valueOf(0));
            for (int j = 0; j < userProfile.agenda.get(i).size(); j++){
                meanPerDay.set(i, meanPerDay.get(i) + this.Day.get(j).get(i).get(Task.get("Sport")));
            }
            meanPerDay.set(i, meanPerDay.get(i)/userProfile.agenda.get(i).size());
            if (meanMax < meanPerDay.get(i)){
                meanMax = meanPerDay.get(i);
            }
        }

        for (int i =0; i < meanPerDay.size(); i++){
            if (meanPerDay.get(i) > meanMax - bestDaySelection){
                preSelectedDay.add(i);
            }
        }
    }

    // Search if there is already some sport during the day
    // return all the timeslot where there is Sport
    private List<Integer> isAlreadySport(int dayToSearch){
        List<Integer> sportSlot = new ArrayList<>();

        for (int i = 0; i < userProfile.agenda.get(dayToSearch).size(); i++){
            if (userProfile.agenda.get(dayToSearch).get(i) == TimeSlot.CurrentTask.NEWEVENT){
                //cherche le nom de l'evenement
                String eventName = "";
                if (i%4 == 0){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_1;
                } else if(i%4 == 1){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_2;
                } else if(i%4 == 2){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_3;
                } else if(i%4 == 3){
                    eventName = userProfile.fullAgenda.get(dayToSearch).get(i).new_task_4;
                }
                // vérifie si l'événement est du sport
                for (int j = 0; j < userProfile.savedEvent.size(); j++){
                    if (userProfile.savedEvent.get(j).name.equals(eventName)){
                        if (userProfile.savedEvent.get(j).sport){
                            sportSlot.add(i);
                        }
                        break;
                    }
                }
            }
        }

        return sportSlot;
    }

    private class freeLocation{
        int position;
        double score;
    }*/
}


