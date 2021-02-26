package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class DayActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton button_newEvent, button_back;
    private TextView date_txt;
    private ListView listView;
    private ListAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        eventsRef = database.getReference("Events");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        button_back = findViewById(R.id.button_back1);
        button_newEvent = findViewById(R.id.button_newEvent);

        String date = getIntent().getStringExtra("Date"); // Get selected date from main activity or edit activity
        date_txt = findViewById(R.id.textView_date);
        date_txt.setText(date);

        listView = findViewById(R.id.listView_events);
        retrieveData();

        // Back button
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DayActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        // New event button
        button_newEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DayActivity.this, NewEventActivity.class);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });
    }

    // Retrieve data from firebase
    public void retrieveData() {
        String user_uid = currentUser.getUid();
        ArrayList<String> dayEventList = new ArrayList<>();
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){   //foreach event
                    //If dataSnapshot "Creator" is the current user.
                    StringBuilder builder = new StringBuilder();
                    if(dataSnapshot.child("Creator").getValue().equals(user_uid) && dataSnapshot.child("Start date").getValue().equals(date_txt.getText().toString())){
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String time = dataSnapshot.child("Start time").getValue().toString();
                        if(dataSnapshot.child("Collaborators").getValue().equals("-")){
                            builder.append(title + "\n" + time + "\nCollaborators: -");
                        } else {
                            String collab = "";
                            int count = 1;
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){
                                if(count==1){
                                    collab = ds.child("Name").getValue().toString();
                                } else {
                                    collab = ds.child("Name").getValue().toString() + ", " + collab;
                                }
                                count++;
                            }
                            builder.append(title + "\n" + time + "\nCollaborators: " + collab);
                        }
                        dayEventList.add(builder.toString());
                    }
                    //If dataSnapshot "Creator" is not the current user, we search if the current user is a  so we can view the event.
                    else if(dataSnapshot.child("Start date").getValue().equals(date_txt.getText().toString())) {
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String time = dataSnapshot.child("Start time").getValue().toString();
                        if(dataSnapshot.child("Collaborators").getValue().equals("-")){
                            continue;
                        } else {
                            String collab = dataSnapshot.child("Creator").getValue().toString();
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){
                                if(!ds.getKey().equals(user_uid)){
                                    collab = ds.child("Name").getValue().toString() + ", " + collab;
                                }
                            }
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){
                                if(ds.getKey().equals(user_uid)){
                                    builder.append(title + "\n" + time + "\nCollaborators: " + collab);
                                }
                            }
                            dayEventList.add(builder.toString());
                        }
                    }
                }
                EventsAdapter eventsAdapter = new EventsAdapter(dayEventList, DayActivity.this);
                listView.setAdapter(eventsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Edit Event
    public void onEditClick(View view){
        View parentView = (View) view.getParent();
        ListView listView = (ListView) parentView.getParent();
        int position = listView.getPositionForView(parentView);
        Object item = listView.getItemAtPosition(position);
        String uuid = currentUser.getUid();
        String date = date_txt.getText().toString();

        String[] obj = item.toString().split("\n");
        String title = obj[0];
        String time = obj[1];
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {  //foreach event
                    if (snapshot.child("Title").getValue().toString().equals(title) && snapshot.child("Start date").getValue().toString().equals(date)
                            &&  snapshot.child("Start time").getValue().toString().equals(time)) {
                        Intent intent = new Intent(DayActivity.this, EditEventActivity.class);
                        String key = snapshot.getKey(); //Key of Event
                        String id;
                        //Check if currentUser the creator or collaborator of event.
                        if(snapshot.child("Creator").getValue().toString().equals(uuid)){
                            id = "Creator";
                        } else {
                            id = "Collaborator";
                        }
                        intent.putExtra("Title", title);
                        intent.putExtra("Date", date);
                        intent.putExtra("Time", time);
                        intent.putExtra("ID", id);
                        intent.putExtra("Key", key);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Send SMS
    public void onBellClick(View v) {
        // First get permission to send SMS
        if (ActivityCompat.checkSelfPermission(DayActivity.this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DayActivity.this, new String[]{Manifest.permission.SEND_SMS}, 5434);
            return;
        }
        View parentRow = (View) v.getParent();
        ListView listView = (ListView) parentRow.getParent();
        final int position = listView.getPositionForView(parentRow);
        Object item = listView.getItemAtPosition(position);
        String[] obj = item.toString().split("\n");
        String SMS = obj[0] + " " + obj[1] + " " + date_txt.getText().toString();
        String row3 = obj[2];
        String[] row3_split = row3.split(":");
        String collabs = row3_split[1];
        String[] split_collabs = collabs.split("\\,");
        AlertDialog.Builder alert = new AlertDialog.Builder(DayActivity.this);
        alert.setTitle("Send reminder SMS to collaborators.");
        alert.setMessage("Are you sure you want to send SMS?");
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String[] innerData = new String[split_collabs.length];
                for(int i=0;i<split_collabs.length;i++){
                    innerData[i] = split_collabs[i];
                    String id = innerData[i].substring(1);;
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {   //foreach event
                                if (dataSnapshot.child("Name").getValue().equals(id)) {
                                    String phoneNumber = dataSnapshot.child("Phone Number").getValue().toString();
                                    SmsManager manager = SmsManager.getDefault();
                                    manager.sendTextMessage(phoneNumber, null, "You have a reminder: " + SMS, null, null);
                                    Toast.makeText(DayActivity.this, R.string.toast_smsSent , Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        alert.show();
    }
}
