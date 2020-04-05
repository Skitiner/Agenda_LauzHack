package com.example.agenda_lauzhack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    public static final String USER_PROFILE = "USER_PROFILE";
    Profile userProfile = new Profile(); // TODO: 04.04.2020 vérifier si un profil existe deja 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent1 = getIntent();
        if ((Profile) intent1.getSerializableExtra(USER_PROFILE)!=null) {
            userProfile = (Profile) intent1.getSerializableExtra(USER_PROFILE);
        }
        if (!userProfile.licenceAccepted){
            Intent intent = new Intent(MainActivity.this, popupActivity.class);
            intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
            startActivity(intent);
        }
    }

    public void goToAgendaActivity(View view) {
        Intent agenda = new Intent(this, AgendaActivity.class);
        startActivity(agenda);

    }
  
    public void clickedProfileButtonXmlCallback(View view) {

        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
        intent.putExtra(ProfileActivity.USER_PROFILE, userProfile);
        startActivity(intent);

    }

}
