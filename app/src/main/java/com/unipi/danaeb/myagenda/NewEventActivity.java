package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class NewEventActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText event_title, event_location, event_description;
    private TextView start_date, start_time, end_date, end_time, colorPicker;
    private FloatingActionButton button_back, button_save, button_location;
    private Spinner spinner_reminder, spinner_collaborators;

    private Dialog myDialog;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        String date = getIntent().getStringExtra("Date"); // Get selected date from day activity

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        eventsRef = database.getReference("Events");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        event_title = findViewById(R.id.editText_eventTitle);
        event_location = findViewById(R.id.editText_eventLocation);
        event_description = findViewById(R.id.editText_eventDescription);

        start_date = findViewById(R.id.textView_startDate);
        end_date = findViewById(R.id.textView_endDate);
        start_time = findViewById(R.id.textView_startTime);
        end_time = findViewById(R.id.textView_endTime);
        colorPicker = findViewById(R.id.textView_colorPicker);

        button_back = findViewById(R.id.button_back3);
        button_location = findViewById(R.id.button_getLocation);
        button_save = findViewById(R.id.button_saveChanges);

        spinner_collaborators = findViewById(R.id.spinner_addCollaborators);
        spinner_reminder = findViewById(R.id.spinner_pickReminder);

        myDialog = new Dialog(this);

        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");

        // Back button
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEventActivity.this, DayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });

        // Map button
        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEventActivity.this, MapsActivity.class);
                startActivityForResult(intent,123);
            }
        });

        // Set selected date to textboxes
        start_date = findViewById(R.id.textView_startDate);
        start_date.setText(date);
        end_date = findViewById(R.id.textView_endDate);
        end_date.setText(date);

        // Set current hour to textbox
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
        String strDate = mdformat.format(calendar.getTime());
        start_time = findViewById(R.id.textView_startTime);
        start_time.setText(strDate);

        // Add 1 hour to textView_endTime textbox
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String strDate1 = mdformat.format(calendar.getTime());
        end_time = findViewById(R.id.textView_endTime);
        end_time.setText(strDate1);

        start_date.setOnClickListener(this);
        end_date.setOnClickListener(this);
        start_time.setOnClickListener(this);
        end_time.setOnClickListener(this);

        //Create contact list from SQLite database and load it in spinner_collaborators
        Cursor cursor = db.rawQuery("SELECT * FROM Contacts",null);
        contacts.add(new Contact("Collaborators", "-"));
        if (cursor.getCount()>0){
            while (cursor.moveToNext()){
                Contact contact = new Contact(cursor.getString(0), cursor.getString(1));
                contacts.add(contact);
            }
        }
        SpinnerContactAdapter contactAdapter = new SpinnerContactAdapter(this, 0, contacts);
        spinner_collaborators.setAdapter(contactAdapter); // Set the spinners adapter to the previously created one.

        // Create reminder list and load it in spinner_reminder
        String[] items = new String[]{"15 minutes before", "30 minutes before", "1 hour before", "1 day before"}; // Create a list of items for the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items); // Create an adapter to describe how the items are displayed.
        spinner_reminder.setAdapter(adapter); // Set the spinners adapter to the previously created one.
    }

    //Pop up window to choose date and time
    @Override
    public void onClick(View v) {
        // Date picker pops up when date textboxes are clicked
        if ((v == start_date) || (v == end_date)) {

            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            // Launch Date Picker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            if (v == start_date) {
                                start_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                                end_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            } else if (v == end_date) {
                                end_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        // Time picker pops up when time textboxes are clicked
        if ((v == start_time) || (v == end_time)) {

            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            if (v == start_time) {
                                start_time.setText(hourOfDay + ":" + minute);
                            } else if (v == end_time) {
                                end_time.setText(hourOfDay + ":" + minute);
                            }
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        }
    }

    // New dialog window to change event color
    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.custompopup);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();

        RadioGroup radioGroup = myDialog.findViewById(R.id.RadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId(); // get selected radio button from radioGroup
                View radioButton = radioGroup.findViewById(radioButtonID); // find the radiobutton by returned id
                int radioId = radioGroup.indexOfChild(radioButton); //get the index of the selected radio button
                RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                String selection = (String) btn.getText();
                if (selection.equals("Purple")){
                    colorPicker.setBackgroundResource(R.color.Purple);
                } else if (selection.equals("Red")){
                    colorPicker.setBackgroundResource(R.color.Red);
                } else if (selection.equals("Green")){
                    colorPicker.setBackgroundResource(R.color.Green);
                } else if (selection.equals("Teal")){
                    colorPicker.setBackgroundResource(R.color.Teal);
                } else if (selection.equals("Black")){
                    colorPicker.setBackgroundResource(R.color.Black);
                } else if (selection.equals("White")){
                    colorPicker.setBackgroundResource(R.color.White);
                }
                colorPicker.setText(selection);
                myDialog.dismiss();
            }
        });
    }

    // Save event to firebase
    public void save(View view) {
        String date = getIntent().getStringExtra("Date"); // Get selected date from day activity
        DatabaseReference event_ref = eventsRef.push();   //New Event
        if (event_title.getText().toString().matches("")) {
            Toast.makeText(NewEventActivity.this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        } else {
            event_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    event_ref.child("Creator").child("Uid").setValue(currentUser.getUid());
                    String uid = currentUser.getUid();
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                            for(DataSnapshot zoneSnapshot : datasnapshot.getChildren()){    //foreach user
                                if(zoneSnapshot.getKey().equals(uid)){
                                    event_ref.child("Creator").child("Name").setValue(zoneSnapshot.child("Name").getValue().toString());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    event_ref.child("Title").setValue(event_title.getText().toString());
                    if (event_location.getText().toString().matches("")) {
                        event_ref.child("Location").setValue("-");
                    } else {
                        event_ref.child("Location").setValue(event_location.getText().toString());
                    }
                    if (event_description.getText().toString().matches("")) {
                        event_ref.child("Description").setValue("-");
                    } else {
                        event_ref.child("Description").setValue(event_description.getText().toString());
                    }
                    event_ref.child("Start date").setValue(start_date.getText().toString());
                    event_ref.child("Start time").setValue(start_time.getText().toString());
                    event_ref.child("End date").setValue(end_date.getText().toString());
                    event_ref.child("End time").setValue(end_time.getText().toString());
                    event_ref.child("Reminder").setValue(spinner_reminder.getSelectedItem().toString());
                    event_ref.child("Color").setValue(colorPicker.getText().toString());
                    // Add collaborators to firebase based on collaborators uid
                    event_ref.child("Collaborators").setValue("-");
                    DatabaseReference collab_ref = event_ref.child("Collaborators").getRef();
                    for(Contact c : contacts){
                        if(c.isSelected()){
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                    for (DataSnapshot zoneSnapshot : datasnapshot.getChildren()) {
                                        String uid = zoneSnapshot.getKey();
                                        String name = zoneSnapshot.child("Name").getValue().toString();
                                        String phone = zoneSnapshot.child("Phone Number").getValue().toString();
                                        usersRef.child(uid);
                                        if(name.equals(c.getName()) && phone.equals(c.getPhoneNumber())){
                                            collab_ref.child(uid).child("Name").setValue(c.getName());
                                            collab_ref.child(uid).child("Phone Number").setValue(c.getPhoneNumber());
                                            collab_ref.child(uid).child("Comments").setValue("-");
                                            collab_ref.child(uid).child("Attendance").setValue("false");
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                    Toast.makeText(getApplicationContext(), R.string.toast_EventSave, Toast.LENGTH_LONG).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Intent intent = new Intent(this, DayActivity.class);
            intent.putExtra("Date", date);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==123){   //Return from MapsActivity
            double latitude = data.getDoubleExtra("latitude",0.0);
            double longitude = data.getDoubleExtra("longitude",0.0);
            String location = data.getStringExtra("location");
            if(location!=null && !location.isEmpty()){
                event_location.setText(location);
            }
        }
    }

}