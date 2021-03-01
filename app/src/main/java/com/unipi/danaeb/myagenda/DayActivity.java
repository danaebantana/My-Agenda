package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
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
import java.util.Collections;

public class DayActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton button_newEvent, button_back;
    private TextView date_txt;
    private ListView listView;
    private SQLiteDatabase db;
    ArrayList<String> eventUid = new ArrayList<String>();

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
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");

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
        ArrayList<Event> dayEventList = new ArrayList<Event>();
        ArrayList<String> stringEventList = new ArrayList<String>();
        eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserName = "";
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){   //foreach event
                    //If the current user is the 'Creator' of this event.
                    if(dataSnapshot.child("Creator").child("Uid").getValue().equals(user_uid) && dataSnapshot.child("Start date").getValue().equals(date_txt.getText().toString())){
                        Event event = new Event();
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String time = dataSnapshot.child("Start time").getValue().toString();
                        currentUserName = dataSnapshot.child("Creator").child("Name").getValue().toString();
                        event.setTitle(title);
                        event.setTime(time);
                        event.setUid(dataSnapshot.getKey());
                        ArrayList<String> collab = new ArrayList<String>();
                        ArrayList<String> comments = new ArrayList<String>();
                        ArrayList<String> attending = new ArrayList<String>();
                        if(dataSnapshot.child("Collaborators").getValue().equals("-")){
                            collab.add("-");
                            event.setCollaborators(collab);
                        } else {
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){
                                collab.add(ds.child("Name").getValue().toString());
                                comments.add(ds.child("Comments").getValue().toString());
                                attending.add(ds.child("Attendance").getValue().toString());
                            }
                            event.setCollaborators(collab);
                            event.setComments(comments);
                            event.setAttendance(attending);
                        }
                        dayEventList.add(event);
                    }
                    //If the current user is not the 'Creator' of the event, we search if the current user is a collaborator so we can view the event.
                    else if(dataSnapshot.child("Start date").getValue().equals(date_txt.getText().toString()) && !dataSnapshot.child("Collaborators").getValue().equals("-")) {
                        Event event = new Event();
                        String title = dataSnapshot.child("Title").getValue().toString();
                        String time = dataSnapshot.child("Start time").getValue().toString();
                        ArrayList<String> collab = new ArrayList<String>();
                        ArrayList<String> comments = new ArrayList<String>();
                        ArrayList<String> attending = new ArrayList<String>();
                        boolean flag = false;
                        for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){    //Check if current user is a collaborator of this event.
                            if(ds.getKey().equals(user_uid)){
                                flag = true;
                                event.setTitle(title);
                                event.setTime(time);
                                event.setUid(dataSnapshot.getKey());
                                currentUserName = ds.child("Name").getValue().toString();
                                collab.add(dataSnapshot.child("Creator").child("Name").getValue().toString());
                                comments.add("-");
                                attending.add("true");
                            }
                        }
                        if(flag){  //Current user is a collaborator
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){   //Get other collaborators if they exist.
                                if(!ds.getKey().equals(user_uid)){
                                    collab.add(ds.child("Name").getValue().toString());
                                    comments.add(ds.child("Comments").getValue().toString());
                                    attending.add(ds.child("Attendance").getValue().toString());
                                }
                            }
                            for (DataSnapshot ds : dataSnapshot.child("Collaborators").getChildren()){   //Add current user's comments and attendance.
                                if(ds.getKey().equals(user_uid)){
                                    collab.add("My Comments");
                                    comments.add(ds.child("Comments").getValue().toString());
                                    attending.add(ds.child("Attendance").getValue().toString());
                                }
                            }
                            event.setCollaborators(collab);
                            event.setComments(comments);
                            event.setAttendance(attending);
                            dayEventList.add(event);
                        }
                    }
                }
                //sort events based on time of event.
                Collections.sort(dayEventList, (e1, e2) -> e1.getTime().compareTo(e2.getTime()));
                //We transform each event into a string to be printed.
                for (Event event : dayEventList){
                    StringBuilder builder = new StringBuilder();
                    if(event.getCollaborators().get(0).equals("-")){   //event has no collaborators.
                        builder.append(event.getTitle() + "\n" + event.getTime() + "\nCollaborators: -");
                    } else {
                        builder.append(event.getTitle() + "\n" + event.getTime() + "\nCollaborators:  \n");
                        int numOfCollb = event.getCollaborators().size();
                        for(int i=0; i<numOfCollb; i++){
                            builder.append(event.getCollaborators().get(i) + ": " + event.getComments().get(i) + "\n");
                        }
                        builder.append("Attending: ");
                        int numOfAttendees = event.getAttendance().size();
                        boolean flag = false;
                        for(int i=0; i<numOfAttendees; i++){
                            if(event.getAttendance().get(i).equals("true") && !event.getCollaborators().get(i).equals("My Comments")){
                                builder.append(event.getCollaborators().get(i) + " ");
                                flag = true;
                            } else if(event.getAttendance().get(i).equals("true") && event.getCollaborators().get(i).equals("My Comments")){
                                builder.append(currentUserName + " ");
                                flag = true;
                            }
                        }
                        if(!flag){
                            builder.append("-");
                        }
                    }
                    eventUid.add(event.getUid());
                    stringEventList.add(builder.toString());
                }
                EventsAdapter eventsAdapter = new EventsAdapter(stringEventList, DayActivity.this);
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
        String selectedEventUid = eventUid.get(position);   //get the uid of the selected event.

        String[] obj = item.toString().split("\n");
        String title = obj[0];
        String time = obj[1];
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {  //foreach event
                    if (snapshot.getKey().equals(selectedEventUid)) {
                        Intent intent = new Intent(DayActivity.this, EditEventActivity.class);
                        String key =  selectedEventUid; //Key of Event
                        String id;
                        //Check if currentUser the creator or collaborator of event.
                        if(snapshot.child("Creator").child("Uid").getValue().toString().equals(uuid)){
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
        //Check for collaborators
        String[] collaborators = obj[2].split(":");
        ArrayList<String> listOfCollaboratorsNames = new ArrayList<String>();
        ArrayList<String> listOfCollaboratorsPhoneNumbers = new ArrayList<String>();
        if(collaborators[1].substring(1).equals("-")){  //No collaborators
            Toast.makeText(this, R.string.toast_noCollToInform, Toast.LENGTH_LONG).show();
        } else {
            for(int i=3; i<obj.length; i++){
                String[] collaborator = obj[i].split(":");
                if(collaborator[0].equals("My Comments") || collaborator[0].equals("Attending")){
                    break;
                }
                listOfCollaboratorsNames.add(collaborator[0]);
                Cursor cursor = db.rawQuery("SELECT * FROM Contacts WHERE name=?", new String[]{collaborator[0]});
                if (cursor.getCount()>0){  //Contact name found. Cursor will always return 1 or 0 objects.
                    while (cursor.moveToNext()){
                        listOfCollaboratorsPhoneNumbers.add(cursor.getString(1));  //Get phoneNumbers
                    }
                } else{
                    listOfCollaboratorsPhoneNumbers.add("-");  //Get phoneNumbers
                }
            }
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(DayActivity.this);
        alert.setTitle("Send reminder SMS to collaborators.");
        alert.setMessage("Are you sure you want to send SMS?");
        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                int i=0;
                for (String phoneNumber : listOfCollaboratorsPhoneNumbers){
                    if(phoneNumber.equals("-")){
                        Toast.makeText(getApplicationContext(), R.string.toast_noCorrespondingNumber + listOfCollaboratorsNames.get(i), Toast.LENGTH_LONG).show();
                    } else {
                        SmsManager manager = SmsManager.getDefault();
                        manager.sendTextMessage(phoneNumber, null, R.string.toast_smsReminder + SMS, null, null);
                    }
                    i++;
                }
                Toast.makeText(DayActivity.this, R.string.toast_smsSent , Toast.LENGTH_LONG).show();
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
