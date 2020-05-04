package com.ludiostrix.organise_mois;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
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
        if (userProfile.licenceAccepted){
            Button acceptButton = findViewById(R.id.acceptButton);
            acceptButton.setVisibility(View.GONE);
        }
        else {
            FloatingActionButton backToProfileButton = findViewById(R.id.backToProfile);
            backToProfileButton.setVisibility(View.GONE);
        }
    }

    public void setText() throws IOException {
        InputStream is = getApplicationContext().getAssets().open("General terms and conditions of use.txt");
        String a = Locale.getDefault().getLanguage();
        if (Locale.getDefault().getLanguage().equals("en")) {
            is = getApplicationContext().getAssets().open("General terms and conditions of use.txt");
        }
        else if(Locale.getDefault().getLanguage().equals("fr")){
            is = getApplicationContext().getAssets().open("Conditions générales d'utilisation.txt");
        }
        //InputStreamReader is_utf = new InputStreamReader(is, StandardCharsets.UTF_8);

        Scanner s = new Scanner(is).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";

        result = result.replaceAll("\\uFFFD", "\u00AE");

        TextView conditionTextView = findViewById(R.id.utilisationCondition);
        conditionTextView.setTextColor(getResources().getColor(R.color.black, null));
        conditionTextView.setText(result);
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

    public void clickedAcceptButtonXmlCallback(View view) {
        userProfile.licenceAccepted = true;

        saveToFile();

        Intent intent = new Intent(popupActivity.this, ProfileActivity.class);
        startActivity(intent);
    }

    public void clickedBackToProfileButtonXmlCallback(View view) {
        Intent intent = new Intent(popupActivity.this, ProfileActivity.class);
        startActivity(intent);
    }



}
