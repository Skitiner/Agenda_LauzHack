package com.example.agenda_lauzhack;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

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

public class AgendaActivity extends AppCompatActivity {

    private final int NB_SLOTS_LUNCH = 6;
    private final String WEEK_SAVE = "WEEK_SAVE";
    private final String DAILY_TASK = "DAILY_TASK";
    private final String POPUP = "POPUP";
    private myAdapter adapter;
    private ListView schedule;
    private static ArrayList<ArrayList<timeSlot>> week;
    private static ArrayList<ArrayList<timeSlot.currentTask>> dailyTasks;
    private static ArrayList<ArrayList<Boolean>> cancel_day_taks;
    private int currentDay;
    private Date currentDate;
    private TextView date;
    private int date_offset;
    private Profile userProfile;

    private boolean fixed_work;
    private boolean lunch_time;
    private boolean first_save;
    private boolean popup;
    private boolean settingFinish = false;
    private int nb_day_to_set;
    private int day_already_setted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        Intent intent = getIntent();
        fixed_work = intent.getBooleanExtra(ProfileActivity.FIXED_WORK, false);
        lunch_time = intent.getBooleanExtra(ProfileActivity.LUNCH_TIME, false);
        popup = intent.getBooleanExtra(POPUP, false);

        first_save = true;
        userProfile = new Profile();

        readFromFile();
        setWeekSlots();

        nb_day_to_set = 0;
        for(int i = 0; i < 7; i++) {
            if(!userProfile.freeDay[i])
                nb_day_to_set++;
        }

        Button nextDay = findViewById(R.id.nextDay);
        Button previousDay = findViewById(R.id.previousDay);

        if (fixed_work || lunch_time){
            compare(userProfile.agenda);

            nextDay.setClickable(false);
            nextDay.setVisibility(View.INVISIBLE);
            previousDay.setClickable(false);
            previousDay.setVisibility(View.INVISIBLE);
        }
        else {
            nextDay.setClickable(true);
            nextDay.setVisibility(View.VISIBLE);
            previousDay.setClickable(true);
            previousDay.setVisibility(View.VISIBLE);
        }
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
        if((fixed_work || lunch_time)) {
            int indice = (conversionDayIndice())%7;
            int i = 0;
            while(userProfile.freeDay[indice] && i < 7) {
                View next = findViewById(R.id.nextDay);
                changeDay(next);
                indice = (conversionDayIndice()+currentDay)%7;
                i++;
            }
        }
        schedule.setAdapter(adapter);

