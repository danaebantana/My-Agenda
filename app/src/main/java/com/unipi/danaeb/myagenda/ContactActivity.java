package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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

public class ContactActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    FirebaseDatabase database;
    DatabaseReference rootRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FloatingActionButton buttonNewContact;
    SQLiteDatabase db;
    ListView listView;

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
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");
        listView = findViewById(R.id.listView);
        viewContactList();
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