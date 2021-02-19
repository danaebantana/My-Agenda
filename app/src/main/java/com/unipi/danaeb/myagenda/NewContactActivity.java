package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;

public class NewContactActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference rootRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private StorageReference storageReference;
    SQLiteDatabase db;
    private EditText name, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);
        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        name = findViewById(R.id.editText_contactName);
        phoneNumber = findViewById(R.id.editText_contactPhone);
        db = openOrCreateDatabase("ContactsDB", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Contacts(name TEXT,phonenumber TEXT)");
    }

    public void save(View view){
        if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(phoneNumber.getText().toString())) {          //Check if inputs are valid.
            Toast.makeText(this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        } else {
            //Check if contact is a user of the My-Agenda App.
            String contactName = name.getText().toString();
            String contactPhone = phoneNumber.getText().toString();
            isCollaboratorAUser(contactName, contactPhone);
        }
    }

    private void isCollaboratorAUser(String n, String pn){
        rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = false;
                for (DataSnapshot zoneSnapshot : snapshot.getChildren()) {
                    String name = zoneSnapshot.child("Name").getValue().toString();
                    String phone = zoneSnapshot.child("Phone Number").getValue().toString();
                    if (n.equals(name) && pn.equals(phone) && checkIfAlreadyInDatabase(name, phone)) {
                        db.execSQL("INSERT INTO Contacts VALUES('" + name + "','" + phone + "')");
                        flag = true;
                        break;
                    } else {
                        flag = false;
                    }
                }
                if(flag){
                    Toast.makeText(getApplication(), R.string.toast_save, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(),ContactActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplication(), R.string.toast_noUserFound, Toast.LENGTH_LONG).show();
                    name.setText("");
                    phoneNumber.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //If the given credentials are NOT already a contact the function returns true. Otherwise it returns false.
    private boolean checkIfAlreadyInDatabase(String n, String pn){
        boolean flag = true;
        Cursor cursor = db.rawQuery("SELECT * FROM Contacts",null);
        ArrayList<String> listData = new ArrayList<>();
        if (cursor.getCount()>0){
            while (cursor.moveToNext()){
                String name = cursor.getString(0);
                String phone = cursor.getString(1);
                if(n.equals(name) && pn.equals(phone)){
                    flag = false;
                }
            }
        }
        return flag;
    }
}