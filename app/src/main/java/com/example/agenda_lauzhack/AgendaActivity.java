package com.example.agenda_lauzhack;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgendaActivity extends AppCompatActivity  {

    private myAdapter adapter;
    private ListView schedule;
    private ArrayList<ArrayList<timeSlot>> week;
    private int currentDay;
    private Date currentDate;
    private TextView date;
    private int date_offset;

    private boolean editable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new myAdapter(this.getApplicationContext(), R.layout.time_slot);
        setContentView(R.layout.activity_agenda);
        schedule = findViewById(R.id.schedule);

        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
        date = findViewById(R.id.date);
        currentDate = new Date();
        date.setText(formatter.format(currentDate));
        currentDay = 0;
        date_offset = 0;

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

        adapter.addAll(week.get(0));
        schedule.setAdapter(adapter);

        if (editable) {
            schedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final TextView task1 = (TextView) view.findViewById(R.id.a_1);
                    final TextView task2 = (TextView) view.findViewById(R.id.a_2);
                    final TextView task3 = (TextView) view.findViewById(R.id.a_3);
                    final TextView task4 = (TextView) view.findViewById(R.id.a_4);

                    final TextView time = (TextView) view.findViewById(R.id.t_1) ;

                    time.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (week.get(currentDay).get(position).task_1 != timeSlot.currentTask.WORK_FIX ||
                                    week.get(currentDay).get(position).task_2 != timeSlot.currentTask.WORK_FIX ||
                                    week.get(currentDay).get(position).task_3 != timeSlot.currentTask.WORK_FIX ||
                                    week.get(currentDay).get(position).task_4 != timeSlot.currentTask.WORK_FIX) {

                                task1.setText(R.string.fixed_work);
                                task1.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task1.setTextColor(getResources().getColor(R.color.white, null));
                                task2.setText(R.string.fixed_work);
                                task2.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task2.setTextColor(getResources().getColor(R.color.white, null));
                                task3.setText(R.string.fixed_work);
                                task3.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task3.setTextColor(getResources().getColor(R.color.white, null));
                                task4.setText(R.string.fixed_work);
                                task4.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task4.setTextColor(getResources().getColor(R.color.white, null));

                                week.get(currentDay).get(position).task_1 = timeSlot.currentTask.WORK_FIX;
                                week.get(currentDay).get(position).task_2 = timeSlot.currentTask.WORK_FIX;
                                week.get(currentDay).get(position).task_3 = timeSlot.currentTask.WORK_FIX;
                                week.get(currentDay).get(position).task_4 = timeSlot.currentTask.WORK_FIX;
                            }
                            else {
                                task1.setText("-");
                                task1.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task1.setTextColor(getResources().getColor(R.color.darkBlue, null));
                                task2.setText("-");
                                task2.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task2.setTextColor(getResources().getColor(R.color.darkBlue, null));
                                task3.setText("-");
                                task3.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task3.setTextColor(getResources().getColor(R.color.darkBlue, null));
                                task4.setText("-");
                                task4.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task4.setTextColor(getResources().getColor(R.color.darkBlue, null));

                                week.get(currentDay).get(position).task_1 = timeSlot.currentTask.FREE;
                                week.get(currentDay).get(position).task_2 = timeSlot.currentTask.FREE;
                                week.get(currentDay).get(position).task_3 = timeSlot.currentTask.FREE;
                                week.get(currentDay).get(position).task_4 = timeSlot.currentTask.FREE;
                            }
                        }
                    });

                    task1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (week.get(currentDay).get(position).task_1 != timeSlot.currentTask.WORK_FIX) {
                                task1.setText(R.string.fixed_work);
                                task1.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task1.setTextColor(getResources().getColor(R.color.white, null));

                                week.get(currentDay).get(position).task_1 = timeSlot.currentTask.WORK_FIX;
                            }
                            else {
                                task1.setText("-");
                                task1.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task1.setTextColor(getResources().getColor(R.color.darkBlue, null));

                                week.get(currentDay).get(position).task_1 = timeSlot.currentTask.FREE;
                            }
                        }
                    });

                    task2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (week.get(currentDay).get(position).task_2 != timeSlot.currentTask.WORK_FIX) {
                                task2.setText(R.string.fixed_work);
                                task2.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task2.setTextColor(getResources().getColor(R.color.white, null));

                                week.get(currentDay).get(position).task_2 = timeSlot.currentTask.WORK_FIX;
                            }
                            else {
                                task2.setText("-");
                                task2.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task2.setTextColor(getResources().getColor(R.color.darkBlue, null));

                                week.get(currentDay).get(position).task_2 = timeSlot.currentTask.FREE;
                            }
                        }
                    });

                    task3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (week.get(currentDay).get(position).task_3 != timeSlot.currentTask.WORK_FIX) {
                                task3.setText(R.string.fixed_work);
                                task3.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task3.setTextColor(getResources().getColor(R.color.white, null));

                                week.get(currentDay).get(position).task_3 = timeSlot.currentTask.WORK_FIX;
                            }
                            else {
                                task3.setText("-");
                                task3.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task3.setTextColor(getResources().getColor(R.color.darkBlue, null));

                                week.get(currentDay).get(position).task_3 = timeSlot.currentTask.FREE;
                            }
                        }
                    });

                    task4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (week.get(currentDay).get(position).task_4 != timeSlot.currentTask.WORK_FIX) {
                                task4.setText(R.string.fixed_work);
                                task4.setBackgroundColor(getResources().getColor(R.color.red, null));
                                task4.setTextColor(getResources().getColor(R.color.white, null));

                                week.get(currentDay).get(position).task_4 = timeSlot.currentTask.WORK_FIX;
                            }
                            else {
                                task4.setText("-");
                                task4.setBackgroundColor(getResources().getColor(R.color.gray, null));
                                task4.setTextColor(getResources().getColor(R.color.darkBlue, null));

                                week.get(currentDay).get(position).task_4 = timeSlot.currentTask.FREE;
                            }
                        }
                    });

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_agenda, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.today_button:
                currentDay = 0;
                adapter.clear();
                adapter.addAll(week.get(currentDay));
                SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd/MM/yyyy", Locale.getDefault());
                date = findViewById(R.id.date);
                currentDate = new Date();
                date.setText(formatter.format(currentDate));
                break;
        }
        return super.onOptionsItemSelected(item);
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



            ((TextView) row.findViewById(R.id.t_1)).setText(getItem(position).time + "h");

            // public enum currentTask {SPORT, WORK, EAT, FREE}
            switch (getItem(position).task_1) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_1)).setText(R.string.Sport);
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_1)).setText(R.string.work);
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_1)).setText(R.string.eat);
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_1)).setText("-");
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case WORK_FIX:
                    ((TextView) row.findViewById(R.id.a_1)).setText(R.string.fixed_work);
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.red, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.white, null));
                    break;

            }

            switch (getItem(position).task_2) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_2)).setText(R.string.Sport);
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_2)).setText(R.string.work);
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_2)).setText(R.string.eat);
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_2)).setText("-");
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case WORK_FIX:
                    ((TextView) row.findViewById(R.id.a_2)).setText(R.string.fixed_work);
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.red, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.white, null));
                    break;

            }

            switch (getItem(position).task_3) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_3)).setText(R.string.Sport);
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_3)).setText(R.string.work);
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_3)).setText(R.string.eat);
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_3)).setText("-");
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case WORK_FIX:
                    ((TextView) row.findViewById(R.id.a_3)).setText(R.string.fixed_work);
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.red, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.white, null));
                    break;

            }

            switch (getItem(position).task_4) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_4)).setText(R.string.Sport);
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_4)).setText(R.string.work);
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_4)).setText(R.string.eat);
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_4)).setText("-");
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case WORK_FIX:
                    ((TextView) row.findViewById(R.id.a_4)).setText(R.string.fixed_work);
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.red, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.white, null));
                    break;

            }
            return row;
        }


    }
}

