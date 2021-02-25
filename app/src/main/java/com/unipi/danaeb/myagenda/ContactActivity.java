package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FloatingActionButton button_newContact;
    private ListView listView;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        navigationBar();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        listView = findViewById(R.id.listView_contacts);
        button_newContact = findViewById(R.id.button_newContact);

        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");

        button_newContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),NewContactActivity.class);
                startActivity(intent);
            }
        });

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
                Object item = listView.getItemAtPosition(position);
                String[] obj = item.toString().split("\n");
                String row1 = obj[0];
                String row2 = obj[1];

                String[] names = row1.split(":");
                String name = names[1].substring(1);
                String[] phonenumbers = row2.split(":");
                String phonenumber = phonenumbers[1].substring(1);

                AlertDialog.Builder alert = new AlertDialog.Builder(ContactActivity.this);
                alert.setTitle("Delete entry");
                alert.setMessage("Are you sure you want to delete?");
                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.delete("Contacts","name=? AND phonenumber=?",new String[]{name,phonenumber});
                        Toast.makeText(ContactActivity.this, R.string.toast_ContactDelete , Toast.LENGTH_LONG).show();
                        viewContactList(); //Reload contact data after deletion.
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
        });
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