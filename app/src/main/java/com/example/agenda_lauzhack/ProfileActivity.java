package com.example.agenda_lauzhack;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

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

        if (userProfile != null) {
            positionSport = userProfile.sportRoutine;
            setProfileInfo();
        }
    }

    private void setProfileInfo() {
        TextView NBWorkEditText = findViewById(R.id.NBWorkEditText);
        NBWorkEditText.setText(userProfile.nbWorkHours);

        setFreeDay();

        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);
        WakeUpEditText.setText(userProfile.wakeUp);

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

        setDayColor(mondayButton, userProfile.freeDay[0]);
        setDayColor(tuesdayButton, userProfile.freeDay[1]);
        setDayColor(wednesdayButton, userProfile.freeDay[2]);
        setDayColor(thursdayButton, userProfile.freeDay[3]);
        setDayColor(fridayButton, userProfile.freeDay[4]);
        setDayColor(saturdayButton, userProfile.freeDay[5]);
        setDayColor(sundayButton, userProfile.freeDay[6]);
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

        userProfile.freeDay[position] = !userProfile.freeDay[position];

        setDayColor(dayButton, userProfile.freeDay[position]);

        saveToFile();

    }

    public void clickedSetFixedWorkButtonXmlCallback(View view) {
        Intent intent = new Intent(this, AgendaActivity.class);
        intent.putExtra(FIXED_WORK, true);
        intent.putExtra(LUNCH_TIME, false);
        intent.putExtra(USER_PROFILE, userProfile);
        startActivity(intent);
    }

    public void clickedSetEatTimeButtonXmlCallback(View view) {

        Intent intent = new Intent(this, AgendaActivity.class);
        intent.putExtra(FIXED_WORK, false);
        intent.putExtra(LUNCH_TIME, true);
        intent.putExtra(USER_PROFILE, userProfile);
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
        TextView WakeUpEditText = findViewById(R.id.WakeupEditText);

        if (NBWorkEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetWorkHour, Toast.LENGTH_SHORT).show();
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, R.string.forgetWakeUp, Toast.LENGTH_SHORT).show();
        }
        else {
            userProfile.nbWorkHours = NBWorkEditText.getText().toString();
            userProfile.wakeUp = WakeUpEditText.getText().toString();
            userProfile.sportRoutine = positionSport;

            saveToFile();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            startActivity(intent);

            Toast.makeText(ProfileActivity.this, R.string.Saved, Toast.LENGTH_SHORT).show();
        }
    }

    public void saveToFile(){
        try {
            File file = new File(getFilesDir(), userProfile.FileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            //FileOutputStream fileOutputStream = ctx.openFileOutput(userprofileFileName, Context.MODE_PRIVATE);
            //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            //BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

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
        startActivity(intent);
    }
}

