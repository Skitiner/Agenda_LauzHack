package com.ludiostrix.organise_mois;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.ludiostrix.organise_mois.AgendaActivity.conversionDayIndice;

/*

Activité du profil.

 */

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String FIXED_WORK = "FIXED_WORK";
    public static final String LUNCH_TIME = "LUNCH_TIME";

    private List<Integer> dayIds = Arrays.asList(R.id.monday, R.id.tuesday, R.id.wednesday, R.id.thursday,
            R.id.friday, R.id.saturday, R.id.sunday);

    private List<Integer> sportIds = Arrays.asList(R.id.sloth, R.id.rabbit, R.id.cheetah);

    Profile userProfile = new Profile();

    private int positionSport;
    private boolean[] NewFreeDay;

    private boolean settings = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        readFromFile();

        if (savedInstanceState != null) {
            if (savedInstanceState.getSerializable("nbworkHours") != null) {
                userProfile.nbWorkHours = (String) savedInstanceState.getSerializable("nbworkHours");
            }
            if (savedInstanceState.getSerializable("wakeUp") != null) {
                userProfile.wakeUp = (String) savedInstanceState.getSerializable("wakeUp");
            }
            if (savedInstanceState.getSerializable("freeDay") != null) {
                NewFreeDay = (boolean[]) savedInstanceState.getSerializable("freeDay");
            }
            if (savedInstanceState.getSerializable("sportRoutine") != null) {
                positionSport = (int) savedInstanceState.getSerializable("sportRoutine");
            }
            if (savedInstanceState.getSerializable("OptWorkTime") != null) {
                userProfile.optWorkTime = (String) savedInstanceState.getSerializable("OptWorkTime");
            }
            if (savedInstanceState.getSerializable("settings") != null) {
                settings = (boolean) savedInstanceState.getSerializable("settings");
            }
        }
        else {
            positionSport = userProfile.sportRoutine;
            NewFreeDay = userProfile.freeDay;
        }

        setProfileInfo();
        if (!userProfile.agendaInit && settings) {
            //setProfileInfo();
            resetWorkAndSportToDo();
            settings = false;

            userProfile.sportRoutine = positionSport;
            int newOffsetSettings = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
            for (int i = 0; i < newOffsetSettings ; i++) {
                ArrayList<TimeSlot> tempDay = userProfile.fullAgenda.get(0);
                userProfile.fullAgenda.remove(0);
                userProfile.fullAgenda.add(tempDay);
                ArrayList<TimeSlot> futurTempDay = userProfile.futurFullAgenda.get(0);
                userProfile.futurFullAgenda.remove(0);
                userProfile.futurFullAgenda.add(futurTempDay);
            }
            userProfile.settingDay = Calendar.getInstance();
            userProfile.settingDay.setTimeInMillis(System.currentTimeMillis());
        }
        saveToFile();
    }

    private void resetWorkAndSportToDo(){
        for (int i = 0; i < userProfile.agenda.size(); i++){
            int start = 0;
            if (i == convertedIndice()){
                start = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*4 + Calendar.getInstance().get(Calendar.MINUTE)*4/60 + 1;
            }
            for(int j = start; j < userProfile.agenda.get(i).size(); j++){
                if (!userProfile.canceled_slots.get(i).get(j)){
                    if (userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.NEWEVENT) {
                        for (newEvent event : userProfile.savedEvent) {
                            if (event.name.equals(userProfile.newEventAgenda.get(i).get(j))) {
                                if (event.sport) {
                                    userProfile.lateSportSlot++;
                                } else if (event.work) {
                                    userProfile.lateWorkSlot++;
                                }
                            }
                        }
                    } else if (userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.WORK ||
                            userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.WORK_FIX) {
                        userProfile.lateWorkSlot++;
                    } else if (userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.SPORT) {
                        userProfile.lateSportSlot++;
                    }
                    else if (userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.WORK_CATCH_UP) {
                        userProfile.workCatchUp++;
                    } else if (userProfile.agenda.get(i).get(j) == TimeSlot.CurrentTask.SPORT_CATCH_UP) {
                        userProfile.sportCatchUp++;
                    }
                }
            }
        }

        float sportRatio = 0;
        float workRatio = 0;
        int sportDone = 0;
        int workDone = 0;
        int sportTotal = 0;
        int workTotal = 0;

        int today = convertedIndice();
        for(int j = 0; j < userProfile.agenda.get(today).size(); j++){
            int sport = 0;
            int work = 0;
            if (userProfile.agenda.get(today).get(j) == TimeSlot.CurrentTask.NEWEVENT) {
                for (newEvent event : userProfile.savedEvent) {
                    if (event.name.equals(userProfile.newEventAgenda.get(today).get(j))) {
                        if (event.sport) {
                            sport = 1;
                        } else if (event.work) {
                            work = 1;
                        }
                    }
                }
            } else if (userProfile.agenda.get(today).get(j) == TimeSlot.CurrentTask.WORK ||
                    userProfile.agenda.get(today).get(j) == TimeSlot.CurrentTask.WORK_FIX) {
                work = 1;
            } else if (userProfile.agenda.get(today).get(j) == TimeSlot.CurrentTask.SPORT) {
                sport = 1;
            }

            sportTotal += sport;
            workTotal += work;
            if(j < Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*4 + Calendar.getInstance().get(Calendar.MINUTE)*4/60 + 1){
                sportDone += sport;
                workDone += work;
            }
        }

        if (sportTotal != 0){
            sportRatio = 1 - (float)sportDone/(float)sportTotal;
        }

        if (workTotal != 0) {
            workRatio = 1 - (float) workDone / (float) workTotal;
        }

        int nbWorkDay = 0;
        for (int j = 0; j < userProfile.freeDay.length; j++){
            if (!userProfile.freeDay[j]){
                nbWorkDay++;
            }
        }

        if (userProfile.settingDay.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            if (!userProfile.freeDay[conversionDayIndice()]) {
                userProfile.lateWorkSlot -= ((float)(nbWorkDay-1) / (float)nbWorkDay)*Float.parseFloat(userProfile.nbWorkHours);
                userProfile.lateWorkSlot -= workRatio * (Float.parseFloat(userProfile.nbWorkHours) / (float)nbWorkDay);
            }
            else {
                userProfile.lateWorkSlot -= Float.parseFloat(userProfile.nbWorkHours);
            }
            if (userProfile.sportRoutine == 2) {
                userProfile.lateSportSlot -= 6 * 4;
                userProfile.lateSportSlot -= sportRatio * 4;
            } else {
                userProfile.lateSportSlot -= 6 * 2;
                userProfile.lateSportSlot -= sportRatio * 2;
            }
        }
        else {
            //int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int settingHour = userProfile.settingDay.get(Calendar.HOUR_OF_DAY);
            if (settingHour < 20 && Float.parseFloat(userProfile.wakeUp) < settingHour) {
                if (!userProfile.freeDay[conversionDayIndice()]) {
                    this.userProfile.lateWorkSlot -= workRatio * ((Float.parseFloat(userProfile.nbWorkHours) / (float) nbWorkDay)
                            * (Float.valueOf(20) - (float) settingHour) /
                            (Float.valueOf(20) - (Float.parseFloat(userProfile.wakeUp) + 1)));
                    // coeff < 1 réprensentant le pourcentage d'heure de la journée
                }
                if (userProfile.sportRoutine == 2) {
                    userProfile.lateSportSlot -= 4;
                } else {
                    userProfile.lateSportSlot -= 2;
                }
            } else if (settingHour < 20) {
                if (!userProfile.freeDay[conversionDayIndice()]) {
                    this.userProfile.lateWorkSlot -= workRatio * Float.parseFloat(userProfile.nbWorkHours) / (float) nbWorkDay;
                }
                if (userProfile.sportRoutine == 2) {
                    userProfile.lateSportSlot -= 4;
                } else {
                    userProfile.lateSportSlot -= 2;
                }
            }


            if (!userProfile.freeDay[conversionDayIndice()]) {
                userProfile.lateWorkSlot -= ((float)(nbWorkDay-1) / (float)nbWorkDay)*Float.parseFloat(userProfile.nbWorkHours);
            }
            else {
                userProfile.lateWorkSlot -= Float.parseFloat(userProfile.nbWorkHours);
            }

            if (userProfile.sportRoutine == 2) {
                userProfile.lateSportSlot -= 6 * 4;
            } else {
                userProfile.lateSportSlot -= 6 * 2;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        TextView NBOptWorkEditText = findViewById(R.id.NBOptWorkEditText);
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);
        userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
        userProfile.stringOptWorkTimeToSlot(NBOptWorkEditText.getText().toString());
        userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
        state.putSerializable("nbWorkHours", userProfile.nbWorkHours);
        state.putSerializable("wakeUp", userProfile.wakeUp);
        state.putSerializable("freeDay", NewFreeDay);
        state.putSerializable("sportRoutine", positionSport);
        state.putSerializable("OptWorkTime", userProfile.optWorkTime);
        state.putSerializable("settings", settings);
    }

    public static final String PREFS_NAME = "MyPrefsFile";
    @Override
    public void onResume(){
        super.onResume();

        readFromFile();
    }

    /*protected void onPause(){
        super.onPause();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        // Necessary to clear first if we save preferences onPause.
        editor.clear();
        editor.putBoolean("freeDay0", NewFreeDay[0]);
        editor.putBoolean("freeDay1", NewFreeDay[1]);
        editor.putBoolean("freeDay2", NewFreeDay[2]);
        editor.putBoolean("freeDay3", NewFreeDay[3]);
        editor.putBoolean("freeDay4", NewFreeDay[4]);
        editor.putBoolean("freeDay5", NewFreeDay[5]);
        editor.putBoolean("freeDay6", NewFreeDay[6]);
        editor.putInt("positionSport", positionSport);
        editor.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        NewFreeDay[0] = settings.getBoolean("freeDay0",false);
        NewFreeDay[1] = settings.getBoolean("freeDay1",false);
        NewFreeDay[2] = settings.getBoolean("freeDay2",false);
        NewFreeDay[3] = settings.getBoolean("freeDay3",false);
        NewFreeDay[4] = settings.getBoolean("freeDay4",false);
        NewFreeDay[5] = settings.getBoolean("freeDay5",false);
        NewFreeDay[6] = settings.getBoolean("freeDay6",false);
        positionSport = settings.getInt("positionSport",-1);
        if (positionSport >= 0) {
            setProfileInfo();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        if (savedInstanceState != null) {
            if (savedInstanceState.getSerializable("nbworkHours") != null) {
                userProfile.nbWorkHours = (String) savedInstanceState.getSerializable("nbworkHours");
            }
            if (savedInstanceState.getSerializable("wakeUp") != null) {
                userProfile.wakeUp = (String) savedInstanceState.getSerializable("wakeUp");
            }
            if (savedInstanceState.getSerializable("freeDay") != null) {
                NewFreeDay = (boolean[]) savedInstanceState.getSerializable("freeDay");
            }
            if (savedInstanceState.getSerializable("sportRoutine") != null) {
                positionSport = (int) savedInstanceState.getSerializable("sportRoutine");
            }
            if (savedInstanceState.getSerializable("OptWorkTime") != null) {
                userProfile.optWorkTime = (String) savedInstanceState.getSerializable("OptWorkTime");
            }
        }
        setProfileInfo();
    }*/

    // Met à jour le rendu visuel du profil de facon à ce qu'il soit cohérant avec le profil
    private void setProfileInfo() {
        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        NBWorkEditText.setText(userProfile.slotStringToTimeString(userProfile.nbWorkHours));

        TextView NBOptWorkEditText = findViewById(R.id.NBOptWorkEditText);
        NBOptWorkEditText.setText(userProfile.slotStringToTimeString(userProfile.optWorkTime));

        setFreeDay();

        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);
        WakeUpEditText.setText(userProfile.floatStringToTimeString(userProfile.wakeUp));

        setSport();

    }

    private void setDayColor(Button dayButton, boolean red){
        if (!red) {
            dayButton.setTextColor(getResources().getColor(R.color.red, null));
        }
        else {
            dayButton.setTextColor(getResources().getColor(R.color.green, null));
        }
    }

    private void setFreeDay() {
        Button mondayButton = findViewById(R.id.monday);
        Button tuesdayButton = findViewById(R.id.tuesday);
        Button wednesdayButton = findViewById(R.id.wednesday);
        Button thursdayButton = findViewById(R.id.thursday);
        Button fridayButton = findViewById(R.id.friday);
        Button saturdayButton = findViewById(R.id.saturday);
        Button sundayButton = findViewById(R.id.sunday);

        setDayColor(mondayButton, NewFreeDay[0]);
        setDayColor(tuesdayButton, NewFreeDay[1]);
        setDayColor(wednesdayButton, NewFreeDay[2]);
        setDayColor(thursdayButton, NewFreeDay[3]);
        setDayColor(fridayButton, NewFreeDay[4]);
        setDayColor(saturdayButton, NewFreeDay[5]);
        setDayColor(sundayButton, NewFreeDay[6]);
    }

    private void setSport() {
        ImageButton slothSportButton = findViewById(R.id.sloth);
        ImageButton rabbitSportButton = findViewById(R.id.rabbit);
        ImageButton cheetahSportButton = findViewById(R.id.cheetah);
        TextView lightSportText = findViewById(R.id.light);
        TextView moderatedSportText = findViewById(R.id.moderated);
        TextView intenseSportText = findViewById(R.id.intense);

        if (positionSport==0){
            slothSportButton.setBackgroundColor(getResources().getColor(R.color.green, null));
            rabbitSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            cheetahSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            lightSportText.setTextColor(getResources().getColor(R.color.green, null));
            moderatedSportText.setTextColor(getResources().getColor(R.color.black, null));
            intenseSportText.setTextColor(getResources().getColor(R.color.black, null));
        }
        else if (positionSport==1) {
            slothSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            rabbitSportButton.setBackgroundColor(getResources().getColor(R.color.green, null));
            cheetahSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            lightSportText.setTextColor(getResources().getColor(R.color.black, null));
            moderatedSportText.setTextColor(getResources().getColor(R.color.green, null));
            intenseSportText.setTextColor(getResources().getColor(R.color.black, null));
        }
        else {
            slothSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            rabbitSportButton.setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
            cheetahSportButton.setBackgroundColor(getResources().getColor(R.color.green, null));
            lightSportText.setTextColor(getResources().getColor(R.color.black, null));
            moderatedSportText.setTextColor(getResources().getColor(R.color.black, null));
            intenseSportText.setTextColor(getResources().getColor(R.color.green, null));
        }

    }

    public void clickedDayButtonXmlCallback(View view) {

        Button day = (Button) view;
        final int dayID = day.getId(); // dayID is the id of the tapped Button
        final int position = dayIds.indexOf(dayID); // position is the position of the Button (in the ArrayList dayIds)

        Button dayButton = findViewById(dayID);

        NewFreeDay[position] = !NewFreeDay[position];

        int indice = ((7-conversionDayIndice()) + position)%7;

        for(int i = 0; i < userProfile.agenda.get(indice).size(); i++) {
            if(userProfile.agenda.get(indice).get(i) != TimeSlot.CurrentTask.NEWEVENT) {
                userProfile.agenda.get(indice).set(i, TimeSlot.CurrentTask.FREE);
            }
        }

        int pos;
        for(int i = 0; i < 96; i +=4 ) {
            pos = i/4;
            userProfile.fullAgenda.get(indice).get(pos).task_1 =  userProfile.agenda.get(indice).get(i);
            userProfile.fullAgenda.get(indice).get(pos).task_2 =  userProfile.agenda.get(indice).get(i+1);
            userProfile.fullAgenda.get(indice).get(pos).task_3 =  userProfile.agenda.get(indice).get(i+2);
            userProfile.fullAgenda.get(indice).get(pos).task_4 =  userProfile.agenda.get(indice).get(i+3);
        }

        userProfile.freeDay = NewFreeDay;

        setDayColor(dayButton, NewFreeDay[position]);

    }

    public void clickedSetFixedWorkButtonXmlCallback(View view) {

        int nb_free_day = 0;
        for(int i = 0; i < 7; i++) {
            if(!NewFreeDay[i])
                nb_free_day++;
        }

        if (nb_free_day == 0) {
            Toast.makeText(this, R.string.allFreeDay, Toast.LENGTH_SHORT).show();
            return;
        }

        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        TextView NBOptWorkEditText = findViewById(R.id.NBOptWorkEditText);
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        if (NBWorkEditText.getText().toString().isEmpty()) {
        }
        else if (NBOptWorkEditText.getText().toString().isEmpty()) {
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
        }
        else {
            userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
            userProfile.stringOptWorkTimeToSlot(NBOptWorkEditText.getText().toString());
            userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
            userProfile.sportRoutine = positionSport;
        }
        saveToFile();
        Intent intent = new Intent(this, AgendaActivity.class);
        intent.putExtra(FIXED_WORK, true);
        intent.putExtra(LUNCH_TIME, false);
        startActivity(intent);
    }

    public void clickedSetEatTimeButtonXmlCallback(View view) {

        int nb_free_day = 0;
        for(int i = 0; i < 7; i++) {
            if(!NewFreeDay[i])
                nb_free_day++;
        }

        if (nb_free_day == 0) {
            Toast.makeText(this, R.string.allFreeDay, Toast.LENGTH_SHORT).show();
            return;
        }

        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        TextView NBOptWorkEditText = findViewById(R.id.NBOptWorkEditText);
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        if (NBWorkEditText.getText().toString().isEmpty()) {
        }
        else if (NBOptWorkEditText.getText().toString().isEmpty()) {
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
        }
        else {
            userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
            userProfile.stringOptWorkTimeToSlot(NBOptWorkEditText.getText().toString());
            userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
            userProfile.sportRoutine = positionSport;
        }
        saveToFile();
        Intent intent = new Intent(this, AgendaActivity.class);
        intent.putExtra(FIXED_WORK, false);
        intent.putExtra(LUNCH_TIME, true);
        startActivity(intent);
    }

    public void clickedSportButtonXmlCallback(View view) {

        ImageButton sport = (ImageButton) view;
        final int sportID = sport.getId(); // dayID is the id of the tapped Button
        positionSport = sportIds.indexOf(sportID); // position is the position of the Button (in the ArrayList dayIds)

        setSport();

    }

    public void clickedSaveProfileButtonXmlCallback(View view) {

        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        TextView NBOptWorkEditText = findViewById(R.id.NBOptWorkEditText);
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        boolean allFreeDay = true;
        for (int i = 0; i < NewFreeDay.length; i++){
            if (!NewFreeDay[i]){
                allFreeDay = false;
            }
        }
        if (allFreeDay){
            Toast.makeText(ProfileActivity.this, R.string.allFreeDay, Toast.LENGTH_SHORT).show();
        }
        else if (NBWorkEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetWorkHour, Toast.LENGTH_SHORT).show();
        }else if (NBOptWorkEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetOptWorkHour, Toast.LENGTH_SHORT).show();
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetWakeUp, Toast.LENGTH_SHORT).show();
        }
        else {
            boolean correctnbWork = userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
            boolean correctnbOptWork = userProfile.stringOptWorkTimeToSlot(NBOptWorkEditText.getText().toString());
            boolean correctwakeUp = userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
            if (!correctnbWork) {
                Toast.makeText(ProfileActivity.this, R.string.wrongnbWorkHours, Toast.LENGTH_SHORT).show();
            } else if (!correctnbOptWork) {
                Toast.makeText(ProfileActivity.this, R.string.wrongnbOptWorkHours, Toast.LENGTH_SHORT).show();
            } else if (!correctwakeUp) {
                Toast.makeText(ProfileActivity.this, R.string.wrongwakeUp, Toast.LENGTH_SHORT).show();
            } else {

                saveToFile();
                readFromFile();

                AgendaInitialisation agendaInitialisation = new AgendaInitialisation(getApplicationContext());
                int isOK = agendaInitialisation.slotCalculation();

                if (isOK == 0) {
                    Toast.makeText(ProfileActivity.this, R.string.Saved, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if (isOK == 1){
                    Toast.makeText(ProfileActivity.this, R.string.bad_parameters, Toast.LENGTH_SHORT).show();
                }
                else if (isOK == 2){
                    Toast.makeText(ProfileActivity.this, R.string.too_lots_of_fixedwork, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void saveToFile(){
        try {
            //Context ctx = getApplicationContext();
            //ctx.deleteFile(userProfile.FileName);
            File file = new File(getFilesDir(), userProfile.FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

            userProfile.Save(bufferedWriter);

            bufferedWriter.flush();
            bufferedWriter.close();
            outputStreamWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void clickedSeeUtilisationConditionButtonXmlCallback(View view) {
        Intent intent = new Intent(ProfileActivity.this, GeneralTermsOfUseActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);
    }

    public void onBackPressed() {
        AgendaInitialisation agendaInitialisation = new AgendaInitialisation(getApplicationContext());
        agendaInitialisation.slotCalculation();

        //Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        //startActivity(intent);
        finish();
    }

    public void readFromFile() {
        try {
            Context ctx = getApplicationContext();
            FileInputStream fileInputStream = ctx.openFileInput(userProfile.FileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String lineData = bufferedReader.readLine();

            userProfile.decode(lineData);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clickedBackToMenuButtonXmlCallback (View view) {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int convertedIndice() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        while (offset < 0){
            offset += 7;
        }

        return offset%7;
    }
}