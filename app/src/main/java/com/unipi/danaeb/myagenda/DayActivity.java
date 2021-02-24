package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

import java.sql.Ref;
import java.util.ArrayList;

public class DayActivity extends AppCompatActivity {

    FloatingActionButton newEvent_bt, back_bt2;
    TextView date_txt;
    ListView listView;
    ListAdapter arrayAdapter;
    FirebaseDatabase database;
    DatabaseReference rootRef, ref, eventsRef;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        eventsRef = database.getReference("Events");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = rootRef.child(currentUser.getUid());

        String date = getIntent().getStringExtra("Date"); // Get selected date from main activity or edit activity
        date_txt = findViewById(R.id.date_txt);
        date_txt.setText(date);

        listView = findViewById(R.id.listView);
        retrieveData();

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
                Intent intent = new Intent(DayActivity.this, NewEventActivity.class);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });

        // Click item on listview listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = listView.getItemAtPosition(position);
                String[] obj = item.toString().split("\n");
                String t = obj[0];
                String time = obj[1];
                String collaborators = obj[2];
                eventsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot zoneSnapshot1 : dataSnapshot.getChildren()) {
                            if (zoneSnapshot1.child("Title").getValue().toString().equals(t) && zoneSnapshot1.child("Start date").getValue().toString().equals(date_txt.getText().toString())
                                    &&  zoneSnapshot1.child("Start time").getValue().toString().equals(time)) {
                                String key = zoneSnapshot1.getKey();
                                Intent intent = new Intent(DayActivity.this, EditEventActivity.class);
                                intent.putExtra("Title", t);
                                intent.putExtra("Date", date);
                                intent.putExtra("Time", time);
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
        });

    }

    // Retrieve data from firebase
    public void retrieveData() {
        listView = findViewById(R.id.listView);
        ArrayList<String> arrayList = new ArrayList<>();
        eventsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot : dataSnapshot.getChildren()) {
                    String key = zoneSnapshot.getKey();
                    DatabaseReference key_ref = eventsRef.child(key);
                    key_ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String date = zoneSnapshot.child("Start date").getValue().toString();
                            StringBuilder builder = new StringBuilder();
                            if (date.equals(date_txt.getText().toString())) {
                                final String title = zoneSnapshot.child("Title").getValue().toString();
                                final String time = zoneSnapshot.child("Start time").getValue().toString();
                                if(zoneSnapshot.child("Collaborators").getValue().equals("-")){
                                    builder.append(title + "\n" + time + "\nCollaborators: -");
                                } else {
                                    String collab = "";
                                    long count = zoneSnapshot.getChildrenCount();
                                    long i = 1;
                                    for (DataSnapshot ds : zoneSnapshot.child("Collaborators").getChildren()){
                                       if(count==1 || i==1){
                                           collab = ds.getKey();
                                       } else {
                                           collab = ds.getKey() + ", " + collab;
                                       }
                                       i++;
                                    }
                                    builder.append(title + "\n" + time + "\nCollaborators: " + collab);
                                }
                                arrayList.add(builder.toString());
                            }
                            arrayAdapter = new ArrayAdapter<>(DayActivity.this, android.R.layout.simple_list_item_1, arrayList);
                            listView.setAdapter(arrayAdapter);

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
