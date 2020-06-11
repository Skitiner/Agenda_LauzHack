package com.ludiostrix.organise_mois;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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

    private int event;
    private int eventColor = 0;

    private final ArrayList<Integer> colorIds = new ArrayList() {
        {
            add(R.id.green); add(R.id.red); add(R.id.orange); add(R.id.gray);
            add(R.id.pastel); add(R.id.salmonpink); add(R.id.pastelviolet);
        }
    };

    private final ArrayList<Integer> color = new ArrayList() {
        {
            add(R.color.green); add(R.color.red); add(R.color.orange); add(R.color.gray);
            add(R.color.pastel); add(R.color.salmonpink); add(R.color.pastelviolet);
        }
    };

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
                }
            });
            dialog.show();
        }

    }
    @Override
    public void onBackPressed() {
        finish();
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

            userProfile.agenda = dailyTasks;
            saveToFile();

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
                    saveToFile();
                    Intent intent = new Intent(AgendaActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
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
            setItemApparence((TextView) row.findViewById(R.id.a_1), getItem(position).task_1, getItem(position).new_task_1, position, 0);
            setItemApparence((TextView) row.findViewById(R.id.a_2), getItem(position).task_2, getItem(position).new_task_2, position, 1);
            setItemApparence((TextView) row.findViewById(R.id.a_3), getItem(position).task_3, getItem(position).new_task_3, position, 2);
            setItemApparence((TextView) row.findViewById(R.id.a_4), getItem(position).task_4, getItem(position).new_task_4, position, 3);


            return row;
        }

        private void setItemApparence(TextView textView, timeSlot.currentTask task, String new_task, int position, int task_num) {

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

                case NEWEVENT:
                    for (int i = 0; i <= userProfile.savedEvent.size(); i++){
                        if (userProfile.savedEvent.get(i).name == new_task) {
                            textView.setText(userProfile.savedEvent.get(i).name);
                            textView.setBackgroundColor(getResources().getColor(userProfile.savedEvent.get(i).color, null));
                            break;
                        }
                    }
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
        }

        public void updateWeekAgenda() {
            int position;
            for(int i = 0; i < 96; i +=4 ) {
                position = i/4;
                dailyTasks.get(currentDay).set(i, week.get(currentDay).get(position).task_1);
                dailyTasks.get(currentDay).set(i+1, week.get(currentDay).get(position).task_2);
                dailyTasks.get(currentDay).set(i+2, week.get(currentDay).get(position).task_3);
                dailyTasks.get(currentDay).set(i+3, week.get(currentDay).get(position).task_4);
            }
            adapter.clear();
            adapter.addAll(week.get(currentDay));
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
                if (!lunch_time && ! fixed_work) {
                    if (dailyTasks.get(currentDay).get(daily_task_pos-1) == timeSlot.currentTask.NEWEVENT){
                        dailyTasks.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.FREE);
                        updateAgenda();
                    }
                    else {
                        eventCreation(v);
                    }
                }
            }
        }
    }

    public void eventCreation(View v){

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_new_event, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);


        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

        event = -1;

        colorButtonListener(popupView);

        setAutoComplete(popupView);

        confirmButtonListener(popupView, popupWindow);

    }

    public void setAutoComplete(final View popupView){
        final String[] EVENT_NAME = new String[userProfile.savedEvent.size()];

        for (int i = 0; i < userProfile.savedEvent.size(); i++){
            EVENT_NAME[i] = userProfile.savedEvent.get(i).name;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String> (popupView.getContext(), android.R.layout.simple_dropdown_item_1line, EVENT_NAME);
        AutoCompleteTextView textView = (AutoCompleteTextView) popupView.findViewById(R.id.eventNameAutoCompleteTextView);
        textView.setAdapter(adapter);

    }

    public void colorButtonListener(final View popupView){

        for (Integer buttonId : colorIds) {
            final Button colorButton = popupView.findViewById(buttonId);
            final int colorID = colorButton.getId(); // colorID is the id of the tapped Button

            colorButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    eventColor = colorIds.indexOf(colorID); // position is the position of the Button (in the ArrayList colorIds)
                    colorButton.setText("OK");
                    for (Integer id : colorIds) {
                        if (id != colorID) {
                            Button b = popupView.findViewById(id);
                            b.setText("");
                        }
                    }
                }
            });
        }
    }

    public void confirmButtonListener(final View popupView, final PopupWindow popupWindow){

        Button confirm = popupView.findViewById(R.id.confirmButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AutoCompleteTextView autotextView = (AutoCompleteTextView) popupView.findViewById(R.id.eventNameAutoCompleteTextView);
                LinearLayout linearLayout = popupView.findViewById(R.id.eventParameter);
                LinearLayout autoCompletLayout = popupView.findViewById(R.id.autoCompleteLayout);

                if (autotextView.getText().toString().isEmpty()){
                    Toast.makeText(AgendaActivity.this, R.string.forgetEventName, Toast.LENGTH_SHORT).show();
                }
                else if (linearLayout.getVisibility() == View.GONE){
                    for (int i = 0; i < userProfile.savedEvent.size(); i++){
                        if (autotextView.getText().toString().equals(userProfile.savedEvent.get(i).name)){
                            event = i;
                            break;
                        }
                    }

                    linearLayout.setVisibility(View.VISIBLE);
                    autoCompletLayout.setVisibility(View.GONE);
                    TextView eventName = popupView.findViewById(R.id.eventName);
                    eventName.setText(autotextView.getText().toString());

                    if (event >= 0){
                        int colorIndex = 0;

                        for (int i = 0; i < color.size(); i++){
                            if (color.get(i) == userProfile.savedEvent.get(event).color){
                                colorIndex = i;
                                break;
                            }
                        }

                        Button buttonColor = popupView.findViewById(colorIds.get(colorIndex));
                        final int colorID = buttonColor.getId(); // colorID is the id of the tapped Button

                        eventColor = colorIds.indexOf(colorID); // position is the position of the Button (in the ArrayList colorIds)
                        buttonColor.setText("OK");
                        for (Integer id : colorIds) {
                            if (id != colorID) {
                                Button b = popupView.findViewById(id);
                                b.setText("");
                            }
                        }

                    }
                }

                else {
                    EditText startTimeET = popupView.findViewById(R.id.eventStartEditText);
                    EditText stopTimeET = popupView.findViewById(R.id.eventStopEditText);

                    if (startTimeET.getText().toString().isEmpty()) {
                        Toast.makeText(AgendaActivity.this, R.string.forgetEventStart, Toast.LENGTH_SHORT).show();
                    }
                    else if (stopTimeET.getText().toString().isEmpty()) {
                        Toast.makeText(AgendaActivity.this, R.string.forgetEventStop, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        float startTime = stringTimeToFloat(startTimeET.getText().toString());
                        float stopTime = stringTimeToFloat(stopTimeET.getText().toString());

                        if (startTime != -1 && stopTime != -1) {

                            if (event < 0) {
                                newEvent nE = new newEvent();
                                nE.name = autotextView.getText().toString();
                                nE.color = color.get(eventColor);
                                userProfile.savedEvent.add(nE);
                                if (userProfile.savedEvent.size() > 20) {
                                    userProfile.savedEvent.remove(0);
                                }
                                event = userProfile.savedEvent.size()-1;
                            } else {
                                newEvent nE = new newEvent();
                                nE.name = userProfile.savedEvent.get(event).name;
                                nE.color = color.get(eventColor);
                                userProfile.savedEvent.remove(event);
                                userProfile.savedEvent.add(nE);
                            }
                            popupWindow.dismiss();
                            setNewEvent(startTime, stopTime, userProfile.savedEvent.get(event).name);

                        }
                        else if (startTime == -1){
                            Toast.makeText(AgendaActivity.this, R.string.wrongEventStart, Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(AgendaActivity.this, R.string.wrongEventStop, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

    }

    public void setNewEvent(float startTime, float stopTime, String eventName) {
        for (int i = (int)startTime; i < (int)stopTime; i++) {
            if (i == (int)startTime) {
                if (((int)startTime*4)%4 == 0) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)startTime*4)%4 == 1) {
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)startTime*4)%4 == 2) {
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)startTime*4)%4 == 3) {
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
            }
            else if (i == (int)stopTime) {
                if (((int)stopTime*4)%4 == 0) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)stopTime*4)%4 == 1) {
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)stopTime*4)%4 == 2) {
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)stopTime*4)%4 == 3) {
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
            }
            else {
                week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                week.get(currentDay).get(i).new_task_1 = eventName;
                week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                week.get(currentDay).get(i).new_task_2 = eventName;
                week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                week.get(currentDay).get(i).new_task_3 = eventName;
                week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                week.get(currentDay).get(i).new_task_4 = eventName;
            }
        }
        adapter.updateWeekAgenda();
    }

    public float stringTimeToFloat(String time){
        boolean ok;
        boolean entier = true;
        String hour = "0";
        String min = "0";
        float hourt;
        float mint;
        float fTime = -1;

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
            hourt = Float.parseFloat(hour);
            mint = Float.parseFloat(min);
            ok = true;
            if (hourt > 23 || hourt < 0){
                ok = false;
            }
            if (mint >= 60 || mint < 0){
                ok = false;
            }
            else {
                hourt = hourt + roundFifty(mint/60);
                while (hourt>=24){
                    hourt -=24;
                }
            }
            if(ok){
                fTime = hourt;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ok=false;
        }
        if (!ok){
            fTime = -1;
        }
        return fTime;
    }

    private float roundFifty(float fmin){
        float min = 0;
        final float zero = 0;
        final float one = 1;
        final float two = 2;
        final float three = 3;
        final float four = 4;
        final float five = 5;
        final float seven = 7;
        final float height = 8;

        if (fmin >= zero && fmin < one/height){
            min = 0;
        }
        else if (fmin >= one/height && fmin < three/height){
            min = one/four;
        }
        else if (fmin >= three/height && fmin < five/height){
            min = one/two;
        }
        else if (fmin >= five/height && fmin < seven/height){
            min = three/four;
        }
        else if (fmin >= seven/height && fmin < 1){
            min = 1;
        }

        return min;
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