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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class NewEventActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseDatabase database;
    DatabaseReference usersRef, eventsRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private StorageReference storageReference;
    FloatingActionButton back_bt1, save_bt, location_bt;
    Dialog myDialog;
    EditText event_title, event_location, event_description;
    TextView start_date, start_time, end_date, end_time, collaborators, color_txt;
    Spinner reminder_sp, spinner_collaborators;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private ArrayList<Contact> contacts = new ArrayList<Contact>();
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        String date = getIntent().getStringExtra("Date"); // Get selected date from day activity

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        eventsRef = database.getReference("Events");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = usersRef.child(currentUser.getUid());
        event_title = findViewById(R.id.event_title);
        event_location = findViewById(R.id.event_location);
        event_description = findViewById(R.id.event_description);
        start_date = findViewById(R.id.start_date);
        end_date = findViewById(R.id.end_date);
        start_time = findViewById(R.id.start_time);
        end_time = findViewById(R.id.end_time);
        //collaborators = findViewById(R.id.textView_collaborators);
        spinner_collaborators = findViewById(R.id.spinner_collaborators);
        reminder_sp = findViewById(R.id.reminder_sp);
        color_txt = findViewById(R.id.color_txt);
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");

        myDialog = new Dialog(this);

        // Back button
        back_bt1 = findViewById(R.id.back_bt3);
        back_bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEventActivity.this, DayActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Date", date);
                startActivity(intent);
            }
        });

        // Map button
        location_bt = findViewById(R.id.location_bt);
        location_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewEventActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        // Set selected date to textboxes
        start_date = findViewById(R.id.start_date);
        start_date.setText(date);
        end_date = findViewById(R.id.end_date);
        end_date.setText(date);

        // Set current hour to textbox
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH:mm");
        String strDate = mdformat.format(calendar.getTime());
        start_time = findViewById(R.id.start_time);
        start_time.setText(strDate);

        // Add 1 hour to end_time textbox
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String strDate1 = mdformat.format(calendar.getTime());
        end_time = findViewById(R.id.end_time);
        end_time.setText(strDate1);

        start_date.setOnClickListener(this);
        end_date.setOnClickListener(this);
        start_time.setOnClickListener(this);
        end_time.setOnClickListener(this);

        Cursor cursor = db.rawQuery("SELECT * FROM Contacts",null);
        //Create contact list
        contacts.add(new Contact("Collaborators", "-"));
        if (cursor.getCount()>0){
            while (cursor.moveToNext()){
                Contact contact = new Contact(cursor.getString(0), cursor.getString(1));
                contacts.add(contact);
            }
        }
        //load Contact Spinner
        SpinnerContactAdapter contactAdapter = new SpinnerContactAdapter(this, 0, contacts);
        spinner_collaborators.setAdapter(contactAdapter); // Set the spinners adapter to the previously created one.
        /*ArrayList<Contact> selectedContacts = new ArrayList<Contact>();
        spinner_collaborators.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String name = spinner_collaborators.getSelectedItem().toString();
                if(!name.equals("Collaborators")){
                    Contact c =
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });*/

        // Create spinner dropdown for reminder notification
        Spinner dropdown = findViewById(R.id.reminder_sp); // Get the spinner from the xml.
        String[] items = new String[]{"15 minutes before", "30 minutes before", "1 hour before", "1 day before"}; // Create a list of items for the spinner.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items); // Create an adapter to describe how the items are displayed.
        dropdown.setAdapter(adapter); // Set the spinners adapter to the previously created one.
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
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
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
                color_txt = findViewById(R.id.color_txt);
                int radioButtonID = radioGroup.getCheckedRadioButtonId(); // get selected radio button from radioGroup
                View radioButton = radioGroup.findViewById(radioButtonID); // find the radiobutton by returned id
                int radioId = radioGroup.indexOfChild(radioButton); //get the index of the selected radio button
                RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                String selection = (String) btn.getText();
                if (selection.equals("Purple")){
                    color_txt.setBackgroundResource(R.color.Purple);
                } else if (selection.equals("Red")){
                    color_txt.setBackgroundResource(R.color.Red);
                } else if (selection.equals("Green")){
                    color_txt.setBackgroundResource(R.color.Green);
                } else if (selection.equals("Teal")){
                    color_txt.setBackgroundResource(R.color.Teal);
                } else if (selection.equals("Black")){
                    color_txt.setBackgroundResource(R.color.Black);
                } else if (selection.equals("White")){
                    color_txt.setBackgroundResource(R.color.White);
                }
                color_txt.setText(selection);
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
                    event_ref.child("Creator").setValue(currentUser.getUid());
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
                    event_ref.child("Reminder").setValue(reminder_sp.getSelectedItem().toString());
                    event_ref.child("Color").setValue(color_txt.getText().toString());
                    // Add collaborators to firebase based on collaborators uid
                    event_ref.child("Collaborators").setValue("-");
                    DatabaseReference collab_ref = event_ref.child("Collaborators");
                    for(Contact c : contacts){
                        if(c.isSelected()){
                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                                        String uid = zoneSnapshot.getKey();
                                        DatabaseReference uid_ref = usersRef.child(uid);
                                        String name = zoneSnapshot.child("Name").getValue().toString();
                                        String phone = zoneSnapshot.child("Phone Number").getValue().toString();
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
            Intent intent = new Intent(NewEventActivity.this, DayActivity.class);
            intent.putExtra("Date", date);
            startActivity(intent);
        }
    }

    public void addCollaborators(View view){
        Intent intent = new Intent(getApplicationContext(),ContactActivity.class);
        intent.putExtra("id","Event");
        startActivityForResult(intent,456);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==456){

        } else if(requestCode==123){   //Return from MapsActivity
            double latitude = data.getDoubleExtra("latitude",0.0);
            double longitude = data.getDoubleExtra("longitude",0.0);
            String location = data.getStringExtra("location");
            if(location!=null && !location.isEmpty()){
                event_location.setText(location);
            }
        }
    }
}