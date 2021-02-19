package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    DatabaseReference rootRef, ref, dataSnapshot;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = rootRef.child(currentUser.getUid());

        String date = getIntent().getStringExtra("Date"); // Get selected date from main activity
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
                Intent intent = new Intent(getApplicationContext(), NewEventActivity.class);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });
    }

    // Retrieve data from firebase
    public void retrieveData(){
        DatabaseReference events_ref = ref.child("Events");
        listView = findViewById(R.id.listView);
        ArrayList<String> arrayList = new ArrayList<>();
        events_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String date = snapshot.child("Start date").getValue().toString();
                    if (date.equals(date_txt.getText().toString())) {
                        final String title = snapshot.child("Title").getValue().toString();
                        StringBuilder builder = new StringBuilder();
                        builder.append(date);
                        builder.append(title);
                        arrayList.add(builder.toString());
                    } else {
                        arrayList.add("No events this day");
                    }
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