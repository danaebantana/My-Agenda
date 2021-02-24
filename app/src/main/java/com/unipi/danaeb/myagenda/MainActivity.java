package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference ref;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Events");

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String curDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                Intent intent = new Intent(getApplicationContext(), DayActivity.class);
                intent.putExtra("Date", curDate);
                startActivity(intent);
            }
        });
        navigationBar();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot zoneSnapshot : snapshot.getChildren()){
                    String e_date = zoneSnapshot.child("Start date").getValue().toString();
                    String e_time = zoneSnapshot.child("Start time").getValue().toString();
                    try {
                        start(e_date, e_time);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void navigationBar(){
        //Navigation Bar
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.home:
                        intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.profile:
                        intent = new Intent(getApplicationContext(),ProfileActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.contacts:
                        intent = new Intent(getApplicationContext(),ContactActivity.class);
                        intent.putExtra("id","Contacts");
                        startActivity(intent);
                        break;
                    case R.id.logOut:
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), R.string.toast_Logout, Toast.LENGTH_LONG).show();
                        intent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // SMS notification
    public void start(String e_date, String e_time) throws ParseException {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Date date = (Date) format.parse(e_date);
        String[] timeArray = e_time.split(":");
        int HH = Integer.parseInt(timeArray[0]);
        int mm = Integer.parseInt(timeArray[1]);
        Date dat = new Date();
        Calendar cal_alarm = Calendar.getInstance();
        Calendar cal_now = Calendar.getInstance();
        cal_now.setTime(dat);
        cal_alarm.setTime(date);
        cal_alarm.set(Calendar.HOUR_OF_DAY, HH);
        cal_alarm.set(Calendar.MINUTE, mm);
        cal_alarm.set(Calendar.SECOND,0);
        if(cal_alarm.before(cal_now)){
            cal_alarm.add(Calendar.DATE,1);
        }

        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        manager.set(AlarmManager.RTC_WAKEUP,cal_alarm.getTimeInMillis(), pendingIntent);
    }
}