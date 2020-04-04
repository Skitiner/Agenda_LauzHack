package com.example.agenda_lauzhack;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AgendaActivity extends AppCompatActivity  {

    private myAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new myAdapter(this.getApplicationContext(), R.layout.time_slot);
        setContentView(R.layout.activity_agenda);
        ListView schedule = findViewById(R.id.schedule);
        schedule.setAdapter(adapter);

        ArrayList<timeSlot> mySlots = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            timeSlot slot = new timeSlot();
            slot.time = i;
            slot.task_1 = timeSlot.currentTask.SPORT;
            slot.task_2 = timeSlot.currentTask.SPORT;
            slot.task_3 = timeSlot.currentTask.FREE;
            slot.task_4 = timeSlot.currentTask.FREE;

            mySlots.add(slot);
        }

        adapter.addAll(mySlots);
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
                    ((TextView) row.findViewById(R.id.a_1)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_1)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_1)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_1)).setText("-");
                    ((TextView) row.findViewById(R.id.a_1)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_1)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

            }

            switch (getItem(position).task_2) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_2)).setText("-");
                    ((TextView) row.findViewById(R.id.a_2)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_2)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

            }

            switch (getItem(position).task_3) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_3)).setText("-");
                    ((TextView) row.findViewById(R.id.a_3)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_3)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

            }

            switch (getItem(position).task_4) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.darkBlue, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.lightBlue, null));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.lightBlue, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.orange, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_4)).setText("-");
                    ((TextView) row.findViewById(R.id.a_4)).setBackgroundColor(getResources().getColor(R.color.gray, null));
                    ((TextView) row.findViewById(R.id.a_4)).setTextColor(getResources().getColor(R.color.darkBlue, null));
                    break;

            }
            return row;
        }


    }
}

