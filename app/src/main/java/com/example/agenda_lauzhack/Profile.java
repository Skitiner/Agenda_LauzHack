package com.example.agenda_lauzhack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;

public class Profile implements Serializable {
    private static final String TAG = "Profile";

    protected boolean licenceAccepted;
    protected String nbWorkHours;
    protected boolean[] freeDay;
    protected String wakeUp;
    protected int sportRoutine;
    protected String FileName;

    public Profile(){
        this.licenceAccepted = false;
        this.nbWorkHours = "42";
        this.freeDay = new boolean[] {false, false, false, false, false, false, false};
        this.wakeUp = "8";
        this.sportRoutine = 1;
        this.FileName = "userProfile.txt";
    }

    public void Save(BufferedWriter bufferedWriter){
        try {
            bufferedWriter.write(String.valueOf(this.licenceAccepted));
            bufferedWriter.write("/");
            bufferedWriter.write(this.nbWorkHours);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decode(String lineData) {
        String lA="";
        String nW="";
        Character[] fD= new Character[] {'0','0','0','0','0','0','0'};
        String wU="";
        String sR="";
        int j = 0;
        int n = 0;

        for (int i=0;i<lineData.length();i++) {
            if (lineData.charAt(i) !='/'){
                switch (j){
                    case 0 : lA += lineData.charAt(i);
                             break;
                    case 1 : nW += lineData.charAt(i);
                             break;
                    case 2 : fD[n] = lineData.charAt(i);
                             n++;
                             break;
                    case 3 : wU += lineData.charAt(i);
                             break;
                    case 4 : sR += lineData.charAt(i);
                             break;
                }
            }
            else {
                j++;
            }
        }

        this.licenceAccepted = Boolean.parseBoolean(lA);
        this.nbWorkHours = nW;
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
    }

}