        if(lunch_time || fixed_work)
            schedule.setSelection(6);
        else
            schedule.setSelection(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)-1);

        adapter.updateWeek();

        if(popup) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AgendaActivity.this);
            LayoutInflater inflater = getLayoutInflater();


            builder.setMessage("Would you start your next activity?");
            builder.setTitle("Next Activity !");

            builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent start = new Intent(AgendaActivity.this, Start_broadcast.class);
                    sendBroadcast(start);
                }
            });
            builder.setNeutralButton(R.string.minutes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent postpone = new Intent(AgendaActivity.this, Postpone_broadcast.class);
                    sendBroadcast(postpone);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent cancel = new Intent(AgendaActivity.this, Cancel_broadcast.class);
                    sendBroadcast(cancel);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    readFromFile();
                    setWeekSlots();
                    adapter.updateWeek();
                }
            });
            dialog.show();
        }

    }

    // Fonction pour adapter le dailyTask au jour actuel
    private void setWeekSlots() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        int offset_indice = offset%7;

        dailyTasks = new ArrayList<>(userProfile.agenda);
        cancel_day_taks = new ArrayList<>(userProfile.canceled_slots);

        Log.w("SET_DAY", String.valueOf(setting_day));
        Log.w("ACT_DAY", String.valueOf(actual_day));

        for (int i = 0; i < 7; i++) {
            int indice = (i + offset_indice)%7;
            dailyTasks.set(i, userProfile.agenda.get(indice));
            cancel_day_taks.set(i, userProfile.canceled_slots.get(indice));
        }
    }

    public void saveTimeSlots(View view) {
        day_already_setted++;

        if(day_already_setted >= nb_day_to_set)
            settingFinish = true;

        if(settingFinish) {
            Intent intent = new Intent(AgendaActivity.this, ProfileActivity.class);
            startActivity(intent);
            adapter.updateWeek();
            Toast.makeText(this, R.string.Saved, Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            Toast.makeText(this, R.string.next_day, Toast.LENGTH_SHORT).show();
        }

        if(first_save && !settingFinish) {
            first_save = false;

            AlertDialog.Builder builder = new AlertDialog.Builder(AgendaActivity.this);

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
        else if (!settingFinish){
            View next = findViewById(R.id.nextDay);
            changeDay(next);

        }

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

    private void setScheduleOverDays() {
        int indice;

        for (int i = 0; i < 7; i++) {
            indice = (conversionDayIndice() +i)%7;

            if(userProfile.freeDay[indice] == true)
                continue;

            for(int j = 0; j < 96; j++) {
                Log.w("BUG", " " + currentDay);
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
        if (view == null)
            return;

        if (view.getId() == R.id.nextDay) {
            currentDay++;
            currentDay %= 7;
            date_offset++;

            int indice = (conversionDayIndice() + currentDay)%7;
            int i = 0;

            if(lunch_time || fixed_work) {
                while (userProfile.freeDay[indice] && i < 7) {
                    currentDay++;
                    currentDay %= 7;
                    date_offset++;
                    i++;
                    indice = (conversionDayIndice() + currentDay) % 7;
                }
                if(i == 7) {
                    settingFinish = true;
                }
            }
        } else if (view.getId() == R.id.previousDay) {
            if (currentDay == 0)
                currentDay = 6;
            else
                currentDay--;
            date_offset--;

            int indice = (conversionDayIndice() + currentDay)%7;
            int i = 0;

            if(lunch_time || fixed_work) {
                while (userProfile.freeDay[indice] && i < 7) {
                    if (currentDay == 0)
                        currentDay = 6;
                    else
                        currentDay--;
                    date_offset--;
                    i++;
                    indice = (conversionDayIndice() + currentDay) % 7;
                }
                if(i == 7) {
                    settingFinish = true;
                }
            }
        }

        Log.w("CURRENT DAY", " " +currentDay);
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

    private void saveToFile(){
        try {
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

    private void readFromFile() {
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
            setItemApparence((TextView) row.findViewById(R.id.a_1), getItem(position).task_1, position, 0);
            setItemApparence((TextView) row.findViewById(R.id.a_2), getItem(position).task_2, position, 1);
            setItemApparence((TextView) row.findViewById(R.id.a_3), getItem(position).task_3, position, 2);
            setItemApparence((TextView) row.findViewById(R.id.a_4), getItem(position).task_4, position, 3);


            return row;
        }

        private void setItemApparence(TextView textView, timeSlot.currentTask task, int position, int task_num) {

            int slot_indice = position*4 + task_num;
            textView.setPaintFlags(Paint.ANTI_ALIAS_FLAG);

            switch (task) {
                case SPORT:
                    textView.setText(R.string.Sport);
                    textView.setBackgroundColor(getResources().getColor(R.color.orange, null));
                    if(cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                        textView.setBackgroundColor(getResources().getColor(R.color.orange_canceled, null));

                    break;

                case WORK:
                    textView.setText(R.string.work);
                    textView.setBackgroundColor(getResources().getColor(R.color.salmonpink, null));
                    if(cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                        textView.setBackgroundColor(getResources().getColor(R.color.salmonpink_canceled, null));
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
                    if(cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                        textView.setBackgroundColor(getResources().getColor(R.color.gray_canceled, null));
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
                    textView.setBackgroundColor(getResources().getColor(R.color.green, null));
                    break;

            }

            if(cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
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

            if(lunch_time || fixed_work) {
                userProfile.agenda = dailyTasks;
                saveToFile();
            }
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

            if(lunch_time || fixed_work) {
                userProfile.agenda = dailyTasks;
                saveToFile();
            }
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
                    if (v.getId() == R.id.t_1) {
                        if  (dailyTasks.get(currentDay).get(daily_task_pos) != timeSlot.currentTask.EAT ||
                                dailyTasks.get(currentDay).get(daily_task_pos+1) != timeSlot.currentTask.EAT ||
                                dailyTasks.get(currentDay).get(daily_task_pos+2) != timeSlot.currentTask.EAT ||
                                dailyTasks.get(currentDay).get(daily_task_pos+3) != timeSlot.currentTask.EAT) {


                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.EAT);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.EAT);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.EAT);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.EAT);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);
                        }

                    } else {
                        if ( dailyTasks.get(currentDay).get(daily_task_pos-1) != timeSlot.currentTask.EAT)
                            dailyTasks.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.EAT);
                        else
                            dailyTasks.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.FREE);
                    }
                    updateAgenda();

                }

                if(lunch_time || fixed_work) {
                    userProfile.agenda = dailyTasks;
                    saveToFile();
                }
            }
        }
    }

    private int compare(ArrayList<ArrayList<timeSlot.currentTask>> week_slots){
        int nbFixedWork = 0;

        int offset_indice = convertedIndice();

        for (int i = 0; i < 7; i++) {
            int indice = (i + offset_indice)%7;

            for (int j = 0; j < week_slots.get(indice).size(); j++) {
                if (!(week_slots.get(indice).get(j) == timeSlot.currentTask.WORK_FIX || week_slots.get(indice).get(j) == timeSlot.currentTask.EAT)){
                    dailyTasks.get(i).set(j, timeSlot.currentTask.FREE);
                }
            }
        }
        return nbFixedWork;
    }

   private int convertedIndice() {
        int setting_day = userProfile.settingDay.get(Calendar.DAY_OF_YEAR);
        int actual_day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        int year_offset =  Calendar.getInstance().get(Calendar.YEAR) - userProfile.settingDay.get(Calendar.YEAR);
        int offset = 365*year_offset + actual_day - setting_day + (int) (0.25*(year_offset + 3));

        return offset%7;
    }
}

