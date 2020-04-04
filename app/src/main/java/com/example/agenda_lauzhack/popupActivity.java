package com.example.agenda_lauzhack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Scanner;

public class popupActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";
    Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        Intent intent = getIntent();
        userProfile = (Profile) intent.getSerializableExtra(USER_PROFILE);
        try {
            setText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setText() throws IOException {
        InputStream is = getApplicationContext().getAssets().open("Conditions_generales_dutilisation_MotiveMe.txt");

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";

        TextView conditionTextView = findViewById(R.id.utilisationCondition);
        conditionTextView.setText(result);
    }

    public void clickedAcceptButtonXmlCallback(View view) {
        userProfile.licenceAccepted = true;
        Intent intent = new Intent(popupActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);
    }



}
