package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference rootRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EditText name, surname, address, profession;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = rootRef.child(currentUser.getUid());
        name = findViewById(R.id.editText_name);
        surname = findViewById(R.id.editText_surname);
        address = findViewById(R.id.editText_address);
        profession = findViewById(R.id.editText_profession);
        save = findViewById(R.id.button_save);

        checkForUserInfo();
    }

    private void checkForUserInfo(){
        try {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //User info exists.
                    if (snapshot.exists()) {
                        name.setText(snapshot.child("Name").getValue().toString());
                        surname.setText(snapshot.child("Surname").getValue().toString());
                        address.setText(snapshot.child("Address").getValue().toString());
                        profession.setText(snapshot.child("Profession").getValue().toString());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {

        }
    }

    public void save(View view){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //first login of user.
                if (!snapshot.exists()) {
                    ref.child("Name").setValue(name.getText().toString());
                    ref.child("Surname").setValue(surname.getText().toString());
                    ref.child("Address").setValue(address.getText().toString());
                    ref.child("Profession").setValue(profession.getText().toString());
                }
                else{ //update data.
                    ref.child("Name").setValue(name.getText().toString());
                    ref.child("Surname").setValue(surname.getText().toString());
                    ref.child("Address").setValue(address.getText().toString());
                    ref.child("Profession").setValue(profession.getText().toString());
                }
                Toast.makeText(getApplicationContext(), R.string.toast_ProfileSave, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}