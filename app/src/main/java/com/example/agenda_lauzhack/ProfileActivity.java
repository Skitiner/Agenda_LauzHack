package com.example.agenda_lauzhack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";

    private ArrayList<Integer> dayIds = new ArrayList() {
        {
            add(R.id.monday); add(R.id.tuesday); add(R.id.wednesday); add(R.id.thursday);
            add(R.id.friday); add(R.id.saturday); add(R.id.sunday);
        }
    };

    private ArrayList<Integer> sportIds = new ArrayList() {
        {
            add(R.id.koala); add(R.id.rabbit); add(R.id.leopard);
        }
    };

    Profile userProfile;

    private int positionSport;
    private boolean[] NewFreeDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent intent = getIntent();
        userProfile = (Profile) intent.getSerializableExtra(USER_PROFILE);
        if (userProfile != null) {
            positionSport = userProfile.sportRoutine;
            NewFreeDay = userProfile.freeDay;
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
        ImageButton koalaSportButton = findViewById(R.id.koala);
        ImageButton rabbitSportButton = findViewById(R.id.rabbit);
        ImageButton leopardSportButton = findViewById(R.id.leopard);

        if (positionSport==0){
            koalaSportButton.setBackgroundColor(0xFF00897B);
            rabbitSportButton.setBackgroundColor(0xFFFF0000);
            leopardSportButton.setBackgroundColor(0xFFFF0000);
        }
        else if (positionSport==1) {
            koalaSportButton.setBackgroundColor(0xFFFF0000);
            rabbitSportButton.setBackgroundColor(0xFF00897B);
            leopardSportButton.setBackgroundColor(0xFFFF0000);
        }
        else {
            koalaSportButton.setBackgroundColor(0xFFFF0000);
            rabbitSportButton.setBackgroundColor(0xFFFF0000);
            leopardSportButton.setBackgroundColor(0xFF00897B);
        }

    }

    public void clickedDayButtonXmlCallback(View view) {

        Button day = (Button) view;
        final int dayID = day.getId(); // dayID is the id of the tapped Button
        final int position = dayIds.indexOf(dayID); // position is the position of the Button (in the ArrayList dayIds)

        Button dayButton = findViewById(dayID);

        NewFreeDay[position] = !userProfile.freeDay[position];

        setDayColor(dayButton, userProfile.freeDay[position]);

    }

    public void clickedSetFixedWorkButtonXmlCallback(View view) {
        // TODO: 04.04.2020 huuu
        Toast.makeText(ProfileActivity.this, "Fixed work", Toast.LENGTH_SHORT).show();
    }
    public void clickedSetEatTimeButtonXmlCallback(View view) {
        // TODO: 04.04.2020 huuu
        Toast.makeText(ProfileActivity.this, "Lunch Time", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ProfileActivity.this, "You forgot to enter a number of work hour !", Toast.LENGTH_SHORT).show();
        }
        else if (WakeUpEditText.getText().toString().isEmpty()) {
            Toast.makeText(ProfileActivity.this, "You forgot to enter a wake up hour !", Toast.LENGTH_SHORT).show();
        }
        else {
            userProfile.nbWorkHours = NBWorkEditText.getText().toString();
            userProfile.freeDay = NewFreeDay;
            userProfile.wakeUp = WakeUpEditText.getText().toString();
            userProfile.sportRoutine = positionSport;
            Toast.makeText(ProfileActivity.this, "Correctly saved !", Toast.LENGTH_SHORT).show();
        }

        // TODO: 04.04.2020 save sur le natel

    }

    public void clickedBackToMenuButtonXmlCallback(View view) {

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);

    }
}
