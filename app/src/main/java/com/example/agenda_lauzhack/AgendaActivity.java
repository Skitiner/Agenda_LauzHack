package com.example.agenda_lauzhack;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AgendaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        //myAdapter adapter = new myAdapter(this, R.layout);

        timeSlot slot1 = new timeSlot();
        slot1.time = 1;
        slot1.task_1 = timeSlot.currentTask.EAT;
        slot1.task_2 = timeSlot.currentTask.EAT;
        slot1.task_3 = timeSlot.currentTask.FREE;
        slot1.task_4 = timeSlot.currentTask.SPORT;

    }

    private class myAdapter extends ArrayAdapter<timeSlot> {

        private int time_layout;

        public myAdapter(@NonNull Context context, int resource) {
            super(context, resource);
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
                    ((TextView) row.findViewById(R.id.a_1)).setBackground(Drawable.createFromPath("@color/darkBlue"));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_1)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_1)).setBackground(Drawable.createFromPath("@color/lightBlue"));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_1)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_1)).setBackground(Drawable.createFromPath("@color/orange"));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_1)).setText("-");
                    ((TextView) row.findViewById(R.id.a_1)).setBackground(Drawable.createFromPath("@color/gray"));
                    break;

            }

            switch (getItem(position).task_2) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_2)).setBackground(Drawable.createFromPath("@color/darkBlue"));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_2)).setBackground(Drawable.createFromPath("@color/lightBlue"));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_2)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_2)).setBackground(Drawable.createFromPath("@color/orange"));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_2)).setText("-");
                    ((TextView) row.findViewById(R.id.a_2)).setBackground(Drawable.createFromPath("@color/gray"));
                    break;

            }

            switch (getItem(position).task_3) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_3)).setBackground(Drawable.createFromPath("@color/darkBlue"));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_3)).setBackground(Drawable.createFromPath("@color/lightBlue"));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_3)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_3)).setBackground(Drawable.createFromPath("@color/orange"));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_3)).setText("-");
                    ((TextView) row.findViewById(R.id.a_3)).setBackground(Drawable.createFromPath("@color/gray"));
                    break;

            }

            switch (getItem(position).task_4) {
                case SPORT:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Sport");
                    ((TextView) row.findViewById(R.id.a_4)).setBackground(Drawable.createFromPath("@color/darkBlue"));
                    break;

                case WORK:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Work");
                    ((TextView) row.findViewById(R.id.a_4)).setBackground(Drawable.createFromPath("@color/lightBlue"));
                    break;

                case EAT:
                    ((TextView) row.findViewById(R.id.a_4)).setText("Eat");
                    ((TextView) row.findViewById(R.id.a_4)).setBackground(Drawable.createFromPath("@color/orange"));
                    break;

                case FREE:
                    ((TextView) row.findViewById(R.id.a_4)).setText("-");
                    ((TextView) row.findViewById(R.id.a_4)).setBackground(Drawable.createFromPath("@color/gray"));
                    break;

            }
            return row;
        }


    }
}

