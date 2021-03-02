package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseDatabase database;
    DatabaseReference ref, usersRef;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private ImageView recognize;
    private static final int REC_RESULT = 653;
    private CalendarView calendarView;
    private TTS MyTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Events");
        usersRef = database.getReference("Users");
        recognize = findViewById(R.id.imageView_recognize);

        calendarView = (CalendarView) findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                //check if user has given his profile details.
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {  //foreach user
                            if(dataSnapshot.getKey().equals(currentUser.getUid())){
                                if(dataSnapshot.child("Name").exists()){
                                    String curDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                    Intent intent = new Intent(getApplicationContext(), DayActivity.class);
                                    intent.putExtra("Date", curDate);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getApplication(),R.string.toast_needProfileData, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        navigationBar();
        speak();
    }

    // Welcome
    public void speak() {
        MyTts = new TTS(MainActivity.this);
        MyTts.speak("Welcome");
    }

    //Function to activate voice recognition.
    public void recognize(View view){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"EL");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please say a number");
        startActivityForResult(intent,REC_RESULT);
    }

    //When Recognize image button is pressed this function is responsible to collect the data and see if it matches the two phrases.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {  //foreach user
                    if(dataSnapshot.getKey().equals(currentUser.getUid())){
                        if(dataSnapshot.child("Name").exists()){    //User has given his profile details.
                            if (requestCode==REC_RESULT && resultCode==RESULT_OK){
                                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);  //recognize only the numbers of the messages.
                                int numOfDays = 0;
                                long date = calendarView.getDate();
                                String m = (String) android.text.format.DateFormat.format("MM", date);  //Get month
                                String y = (String) android.text.format.DateFormat.format("yyyy", date);  //Get year
                                int year = Integer.parseInt(y);
                                int month = 0;
                                if(m.equals("02")){  //days=28
                                    if (year%4!=0 || year%400!=0){
                                        numOfDays = 28;
                                    } else {
                                        numOfDays = 29;
                                    }
                                    month = 2;
                                } else if(m.equals("01") || m.equals("03") || m.equals("05") || m.equals("07") || m.equals("08")){  //days=31
                                    numOfDays = 31;
                                    month = Integer.parseInt(m.substring(1));
                                } else if (m.equals("10") || m.equals("12")){
                                    numOfDays = 31;
                                    month = Integer.parseInt(m);
                                }
                                else if(m.equals("04") || m.equals("06") || m.equals("09")) { //days=30
                                    numOfDays = 30;
                                    month = Integer.parseInt(m.substring(1));
                                } else if(m.equals("11")){
                                    numOfDays = 30;
                                    month = Integer.parseInt(m);
                                }
                                for(int i = 1; i <= numOfDays; i++){
                                    if(matches.contains(String.valueOf(i))){
                                        String curDate = i + "/" + month + "/" + year;
                                        Intent intent = new Intent(getApplicationContext(), DayActivity.class);
                                        intent.putExtra("Date", curDate);
                                        startActivity(intent);
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getApplication(),R.string.toast_needProfileData, Toast.LENGTH_LONG).show();
                        }
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

    @Override
    public void onInit(int status) {

    }
}