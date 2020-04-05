package com.example.agenda_lauzhack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgendaActivity extends AppCompatActivity  {

    private final int NB_SLOTS_LUNCH = 6;
    private final String WEEK_SAVE = "WEEK_SAVE";
    private final String DAILY_TASK = "DAILY_TASK";
    private myAdapter adapter;
    private ListView schedule;
    private static ArrayList<ArrayList<timeSlot>> week;
    private static ArrayList<ArrayList<timeSlot.currentTask>> dailyTasks;
    private int currentDay;
    private Date currentDate;
    private TextView date;
    private int date_offset;
    private Profile userProfile;

    private boolean fixed_work;
    private boolean lunch_time;
    private boolean first_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        Intent intent = getIntent();
        fixed_work = intent.getBooleanExtra(ProfileActivity.FIXED_WORK, false);
        lunch_time = intent.getBooleanExtra(ProfileActivity.LUNCH_TIME, false);
        first_save = true;
        userProfile = new Profile();

        readFromFile();
        dailyTasks = userProfile.agenda;

        Log.w("DAILY", dailyTasks.toString());

        Log.w("BAVE", "size=" + dailyTasks.size());

        View view = findViewById(R.id.save_schedule);
        if(!fixed_work && !lunch_time)
            view.setVisibility(View.INVISIBLE);
        else
            view.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {
            week = (ArrayList) savedInstanceState.getSerializable(WEEK_SAVE);
            dailyTasks = (ArrayList) savedInstanceState.getSerializable(DAILY_TASK);
        }

        if(adapter == null)
            adapter = new myAdapter(this.getApplicationContext(), R.layout.time_slot);

        if (week == null){
            week = new ArrayList<>();

            timeSlot.currentTask task;

            for(int j = 0; j < 7; j++ ) {
                ArrayList<timeSlot> mySlots = new ArrayList<>();
                for (int i = 0; i < 24; i++) {

                    task = timeSlot.currentTask.FREE;

                    timeSlot slot = new timeSlot();
                    slot.time = i;
                    slot.task_1 = task;
                    slot.task_2 = task;
                    slot.task_3 = task;
                    slot.task_4 = task;

                    mySlots.add(slot);

                }
                week.add(mySlots);
            }
        }
        schedule = findViewById(R.id.schedule);

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        date = findViewById(R.id.date);
        currentDate = new Date();
        date.setText(formatter.format(currentDate));
        currentDay = 0;
        date_offset = 0;

        //Go to first working day
        if(fixed_work || lunch_time) {
            int indice = (conversionDayIndice())%7;
            while(userProfile.freeDay[indice]) {
                View next = findViewById(R.id.nextDay);
                changeDay(next);
                Log.w("TEST", "day: " + currentDay);
                indice = (conversionDayIndice()+currentDay)%7;
            }
        }
        schedule.setAdapter(adapter);
        schedule.setSelection(6);

        // Calculation of optimal schedule
        if(userProfile != null && userProfile.calculation) {

            DaySlotsCalculation myCalculation = new DaySlotsCalculation(userProfile);
            dailyTasks = myCalculation.slotCalculation(dailyTasks);
            adapter.updateWeek();
        }

        adapter.updateWeek();

    }

    public void saveTimeSlots(View view) {
        if(first_save) {
            first_save = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(AgendaActivity.this);
            LayoutInflater inflater = getLayoutInflater();


            builder.setMessage(R.string.same_day);
            builder.setTitle(R.string.same_day_title);

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    setScheduleOverDays();

                    Intent intent = new Intent(AgendaActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    View next = findViewById(R.id.nextDay);
                    changeDay(next);
                    dialog.cancel();
                }
            });

            AlertDialog dialog = builder.create();

            dialog.show();
        }
        else {
            if(currentDay == 6) {
                Intent intent = new Intent(AgendaActivity.this, ProfileActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Next Day", Toast.LENGTH_SHORT).show();
            }

            View next = findViewById(R.id.nextDay);
            changeDay(next);

        }

    }

    private int conversionDayIndice() {
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

    private void setScheduleOverDays() {
        int indice;

        for (int i = 0; i < 7; i++) {
            indice = (conversionDayIndice() +i)%7;

            if(userProfile.freeDay[indice] == true)
                continue;

            for(int j = 0; j < 96; j++) {
                if(dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.WORK_FIX && fixed_work)
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));

                if(dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.EAT && lunch_time)
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));
            }
        }

        adapter.updateWeek();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(WEEK_SAVE, week);
        outState.putSerializable(DAILY_TASK, dailyTasks);
    }

    public void goToday(View view) {
        switch (view.getId()) {
            case R.id.date:
                currentDay = 0;
                date_offset = 0;
                adapter.updateAgenda();
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
                date = findViewById(R.id.date);
                currentDate = new Date();
                date.setText(formatter.format(currentDate));
                break;
        }
    }

    public void changeDay(View view) {
        adapter.clear();
        if(view.getId() == R.id.nextDay){
            currentDay++;
            currentDay %= 7;
            date_offset++;
        }
        else if(view.getId() == R.id.previousDay){
            if(currentDay == 0)
                currentDay = 6;
            else
                currentDay--;

            date_offset--;
        }
        adapter.addAll(week.get(currentDay));

        // Start date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        String dt = sdf.format(currentDate);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(dt));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, date_offset);  // number of days to add
        dt = sdf.format(c.getTime());

        date.setText(dt);
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

    private class myAdapter extends ArrayAdapter<timeSlot> {

        private int time_layout;

        public myAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            time_layout = resource;
        }

        @NonNull
        @Override
        public View getView(int position,
                            @Nullable View convertView,
                            @NonNull ViewGroup parent) {

            View row = convertView;

            if (row == null) {
                //Inflate it from layout
                row = LayoutInflater.from(getContext())
                        .inflate(time_layout, parent, false);
            }

            row.findViewById(R.id.t_1).setOnClickListener(new textViewOnClickListener(row, position, 0));
            row.findViewById(R.id.a_1).setOnClickListener(new textViewOnClickListener(row, position, 1));
            row.findViewById(R.id.a_2).setOnClickListener(new textViewOnClickListener(row, position, 2));
            row.findViewById(R.id.a_3).setOnClickListener(new textViewOnClickListener(row, position, 3));
            row.findViewById(R.id.a_4).setOnClickListener(new textViewOnClickListener(row, position, 4));


            ((TextView) row.findViewById(R.id.t_1)).setText(getItem(position).time + "h");
            setItemApparence((TextView) row.findViewById(R.id.a_1), getItem(position).task_1);
            setItemApparence((TextView) row.findViewById(R.id.a_2), getItem(position).task_2);
            setItemApparence((TextView) row.findViewById(R.id.a_3), getItem(position).task_3);
            setItemApparence((TextView) row.findViewById(R.id.a_4), getItem(position).task_4);


            return row;
        }

        private void setItemApparence(TextView textView, timeSlot.currentTask task) {
            switch (task) {
                case SPORT:
                    textView.setText(R.string.Sport);
                    textView.setBackgroundColor(getResources().getColor(R.color.orange, null));
                    break;

                case WORK:
                    textView.setText(R.string.work);
                    textView.setBackgroundColor(getResources().getColor(R.color.salmonpink, null));
                    break;

                case EAT:
                    textView.setText(R.string.eat);
                    textView.setBackgroundColor(getResources().getColor(R.color.pastel, null));
                    break;

                case FREE:
                    textView.setText("-");
                    textView.setBackgroundColor(getResources().getColor(R.color.green, null));
                    break;

                case WORK_FIX:
                    textView.setText(R.string.fixed_work);
                    textView.setBackgroundColor(getResources().getColor(R.color.gray, null));
                    break;
                case MORNING_ROUTINE:
                    textView.setText(R.string.morningRoutine);
                    textView.setBackgroundColor(getResources().getColor(R.color.pastel, null));
                    break;
                case SLEEP:
                    textView.setText(R.string.sleep);
                    textView.setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    break;
                case PAUSE:
                    textView.setText(R.string.pause);
                    textView.setBackgroundColor(getResources().getColor(R.color.black, null));
                    break;

            }
        }

        public void updateAgenda() {
            int position;
            for(int i = 0; i < 96; i +=4 ) {
                position = i/4;
                week.get(currentDay).get(position).task_1 =  dailyTasks.get(currentDay).get(i);
                week.get(currentDay).get(position).task_2 =  dailyTasks.get(currentDay).get(i+1);
                week.get(currentDay).get(position).task_3 =  dailyTasks.get(currentDay).get(i+2);
                week.get(currentDay).get(position).task_4 =  dailyTasks.get(currentDay).get(i+3);
            }
            adapter.clear();
            adapter.addAll(week.get(currentDay));
            userProfile.agenda = dailyTasks;
            Log.w("TEST", userProfile.agenda.toString());
            saveToFile();
        }

        public void updateWeek() {
            int position;
            for(int j = 0; j < 7; j++) {
                for (int i = 0; i < 96; i += 4) {
                    position = i / 4;
                    week.get(j).get(position).task_1 = dailyTasks.get(j).get(i);
                    week.get(j).get(position).task_2 = dailyTasks.get(j).get(i + 1);
                    week.get(j).get(position).task_3 = dailyTasks.get(j).get(i + 2);
                    week.get(j).get(position).task_4 = dailyTasks.get(j).get(i + 3);
                }
            }
            adapter.clear();
            adapter.addAll(week.get(currentDay));
            userProfile.agenda = dailyTasks;
            saveToFile();
        }


        private class textViewOnClickListener implements View.OnClickListener {

            View row;
            int position;
            int taskNum;
            int daily_task_pos;

            public textViewOnClickListener(View row, int position, int taskNum) {
                this.row = row;
                this.position = position;
                this.taskNum = taskNum;
                daily_task_pos = position*4 + taskNum;
            }

            @Override
            public void onClick(View v) {

                if(fixed_work) {
                    if (v.getId() == R.id.t_1) {
                        if  (dailyTasks.get(currentDay).get(daily_task_pos) != timeSlot.currentTask.WORK_FIX ||
                                dailyTasks.get(currentDay).get(daily_task_pos+1) != timeSlot.currentTask.WORK_FIX ||
                                dailyTasks.get(currentDay).get(daily_task_pos+2) != timeSlot.currentTask.WORK_FIX ||
                                dailyTasks.get(currentDay).get(daily_task_pos+3) != timeSlot.currentTask.WORK_FIX) {


                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.WORK_FIX);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.WORK_FIX);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.WORK_FIX);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.WORK_FIX);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);
                        }

                    } else {
                        if ( dailyTasks.get(currentDay).get(daily_task_pos-1) != timeSlot.currentTask.WORK_FIX)
                            dailyTasks.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.WORK_FIX);
                        else
                            dailyTasks.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.FREE);
                    }
                    updateAgenda();
                }

                if(lunch_time) {
                    if (v.getId() != R.id.t_1)
                    {
                        if ( dailyTasks.get(currentDay).get(daily_task_pos-1) != timeSlot.currentTask.EAT) {
                            for(int i = 0; i < NB_SLOTS_LUNCH; i++) {
                                if((daily_task_pos-1+i) >=  dailyTasks.get(currentDay).size()) {
                                    break;
                                }
                                dailyTasks.get(currentDay).set(daily_task_pos-1 + i, timeSlot.currentTask.EAT);
                            }
                        }
                        else {
                            for(int i = -NB_SLOTS_LUNCH; i < NB_SLOTS_LUNCH; i++) {

                                if((daily_task_pos-1+i) >=  dailyTasks.get(currentDay).size()) {
                                    break;
                                }
                                if(dailyTasks.get(currentDay).get(daily_task_pos-1+i) == timeSlot.currentTask.EAT)
                                    dailyTasks.get(currentDay).set(daily_task_pos-1+i, timeSlot.currentTask.FREE);
                            }
                        }
                        updateAgenda();

                    }

                }

                userProfile.agenda = dailyTasks;
                saveToFile();
            }
        }
    }
}

