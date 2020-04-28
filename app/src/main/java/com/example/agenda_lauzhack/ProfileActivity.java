package com.example.agenda_lauzhack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import static com.example.agenda_lauzhack.AgendaActivity.conversionDayIndice;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String FIXED_WORK = "FIXED_WORK";
    public static final String LUNCH_TIME = "LUNCH_TIME";

    private ArrayList<Integer> dayIds = new ArrayList() {
        {
            add(R.id.monday); add(R.id.tuesday); add(R.id.wednesday); add(R.id.thursday);
            add(R.id.friday); add(R.id.saturday); add(R.id.sunday);
        }
    };

    private ArrayList<Integer> sportIds = new ArrayList() {
        {
            add(R.id.sloth); add(R.id.rabbit); add(R.id.cheetah);
        }
    };

    Profile userProfile = new Profile();

    private int positionSport;
    private boolean[] NewFreeDay;

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
        }
        else {
            positionSport = userProfile.sportRoutine;
            NewFreeDay = userProfile.freeDay;
        }

        if (userProfile != null) {
            setProfileInfo();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);
        userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
        userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
        state.putSerializable("nbWorkHours", userProfile.nbWorkHours);
        state.putSerializable("wakeUp", userProfile.wakeUp);
        state.putSerializable("freeDay", NewFreeDay);
        state.putSerializable("sportRoutine", positionSport);
    }

    private void setProfileInfo() {
        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        NBWorkEditText.setText(userProfile.slotStringToTimeString(userProfile.nbWorkHours));

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
            if(userProfile.agenda.get(indice).get(i) == timeSlot.currentTask.WORK_FIX || userProfile.agenda.get(indice).get(i) == timeSlot.currentTask.EAT) {
                userProfile.agenda.get(indice).set(i, timeSlot.currentTask.FREE);
            }
        }
        userProfile.freeDay = NewFreeDay;

        saveToFile();
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
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        if (NBWorkEditText.getText().toString().isEmpty()) {
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
        }
        else {
            userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
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
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        if (NBWorkEditText.getText().toString().isEmpty()) {
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
        }
        else {
            userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
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

    public boolean isWorkHour(String time){
        boolean ok = false;
        boolean entier = true;
        String hour = "0";
        String min = "0";
        int hourt;
        int mint;
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
             hourt = Integer.parseInt(hour);
             mint = Integer.parseInt(min);
             ok = true;
             if (hourt > 70 || hourt < 0){
                 ok = false;
             }
             if (mint > 30 && mint < 60){
                 hourt = (hourt + 1)%24;
             }
             else if (mint >= 60 || mint < 0){
                 ok = false;
             }
             if(ok){
                 userProfile.nbWorkHours = String.valueOf(hourt);
             }
         } catch (Exception e) {
             e.printStackTrace();
             ok=false;
         }
        return ok;
    }

    public void clickedSaveProfileButtonXmlCallback(View view) {

        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
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
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetWakeUp, Toast.LENGTH_SHORT).show();
        }
        else {
            boolean correctnbWork = userProfile.stringWorkTimeToSlot(NBWorkEditText.getText().toString());
            boolean correctwakeUp = userProfile.stringTimewakeUpToFloat(WakeUpEditText.getText().toString());
            if (!correctnbWork) {
                Toast.makeText(ProfileActivity.this, R.string.wrongnbWorkHours, Toast.LENGTH_SHORT).show();
            } else if (!correctwakeUp) {
                Toast.makeText(ProfileActivity.this, R.string.wrongwakeUp, Toast.LENGTH_SHORT).show();
            } else {
                userProfile.sportRoutine = positionSport;
                userProfile.settingDay = Calendar.getInstance();
                userProfile.settingDay.setTimeInMillis(System.currentTimeMillis());

                saveToFile();

                DaySlotsCalculation daySlotsCalculation = new DaySlotsCalculation(getApplicationContext());
                int isOK = daySlotsCalculation.slotCalculation();

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
        Log.w("SAVED PROFILE", "SAVED");
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
        Intent intent = new Intent(ProfileActivity.this, popupActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);
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
        finish();
        startActivity(intent);
    }
}

