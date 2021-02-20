package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    FirebaseDatabase database;
    DatabaseReference rootRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FloatingActionButton buttonNewContact, buttonSelectedCollab;
    SQLiteDatabase db;
    ListView listView;
    String id;
    RadioGroup radioGroup;
    ScrollView scrollView;
    LinearLayout linearLayout;
    ArrayList<Contact> contacts = new ArrayList<Contact>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        navigationBar();
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        buttonNewContact = findViewById(R.id.button_newContact);
        buttonSelectedCollab = findViewById(R.id.button_selectedCollab);
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");
        listView = findViewById(R.id.listView);
        linearLayout = findViewById(R.id.linear_Layout);
        scrollView = findViewById(R.id.scrollView);
        id = getIntent().getStringExtra("id");
        if(id.equals("Contacts")){
            listView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.INVISIBLE);
            buttonNewContact.setVisibility(View.VISIBLE);
            buttonSelectedCollab.setVisibility(View.INVISIBLE);
            viewContactList();
        } else if(id.equals("Event")){
            listView.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.VISIBLE);
            buttonNewContact.setVisibility(View.INVISIBLE);
            buttonSelectedCollab.setVisibility(View.VISIBLE);
            selectContactList();
        }
    }

    private void viewContactList(){
        Cursor cursor = db.rawQuery("SELECT * FROM Contacts",null);
        ArrayList<String> listData = new ArrayList<>();
        if (cursor.getCount()>0){
            while (cursor.moveToNext()){
                listData.add("Name: " + cursor.getString(0) + "\n" +
                        "Phone Number: " + cursor.getString(1));
            }
            ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                StringBuilder builder = new StringBuilder();
                builder.append(item).append("\n");
                builder.append("-----------------------------------\n");
                showMessage("Contact Details: ",builder.toString());
            }
        });
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    public void selectContactList(){
        //Clear scrollview
        linearLayout.removeAllViews();
        Cursor cursor = db.rawQuery("SELECT * FROM Contacts ORDER BY name ASC", null);
        int i = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setId(i);
                checkBox.setTextAppearance(android.R.style.TextAppearance_Holo_Medium);
                i++;
                checkBox.setText("Name: " + cursor.getString(0) + "\n" + "Phone Number: " + cursor.getString(1));
                linearLayout.addView(checkBox);
            }
        }
    }

    public void selectedCollab(View view){
        int selectedCheckboxes = 0;
        int count = linearLayout.getChildCount();
        for(int i=0; i<= count; i++){
            View child = linearLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    String[] text = cb.getText().toString().split("\n");
                    String[] name = text[0].split(":");
                    String[] phone = text[1].split(":");
                    Contact contact = new Contact(name[1], phone[1]);
                    contacts.add(contact);
                    selectedCheckboxes++;
                }
            }
        }
        if(selectedCheckboxes!=0){
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra("contactList", contacts);
            setResult(456,intent);
            finish();
        }else{  //No checkBoxes selected
            Toast.makeText(this, R.string.toast_checkBoxes, Toast.LENGTH_LONG).show();
        }
    }

    public void newContact(View view){
        Intent intent = new Intent(getApplicationContext(),NewContactActivity.class);
        startActivity(intent);
    }

    public void navigationBar(){
        //Navigation Bar
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutContact);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigationContact);
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
}