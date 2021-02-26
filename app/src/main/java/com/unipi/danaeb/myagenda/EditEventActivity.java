package com.unipi.danaeb.myagenda;

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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;

public class EditEventActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseDatabase database;
    private DatabaseReference rootRef, eventsRef;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton button_back, button_save, button_delete, button_location;
    private EditText editEvent_title, editEvent_location, editEvent_description;
    private TextView start_date, start_time, end_date, end_time, colorPicker;
    private Spinner spinner_editReminder, spinner_editCollaborators;
    private Dialog myDialog;
    private SQLiteDatabase db;
    private int GOOGLE_MAPS_ACTIVITY = 123;
    private Double lon, lat;
    private String location_name;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        eventsRef = database.getReference("Events");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        button_back = findViewById(R.id.button_back2);
        button_save = findViewById(R.id.button_saveEditEvent);
        button_delete = findViewById(R.id.button_deleteEvent);
        button_location = findViewById(R.id.button_editLocation);
        editEvent_title = findViewById(R.id.editText_editTitle);
        editEvent_location = findViewById(R.id.editText_editLocation);
        editEvent_description = findViewById(R.id.editText_editDescription);
        start_date = findViewById(R.id.textView_editStartDate);
        end_date = findViewById(R.id.textView_editEndDate);
        start_time = findViewById(R.id.textView_editStartTime);
        end_time = findViewById(R.id.textView_editEndTime);
        colorPicker = findViewById(R.id.textView_editColorPicker);
        spinner_editCollaborators = findViewById(R.id.spinner_editCollaborators);
        spinner_editReminder = findViewById(R.id.spinner_editReminder);
        myDialog = new Dialog(this);
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");

        String title = getIntent().getStringExtra("Title"); // Get selected title from day activity
        String date = getIntent().getStringExtra("Date"); // Get date from day activity
        String key = getIntent().getStringExtra("Key"); // Get key from day activity


        editEvent_title.setText(title);
        // Retrieve data from firebase and print them to textboxes
        DatabaseReference events_ref = eventsRef.child(key);
        events_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String location = dataSnapshot.child("Location").getValue().toString();
                editEvent_location.setText(location);
                String description = dataSnapshot.child("Description").getValue().toString();
                editEvent_description.setText(description);
                String start_d = dataSnapshot.child("Start date").getValue().toString();
                start_date.setText(start_d);
                String end_d = dataSnapshot.child("End date").getValue().toString();
                end_date.setText(end_d);
                String start_t = dataSnapshot.child("Start time").getValue().toString();
                start_time.setText(start_t);
                String end_t = dataSnapshot.child("End time").getValue().toString();
                end_time.setText(end_t);
                //Load collaborator spinner
                Cursor cursor = db.rawQuery("SELECT * FROM Contacts",null);
                //Create contact list isSelected of all contacts is false.
                contacts.add(new Contact("Collaborators", "-"));
                if (cursor.getCount()>0){
                    while (cursor.moveToNext()){
                        Contact contact = new Contact(cursor.getString(0), cursor.getString(1));
                        contacts.add(contact);
                    }
                }
                //load Contact Spinner
                DatabaseReference collab_ref = dataSnapshot.child("Collaborators").getRef();
                collab_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                            String eid = zoneSnapshot.getKey();
                            String name = zoneSnapshot.child("Name").getValue().toString();
                            String phone = zoneSnapshot.child("Phone Number").getValue().toString();
                            for (Contact c : contacts){
                                if(name.equals(c.getName()) && phone.equals(c.getPhoneNumber())){
                                    c.setSelected(true);
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                SpinnerContactAdapter contactAdapter = new SpinnerContactAdapter(getApplication(), 0, contacts);
                spinner_editCollaborators.setAdapter(contactAdapter); // Set the spinners adapter to the previously created one.

                //spinner_editRemider load selected value.

                String color = dataSnapshot.child("Color").getValue().toString();
                colorPicker.setText(color);
                if (color.equals("Purple")){
                    colorPicker.setBackgroundResource(R.color.Purple);
                } else if (color.equals("Red")){
                    colorPicker.setBackgroundResource(R.color.Red);
                } else if (color.equals("Green")){
                    colorPicker.setBackgroundResource(R.color.Green);
                } else if (color.equals("Teal")){
                    colorPicker.setBackgroundResource(R.color.Teal);
                } else if (color.equals("Black")){
                    colorPicker.setBackgroundResource(R.color.Black);
                } else if (color.equals("White")){
                    colorPicker.setBackgroundResource(R.color.White);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        // Back button
        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEventActivity.this, DayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });

        // Map button
        button_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditEventActivity.this, MapsActivity.class);
                startActivityForResult(intent,GOOGLE_MAPS_ACTIVITY);
            }
        });

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
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
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

    // Edit event to firebase
    public void saveEditedEvent(View view) {
        String date = getIntent().getStringExtra("Date");
        String key = getIntent().getStringExtra("Key");
        DatabaseReference events_ref = eventsRef.child(key);
        events_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                events_ref.child("Title").setValue(editEvent_title.getText().toString());
                events_ref.child("Location").setValue(editEvent_location.getText().toString());
                events_ref.child("Description").setValue(editEvent_description.getText().toString());
                events_ref.child("Start date").setValue(start_date.getText().toString());
                events_ref.child("Start time").setValue(start_time.getText().toString());
                events_ref.child("End date").setValue(end_date.getText().toString());
                events_ref.child("End time").setValue(end_time.getText().toString());
                //events_ref.child("Collaborators").setValue(spinner_editCollaborators.getSelectedItem().toString());
                //edit Collaborators.
                events_ref.child("Reminder").setValue(spinner_editReminder.getSelectedItem().toString());
                events_ref.child("Color").setValue(colorPicker.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Toast.makeText(getApplicationContext(), R.string.toast_EventSave, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(EditEventActivity.this, DayActivity.class);
        intent.putExtra("Date", date);
        startActivity(intent);
    }

    // Delete event from firebase
    public void deleteEvent(View view) {
        String key = getIntent().getStringExtra("Key"); // Get selected title from day activity
        String date = getIntent().getStringExtra("Date"); // Get date from day activity
        DatabaseReference events_ref = eventsRef.child(key);
        events_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                events_ref.removeValue();
                Toast.makeText(getApplicationContext(), R.string.toast_EventDelete, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(EditEventActivity.this, DayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Date", date);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if(requestCode==GOOGLE_MAPS_ACTIVITY){
                lat = data.getDoubleExtra("latitude",0.0);
                lon = data.getDoubleExtra("longitude",0.0);
                location_name = data.getStringExtra("name");
                editEvent_location.setText(location_name);
             }
    }
}