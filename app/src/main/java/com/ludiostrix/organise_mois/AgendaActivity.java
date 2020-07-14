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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    private static List<List<Boolean>> cancel_day_taks;


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

    private boolean delNewEvent = false;

    private final List<Integer> colorIds = new ArrayList() {
        {
            add(R.id.pink); add(R.id.violet); add(R.id.lightDarkBlue); add(R.id.blue); add(R.id.turquoise);
            add(R.id.lemonGreen); add(R.id.lightGreen);
        }
    };

    private final List<Integer> color = new ArrayList() {
        {
            add(R.color.pink); add(R.color.violet); add(R.color.lightDarkBlue); add(R.color.blue); add(R.color.turquoise);
            add(R.color.lemonGreen); add(R.color.lightGreen);
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

        /*userProfile.convertInPastDay();

        saveToFile();
        readFromFile(); // pas très opti, mais permet de généré aussi le pastAgenda et newEventPastAgenda

        MainActivity.setAlarmOfTheDay(AgendaActivity.this);*/

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
        View addEvent = findViewById(R.id.addEvent);
        View delEvent = findViewById(R.id.delEvent);
        if(!fixed_work && !lunch_time) {
            view.setVisibility(View.GONE);
            addEvent.setVisibility(View.VISIBLE);
            delEvent.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.VISIBLE);
            addEvent.setVisibility(View.GONE);
            delEvent.setVisibility(View.GONE);
        }

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
        //Intent intent = new Intent(AgendaActivity.this, MainActivity.class);
        //startActivity(intent);
        finish();
    }

    // Fonction pour adapter le dailyTask au jour actuel
    public void setWeekSlots() {

        int offset_indice = convertedIndice();

        dailyTasks = new ArrayList<>(userProfile.agenda);
        cancel_day_taks = new ArrayList<>(userProfile.canceled_slots);
        week = new ArrayList<>(userProfile.fullAgenda);

        for (int i = 0; i < 7; i++) {
            int indice = (i + offset_indice)%7;
            ArrayList<timeSlot.currentTask> day = new ArrayList<>(userProfile.agenda.get(indice));
            ArrayList<Boolean> cancel = new ArrayList<>(userProfile.canceled_slots.get(indice));
            ArrayList<timeSlot> we = new ArrayList<>(userProfile.fullAgenda.get(indice));
            for (int j = 0; j < 96; j++){
                day.set(j, userProfile.agenda.get(indice).get(j));
                cancel.set(j, userProfile.canceled_slots.get(indice).get(j));
            }
            for (int j = 0; j < 24; j++){
                we.set(j, userProfile.fullAgenda.get(indice).get(j));
            }
            dailyTasks.set(i, day);
            cancel_day_taks.set(i, cancel);
            week.set(i, we);
        }

        /*for (int i = 0; i < 7; i++) {
            int indice = (i + offset_indice)%7;
            dailyTasks.set(i, userProfile.agenda.get(indice));
            cancel_day_taks.set(i, userProfile.canceled_slots.get(indice));
            week.set(i,userProfile.fullAgenda.get(indice));
        }*/
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
            userProfile.fullAgenda = week;

            userProfile.settingDay = Calendar.getInstance();
            userProfile.settingDay.setTimeInMillis(System.currentTimeMillis());

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
                if(dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.WORK_FIX && fixed_work) {
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));
                    userProfile.futurAgenda.get(i).set(j, timeSlot.currentTask.WORK_FIX);
                }

                if(dailyTasks.get(i).get(j) == timeSlot.currentTask.WORK_FIX && fixed_work && dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.FREE) {
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));
                    userProfile.futurAgenda.get(i).set(j, timeSlot.currentTask.FREE);
                }

                if(dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.EAT && lunch_time) {
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));
                    userProfile.futurAgenda.get(i).set(j, timeSlot.currentTask.EAT);
                }

                if(dailyTasks.get(i).get(j) == timeSlot.currentTask.EAT && lunch_time && dailyTasks.get(currentDay).get(j) == timeSlot.currentTask.FREE) {
                    dailyTasks.get(i).set(j, dailyTasks.get(currentDay).get(j));
                    userProfile.futurAgenda.get(i).set(j, timeSlot.currentTask.FREE);
                }
            }
            userProfile.updateFuturAgenda(i);
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
        if (!lunch_time && !fixed_work) {
            switch (view.getId()) {
                case R.id.date:
                    currentDay = 0;
                    date_offset = 0;
                    View addEvent = findViewById(R.id.addEvent);
                    View delEvent = findViewById(R.id.delEvent);

                    addEvent.setVisibility(View.VISIBLE);
                    delEvent.setVisibility(View.VISIBLE);

                    adapter.updateAgenda();

                    SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
                    date = findViewById(R.id.date);
                    currentDate = new Date();
                    date.setText(formatter.format(currentDate));

                    /*// Start date
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

                    date.setText(dt);*/
                    break;

            }
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

        //setWeekSlots();

        //saveToFile();


        View addEvent = findViewById(R.id.addEvent);
        View delEvent = findViewById(R.id.delEvent);

        if (date_offset >= 7) {
            addEvent.setVisibility(View.GONE);
            delEvent.setVisibility(View.VISIBLE);

            adapter.addAll(userProfile.futurFullAgenda.get((currentDay + convertedIndice()) % 7));
        }
        else if (date_offset < 0){
            int past = Math.abs(date_offset + 1);
            if (userProfile.pastFullAgenda != null) {
                if (past < userProfile.pastFullAgenda.size()) {
                    adapter.addAll(userProfile.pastFullAgenda.get(userProfile.pastFullAgenda.size() - 1 - past));
                    addEvent.setVisibility(View.VISIBLE);
                    delEvent.setVisibility(View.VISIBLE);
                }
                else {
                    adapter.addAll(userProfile.freeWeekDay);
                    addEvent.setVisibility(View.GONE);
                    delEvent.setVisibility(View.GONE);
                }
            }
            else {
                adapter.addAll(userProfile.freeWeekDay);
            }
        }
        else {
            if(!lunch_time && !fixed_work) {
                addEvent.setVisibility(View.VISIBLE);
                delEvent.setVisibility(View.VISIBLE);
            }
            Log.w("CURRENT DAY", " " + currentDay);
            adapter.addAll(week.get(currentDay));
        }

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
                    if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                        if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.orange_canceled, null));
                    }
                    else {
                        if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.orange_canceled, null));
                    }

                    break;

                case WORK:
                    textView.setText(R.string.work);
                    textView.setBackgroundColor(getResources().getColor(R.color.salmonpink, null));
                    if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                        if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.salmonpink_canceled, null));
                    }
                    else {
                        if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.salmonpink_canceled, null));
                    }
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
                    if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                        if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.gray_canceled, null));
                    }
                    else {
                        if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.gray_canceled, null));
                    }
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
                    //textView.setText(R.string.pause);
                    textView.setText("-");
                    textView.setBackgroundColor(getResources().getColor(R.color.green, null));
                    break;

                case WORK_CATCH_UP:
                    textView.setText(R.string.workCatchUp);
                    textView.setBackgroundColor(getResources().getColor(R.color.salmonpink, null));
                    if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                        if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.salmonpink_canceled, null));
                    }
                    else {
                        if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.salmonpink_canceled, null));
                    }
                    break;

                case SPORT_CATCH_UP:
                    textView.setText(R.string.sportCatchUp);
                    textView.setBackgroundColor(getResources().getColor(R.color.orange, null));
                    if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                        if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.orange_canceled, null));
                    }
                    else {
                        if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                            textView.setBackgroundColor(getResources().getColor(R.color.orange_canceled, null));
                    }

                    break;


                case NEWEVENT:
                    boolean eventExist = false;
                    for (int i = 0; i < userProfile.savedEvent.size(); i++){
                        if (userProfile.savedEvent.get(i).name.equals(new_task)) {
                            textView.setText(userProfile.savedEvent.get(i).name);
                            textView.setBackgroundColor(getResources().getColor(userProfile.savedEvent.get(i).color, null));
                            eventExist = true;
                            break;
                        }
                    }
                    if (!eventExist){
                        textView.setText(new_task);
                        textView.setBackgroundColor(getResources().getColor(R.color.lightOrange, null));
                    }
                    break;

            }

            if (date_offset < 0 && Math.abs(date_offset + 1) < userProfile.pastFullAgenda.size()){
                if(userProfile.pastCanceledSlots.get(userProfile.pastFullAgenda.size() - 1 - (Math.abs(date_offset + 1))).get(slot_indice) == Boolean.TRUE)
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else if (date_offset < 7){
                if (cancel_day_taks.get(currentDay).get(slot_indice) == Boolean.TRUE)
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
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

        public void updateAgenda() {
            int position;
            for(int i = 0; i < 96; i +=4 ) {
                position = i/4;
                week.get(currentDay).get(position).task_1 =  dailyTasks.get(currentDay).get(i);
                week.get(currentDay).get(position).task_2 =  dailyTasks.get(currentDay).get(i+1);
                week.get(currentDay).get(position).task_3 =  dailyTasks.get(currentDay).get(i+2);
                week.get(currentDay).get(position).task_4 =  dailyTasks.get(currentDay).get(i+3);
                week.get(currentDay).get(position).new_task_1 =  userProfile.newEventAgenda.get((currentDay + convertedIndice())%7).get(i);
                week.get(currentDay).get(position).new_task_2 =  userProfile.newEventAgenda.get((currentDay + convertedIndice())%7).get(i+1);
                week.get(currentDay).get(position).new_task_3 =  userProfile.newEventAgenda.get((currentDay + convertedIndice())%7).get(i+2);
                week.get(currentDay).get(position).new_task_4 =  userProfile.newEventAgenda.get((currentDay + convertedIndice())%7).get(i+3);
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
                    week.get(j).get(position).new_task_1 =  userProfile.newEventAgenda.get((j + convertedIndice())%7).get(i);
                    week.get(j).get(position).new_task_2 =  userProfile.newEventAgenda.get((j + convertedIndice())%7).get(i+1);
                    week.get(j).get(position).new_task_3 =  userProfile.newEventAgenda.get((j + convertedIndice())%7).get(i+2);
                    week.get(j).get(position).new_task_4 =  userProfile.newEventAgenda.get((j + convertedIndice())%7).get(i+3);
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
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos, timeSlot.currentTask.WORK_FIX);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.WORK_FIX);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.WORK_FIX);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.WORK_FIX);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);

                        }

                    } else {
                        if ( dailyTasks.get(currentDay).get(daily_task_pos-1) != timeSlot.currentTask.WORK_FIX) {
                            dailyTasks.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.WORK_FIX);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.WORK_FIX);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                        }
                    }
                    updateAgenda();
                    userProfile.updateFuturAgenda((currentDay + convertedIndice()) % 7);
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

                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos, timeSlot.currentTask.EAT);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.EAT);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.EAT);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.EAT);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            dailyTasks.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);

                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+1, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+2, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos+3, timeSlot.currentTask.FREE);
                        }

                    } else {
                        if ( dailyTasks.get(currentDay).get(daily_task_pos-1) != timeSlot.currentTask.EAT) {
                            dailyTasks.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.EAT);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos-1, timeSlot.currentTask.EAT);
                        }
                        else {
                            dailyTasks.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                            userProfile.futurAgenda.get(currentDay).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                        }
                    }

                    updateAgenda();
                    userProfile.updateFuturAgenda((currentDay + convertedIndice()) % 7);

                }
                if (delNewEvent) {
                    if (date_offset < 7 && date_offset >= 0) {
                        if (!(v.getId() == R.id.t_1)) {
                            if (userProfile.agenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos - 1) == timeSlot.currentTask.NEWEVENT) {
                                for (newEvent event : userProfile.savedEvent) {
                                    if (event.name.equals(userProfile.newEventAgenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos - 1))) {
                                        if (event.sport) {
                                            userProfile.lateSportSlot++;
                                        } else if (event.work) {
                                            userProfile.lateWorkSlot++;
                                        }
                                    }
                                }
                                userProfile.agenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                                userProfile.newEventAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos - 1, "");
                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                if (userProfile.agenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos + i) == timeSlot.currentTask.NEWEVENT) {
                                    for (newEvent event : userProfile.savedEvent) {
                                        if (event.name.equals(userProfile.newEventAgenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos + i))) {
                                            if (event.sport) {
                                                userProfile.lateSportSlot++;
                                            } else if (event.work) {
                                                userProfile.lateWorkSlot++;
                                            }
                                        }
                                    }
                                    userProfile.agenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos + i, timeSlot.currentTask.FREE);
                                    userProfile.newEventAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos + i, "");
                                }
                            }
                        }
                        userProfile.updateFullAgenda((currentDay + convertedIndice()) % 7);
                        setWeekSlots();
                        adapter.clear();
                        adapter.addAll(week.get(currentDay));
                    }
                    else if (date_offset >= 7){
                        if (!(v.getId() == R.id.t_1)) {
                            if (userProfile.futurAgenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos - 1) == timeSlot.currentTask.NEWEVENT) {
                                userProfile.futurAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                                userProfile.newEventFuturAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos - 1, "");
                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                if (userProfile.futurAgenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos + i) == timeSlot.currentTask.NEWEVENT) {
                                    userProfile.futurAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos + i, timeSlot.currentTask.FREE);
                                    userProfile.newEventFuturAgenda.get((currentDay + convertedIndice()) % 7).set(daily_task_pos + i, "");
                                }
                            }
                        }
                        userProfile.updateFuturAgenda((currentDay + convertedIndice()) % 7);
                        adapter.clear();
                        adapter.addAll(userProfile.futurFullAgenda.get((currentDay + convertedIndice()) % 7));
                    }
                    else if (date_offset < 0) {
                        int past = Math.abs(date_offset + 1);

                        if (!(v.getId() == R.id.t_1)) {
                            if (userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(daily_task_pos - 1) == timeSlot.currentTask.NEWEVENT) {
                                for (newEvent event : userProfile.savedEvent) {
                                    if (event.name.equals(userProfile.newEventAgenda.get((currentDay + convertedIndice()) % 7).get(daily_task_pos - 1))) {
                                        if (event.sport) {
                                            userProfile.lateSportSlot++;
                                        } else if (event.work) {
                                            userProfile.lateWorkSlot++;
                                        }
                                    }
                                }
                                userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).set(daily_task_pos - 1, timeSlot.currentTask.FREE);
                                userProfile.newEventPastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).set(daily_task_pos - 1, "");
                            }
                        } else {
                            for (int i = 0; i < 4; i++) {
                                if (userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(daily_task_pos + i) == timeSlot.currentTask.NEWEVENT) {
                                    for (newEvent event : userProfile.savedEvent) {
                                        if (event.name.equals(userProfile.newEventPastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(daily_task_pos + i))) {
                                            if (event.sport) {
                                                userProfile.lateSportSlot++;
                                            } else if (event.work) {
                                                userProfile.lateWorkSlot++;
                                            }
                                        }
                                    }
                                    userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).set(daily_task_pos + i, timeSlot.currentTask.FREE);
                                    userProfile.newEventPastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).set(daily_task_pos + i, "");
                                }
                            }
                        }
                        userProfile.updatePastAgenda(userProfile.pastFullAgenda.size() - 1 - past);
                        adapter.clear();
                        adapter.addAll(userProfile.pastFullAgenda.get(userProfile.pastFullAgenda.size() - 1 - past));
                    }
                }
            }
        }
    }
    public void addEventXmlCallback(View view){
        eventCreation(view);
    }

    public void delEventXmlCallback(View view){
        delNewEvent = true;
        View add = findViewById(R.id.addEvent);
        add.setVisibility(View.GONE);
        View del = findViewById(R.id.delEvent);
        del.setVisibility(View.GONE);
        View delFinished = findViewById(R.id.delFinishedEvent);
        delFinished.setVisibility(View.VISIBLE);
        View previousDay = findViewById(R.id.previousDay);
        previousDay.setClickable(false);
        View nextday = findViewById(R.id.previousDay);
        nextday.setClickable(false);
    }

    public void delFinishedEventXmlCallback(View view){
        delNewEvent = false;
        View add = findViewById(R.id.addEvent);
        add.setVisibility(View.VISIBLE);
        View del = findViewById(R.id.delEvent);
        del.setVisibility(View.VISIBLE);
        View delFinished = findViewById(R.id.delFinishedEvent);
        delFinished.setVisibility(View.GONE);
        saveToFile();
        if (date_offset < 7) {
            plan();
        }
        else{
            adapter.clear();
            adapter.addAll(userProfile.futurFullAgenda.get((currentDay +convertedIndice()) % 7));
        }
        MainActivity.setAlarmOfTheDay(AgendaActivity.this);
        View previousDay = findViewById(R.id.previousDay);
        previousDay.setClickable(true);
        View nextday = findViewById(R.id.previousDay);
        nextday.setClickable(true);
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


        LinearLayout weekly = popupView.findViewById(R.id.layoutWeeklyEvent);
        if (date_offset < 0){
            weekly.setVisibility(View.GONE);
        }
        else {
            weekly.setVisibility(View.VISIBLE);
        }

        colorButtonListener(popupView);

        setAutoComplete(popupView);

        confirmButtonListener(popupView, popupWindow);

    }

    public void setAutoComplete(final View popupView){
        List<String> eventName = new ArrayList();
        final String[] EVENT_NAME = new String[userProfile.savedEvent.size()];

        for (int i = 0; i < userProfile.savedEvent.size(); i++){
            eventName.add(userProfile.savedEvent.get(i).name);
        }

        Collections.sort(eventName);

        for (int i = 0; i < userProfile.savedEvent.size(); i++){
            EVENT_NAME[i] = eventName.get(i);
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
                            if (color.get(i).equals(userProfile.savedEvent.get(event).color)){
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

                        if (userProfile.savedEvent.get(event).work){
                            RadioButton workRB = popupView.findViewById(R.id.workRadioButton);
                            workRB.setChecked(true);
                        }
                        else if (userProfile.savedEvent.get(event).sport){
                            RadioButton sportRB = popupView.findViewById(R.id.sportRadioButton);
                            sportRB.setChecked(true);
                        }
                        else {
                            RadioButton freeRB = popupView.findViewById(R.id.freeRadioButton);
                            freeRB.setChecked(true);
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

                        if (startTime >= stopTime){
                            Toast.makeText(AgendaActivity.this, R.string.wrongEventStartStop, Toast.LENGTH_SHORT).show();
                        }
                        else if (startTime != -1 && stopTime != -1) {

                            if (event < 0) {
                                newEvent nE = new newEvent();
                                nE.name = autotextView.getText().toString();
                                nE.color = color.get(eventColor);
                                RadioButton workRB = popupView.findViewById(R.id.workRadioButton);
                                RadioButton sportRB = popupView.findViewById(R.id.sportRadioButton);
                                nE.work = workRB.isChecked();
                                nE.sport = sportRB.isChecked();
                                userProfile.savedEvent.add(nE);
                                if (userProfile.savedEvent.size() > 20) {
                                    userProfile.savedEvent.remove(0);
                                }
                                event = userProfile.savedEvent.size()-1;
                            } else {
                                newEvent nE = new newEvent();
                                nE.name = userProfile.savedEvent.get(event).name;
                                nE.color = color.get(eventColor);
                                RadioButton workRB = popupView.findViewById(R.id.workRadioButton);
                                RadioButton sportRB = popupView.findViewById(R.id.sportRadioButton);
                                nE.work = workRB.isChecked();
                                nE.sport = sportRB.isChecked();
                                userProfile.savedEvent.remove(event);
                                userProfile.savedEvent.add(nE);
                                event = userProfile.savedEvent.size()-1;
                            }
                            popupWindow.dismiss();
                            Switch eventWeekly = popupView.findViewById(R.id.eventWeekly);
                            setNewEvent(startTime, stopTime, userProfile.savedEvent.get(event), eventWeekly.isChecked());

                            int past = Math.abs(date_offset + 1);

                            if (userProfile.pastFullAgenda.size() - 1 - past >= 0 && date_offset < 0) {
                                userProfile.updatePastAgenda(userProfile.pastFullAgenda.size() - 1 - past);
                            }
                            int day = (currentDay + convertedIndice()) % 7;
                            userProfile.updateFullAgenda(day);
                            userProfile.updateFuturAgenda(day);
                            saveToFile();
                            if (date_offset < 7){
                                plan();                   // si jour actuelle, ne pas update ce qui est déja passé
                                MainActivity.setAlarmOfTheDay(AgendaActivity.this);
                            }
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

    public void plan(){
        int dayOffset = 0;
        int actualDay;
        if (date_offset < 0) {
            actualDay = 0;
        }
        else
            actualDay = currentDay;
        do {
            int nbWorkDay = 0;
            for (int j = 0; j < userProfile.freeDay.length; j++) {
                if (!userProfile.freeDay[j]) {
                    nbWorkDay++;
                }
            }
            int[] freedaylist = new int[nbWorkDay];
            int n = 0;
            for (int j = 0; j < userProfile.freeDay.length; j++) {
                if (!userProfile.freeDay[j]) {
                    freedaylist[n] = j;
                    n++;
                }
            }

            boolean freeday = true;
            boolean nextfreeday = true;
            for (int k = 0; k < freedaylist.length; k++) {
                if (freedaylist[k] == (actualDay + conversionDayIndice() + dayOffset) % 7) {
                    freeday = false;
                }
                if (freedaylist[k] == (actualDay + conversionDayIndice() + 1 + dayOffset) % 7) {
                    nextfreeday = false;
                }
            }

            int val = (actualDay + convertedIndice() + dayOffset)%7; // à tester
            int dayCalcul = val;

            DaySlotsCalculation daySlotsCalculation = new DaySlotsCalculation(userProfile, freeday, nextfreeday, val, false);

            IA Agent = new IA(userProfile.weight, userProfile.canceled_slots.get(val), userProfile.sportDayRank,
                    userProfile.lastConnection, userProfile.settingDay, daySlotsCalculation.daily_slots_generated,
                    userProfile.agenda.get(val), userProfile.newEventAgenda.get(val), dayCalcul, userProfile.savedEvent,
                    freeday, Integer.parseInt(userProfile.optWorkTime), userProfile.lateWorkSlot, userProfile.workCatchUp,
                    userProfile.sportRoutine, userProfile.lateSportSlot, userProfile.sportCatchUp,userProfile.agendaInit, false, false);
            Agent.planDay();

            userProfile.sportDayRank = Agent.rank;
            userProfile.agenda.set(val, Agent.dailyAgenda);
            userProfile.lateSportSlot = Agent.sportSlot;
            userProfile.lateWorkSlot = Agent.workSlot;
            userProfile.workCatchUp = Agent.workSlotCatchUp;
            userProfile.sportCatchUp = Agent.sportCatchUp;

            userProfile.updateFullAgenda(val);
            /*int position;
            for (int j = 0; j < 96; j += 4) {
                position = j / 4;
                userProfile.fullAgenda.get(val).get(position).task_1 = userProfile.agenda.get(val).get(j);
                userProfile.fullAgenda.get(val).get(position).task_2 = userProfile.agenda.get(val).get(j + 1);
                userProfile.fullAgenda.get(val).get(position).task_3 = userProfile.agenda.get(val).get(j + 2);
                userProfile.fullAgenda.get(val).get(position).task_4 = userProfile.agenda.get(val).get(j + 3);
            }*/

            /*do {
                freeday = true;
                for (int k = 0; k < freedaylist.length; k++) {
                    if (freedaylist[k] == (conversionDayIndice() + dayOffset + date_offset) % 7) { // (daySinceLastConnection / 7) * 7) make it >= 0
                        freeday = false;
                    }
                }
                dayOffset++;
            } while (!freeday);*/
            dayOffset++;
        } while ((userProfile.lateWorkSlot > 0 || userProfile.lateWorkSlot < -8 || userProfile.lateSportSlot != 0 ||
                userProfile.workCatchUp > 0 || userProfile.sportCatchUp > 0) && dayOffset < 7);
        saveToFile();

        int isOK = 0;               //pas tres utile...
        if (isOK == 0) {
            readFromFile();
            setWeekSlots();
            if (date_offset >= 0) {
                adapter.clear();
                adapter.addAll(week.get(currentDay));
            }
            Toast.makeText(AgendaActivity.this, R.string.Saved, Toast.LENGTH_SHORT).show();
        }
        else if (isOK == 1){
            Toast.makeText(AgendaActivity.this, R.string.bad_parameters, Toast.LENGTH_SHORT).show();
        }
        else if (isOK == 2){
            Toast.makeText(AgendaActivity.this, R.string.too_lots_of_fixedwork, Toast.LENGTH_SHORT).show();
        }
    }

    public void setNewEvent(float startTime, float stopTime, newEvent newEvent, boolean eventWeekly) {
        if(date_offset >= 0) {
            int day = (currentDay + convertedIndice()) % 7;
            for (int i = (int) (4 * startTime); i < (int) (4 * stopTime); i++) {
                if (userProfile.agenda.get(day).get(i) == timeSlot.currentTask.NEWEVENT) {
                    for (newEvent event : userProfile.savedEvent) {
                        if (event.name.equals(userProfile.newEventAgenda.get(day).get(i))) {
                            if (event.sport && !newEvent.sport) {
                                userProfile.lateSportSlot++;
                            } else if (event.work && !newEvent.work) {
                                userProfile.lateWorkSlot++;
                            }
                        }
                    }
                } else if (userProfile.agenda.get(day).get(i) == timeSlot.currentTask.WORK ||
                        userProfile.agenda.get(day).get(i) == timeSlot.currentTask.WORK_FIX && !newEvent.work) {
                    userProfile.lateWorkSlot++;
                } else if (userProfile.agenda.get(day).get(i) == timeSlot.currentTask.SPORT && !newEvent.sport) {
                    userProfile.lateSportSlot++;
                }
                /*else if (userProfile.agenda.get(day).get(i) == timeSlot.currentTask.WORK_CATCH_UP && !newEvent.work) {
                    userProfile.workCatchUp++;
                } else if (userProfile.agenda.get(day).get(i) == timeSlot.currentTask.SPORT_CATCH_UP && !newEvent.sport) {
                    userProfile.sportCatchUp++;
                }*/

                userProfile.agenda.get(day).set(i, timeSlot.currentTask.NEWEVENT);
                userProfile.newEventAgenda.get(day).set(i, newEvent.name);
                if (newEvent.sport) {
                    userProfile.lateSportSlot--;
                } else if (newEvent.work) {
                    userProfile.lateWorkSlot--;
                }

                if (eventWeekly) {
                    userProfile.futurAgenda.get(day).set(i, timeSlot.currentTask.NEWEVENT);
                    userProfile.newEventFuturAgenda.get(day).set(i, newEvent.name);
                }

            /*if ((int)startTime == (int) stopTime){
                if (((int)(startTime*4))%4 == 0) {
                    if (((int)(stopTime*4))%4 == 1) {
                        week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_1 = eventName;
                    }
                    else if (((int)(stopTime*4))%4 == 2) {
                        week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_1 = eventName;
                        week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_2 = eventName;
                    }
                    else if (((int)(stopTime*4))%4 == 3) {
                        week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_1 = eventName;
                        week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_2 = eventName;
                        week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_3 = eventName;
                    }
                }
                else if (((int)(startTime*4))%4 == 1) {
                    if (((int)(stopTime*4))%4 == 2) {
                        week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_2 = eventName;
                    }
                    else if (((int)(stopTime*4))%4 == 3) {
                        week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_2 = eventName;
                        week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_3 = eventName;
                    }
                }
                else if (((int)(startTime*4))%4 == 2) {
                    if (((int)(stopTime*4))%4 == 3) {
                        week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                        week.get(currentDay).get(i).new_task_3 = eventName;
                    }
                }
            }
            else if (i == (int)startTime) {
                if (((int)(startTime*4))%4 == 0) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)(startTime*4))%4 == 1) {
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)(startTime*4))%4 == 2) {
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
                if (((int)(startTime*4))%4 == 3) {
                    week.get(currentDay).get(i).task_4 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_4 = eventName;
                }
            }
            else if (i == (int)stopTime) {
                if (((int)(stopTime*4))%4 == 1) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                }
                if (((int)(stopTime*4))%4 == 2) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                }
                if (((int)(stopTime*4))%4 == 3) {
                    week.get(currentDay).get(i).task_1 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_1 = eventName;
                    week.get(currentDay).get(i).task_2 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_2 = eventName;
                    week.get(currentDay).get(i).task_3 = timeSlot.currentTask.NEWEVENT;
                    week.get(currentDay).get(i).new_task_3 = eventName;
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
            }*/
            }
            adapter.updateAgenda();
            //adapter.updateWeekAgenda();
        }
        else {
            int past = Math.abs(date_offset + 1);
            for (int i = (int) (4 * startTime); i < (int) (4 * stopTime); i++) {
                if (userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(i) == timeSlot.currentTask.NEWEVENT) {
                    for (newEvent event : userProfile.savedEvent) {
                        if (event.name.equals(userProfile.newEventPastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(i))) {
                            if (event.sport && !newEvent.sport) {
                                //userProfile.lateSportSlot++;
                                userProfile.sportCatchUp++;
                            } else if (event.work && !newEvent.work) {
                                //userProfile.lateWorkSlot++;
                                userProfile.workCatchUp++;
                            }
                        }
                    }
                } else if (userProfile.pastAgenda.get(userProfile.pastFullAgenda.size() - 1 - past).get(i) == timeSlot.currentTask.WORK ||
                        userProfile.pastAgenda.get((userProfile.pastFullAgenda.size() - 1 - past)).get(i) == timeSlot.currentTask.WORK_FIX && !newEvent.work) {
                    //userProfile.lateWorkSlot++;
                    userProfile.workCatchUp++;
                } else if (userProfile.pastAgenda.get((userProfile.pastFullAgenda.size() - 1 - past)).get(i) == timeSlot.currentTask.SPORT && !newEvent.sport) {
                    //userProfile.lateSportSlot++;
                    userProfile.sportCatchUp++;
                }

                userProfile.pastAgenda.get((userProfile.pastFullAgenda.size() - 1 - past)).set(i, timeSlot.currentTask.NEWEVENT);
                userProfile.newEventPastAgenda.get((userProfile.pastFullAgenda.size() - 1 - past)).set(i, newEvent.name);
                if (newEvent.sport) {
                    userProfile.lateSportSlot--;
                } else if (newEvent.work) {
                    userProfile.lateWorkSlot--;
                }
            }

            adapter.clear();
            adapter.addAll(userProfile.pastFullAgenda.get(userProfile.pastFullAgenda.size() - 1 - past));
        }
    }

    public float stringTimeToFloat(String time){
        boolean ok;
        boolean entier = true;
        String hour = "0";
        String min = "0";
        float hourt;
        float mint;
        float fTime = -1;
        int nbHChar = 0;

        for (int i = 0; i < time.length(); i++){
            if(time.charAt(i) != ':' && entier && nbHChar < 2) {
                hour += time.charAt(i);
                nbHChar++;
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
            if ((hourt > 23 && (hourt > 24 || mint > 0)) || hourt < 0 ){
                ok = false;
            }
            else if (mint >= 60 || mint < 0){
                ok = false;
            }
            else {
                hourt = hourt + roundFifty(mint/60);
                while (hourt>24){
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
                if (!(week_slots.get(indice).get(j) == timeSlot.currentTask.WORK_FIX || week_slots.get(indice).get(j) == timeSlot.currentTask.EAT || week_slots.get(indice).get(j) == timeSlot.currentTask.NEWEVENT)){
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

        while (offset < 0){
            offset += 7;
        }

        return offset%7;
    }
}