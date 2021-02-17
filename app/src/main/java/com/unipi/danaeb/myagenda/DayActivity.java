package com.unipi.danaeb.myagenda;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DayActivity extends AppCompatActivity {

    FloatingActionButton newEvent_bt, back_bt2;
    TextView date_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        String date = getIntent().getStringExtra("Date"); // Get selected date from main activity
        date_txt = findViewById(R.id.date_txt);
        date_txt.setText(date);

        // Back button
        back_bt2 = findViewById(R.id.back_bt2);
        back_bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DayActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // Add new event button
        newEvent_bt = findViewById(R.id.newEvent_bt);
        newEvent_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewEventActivity.class);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });
    }
}