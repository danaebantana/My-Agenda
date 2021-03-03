package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference usersRef, ref;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private StorageReference storageReference;

    private EditText name, address, profession, phoneNumber;
    private FloatingActionButton save;
    private ImageView profilePic;
    public Uri imageUri;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        navigationBar();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = usersRef.child(currentUser.getUid());

        name = findViewById(R.id.editText_name);
        address = findViewById(R.id.editText_address);
        profession = findViewById(R.id.editText_profession);
        phoneNumber = findViewById(R.id.editText_phoneNumber);
        save = findViewById(R.id.button_save);
        profilePic = findViewById(R.id.imageView_profilePic);

        checkForUserInfo();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePicture();
            }
        });
        viewProfilePic();
    }

    // Check if user exists
    private void checkForUserInfo() {
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            // User info exists.
            try {
                if (snapshot.exists()) {
                    name.setText(snapshot.child("Name").getValue().toString());
                    address.setText(snapshot.child("Address").getValue().toString());
                    profession.setText(snapshot.child("Profession").getValue().toString());
                    phoneNumber.setText(snapshot.child("Phone Number").getValue().toString());
                }
            }catch (Exception e) {
                Toast.makeText(getApplication(), R.string.toast_personalData, Toast.LENGTH_LONG).show();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
        });
    }

    // Save user to firebase
    public void save(View view) {
        if (name.getText().equals("") || address.getText().equals("") || profession.getText().equals("") || phoneNumber.getText().equals("")) {
            Toast.makeText(this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        } else {
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //first login of user.
                    if (snapshot.getValue().toString().equals(currentUser.getEmail())) {
                        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                                boolean flag = false;
                                // check if there is already a user with the same name and phone number.
                                for (DataSnapshot zoneSnapshot : datasnapshot.getChildren()){  //foreach user
                                    if (zoneSnapshot.child("Name").exists()) {
                                        String nameOfUser = zoneSnapshot.child("Name").getValue().toString();
                                        String phoneNumberOfUser = zoneSnapshot.child("Phone Number").getValue().toString();
                                        if (nameOfUser.equals(name.getText().toString()) && phoneNumberOfUser.equals(phoneNumber.getText().toString())) {
                                            flag = true;
                                        }
                                    }
                                }
                                if (flag) {
                                    Toast.makeText(getApplication(), R.string.toast_userExists, Toast.LENGTH_LONG).show();
                                } else {
                                    ref.child("Name").setValue(name.getText().toString());
                                    ref.child("Address").setValue(address.getText().toString());
                                    ref.child("Profession").setValue(profession.getText().toString());
                                    ref.child("Phone Number").setValue(phoneNumber.getText().toString());
                                    ref.child("Email").setValue(currentUser.getEmail());
                                    Toast.makeText(getApplicationContext(), R.string.toast_ProfileSave, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else { // update data.
                        ref.child("Name").setValue(name.getText().toString());
                        ref.child("Address").setValue(address.getText().toString());
                        ref.child("Profession").setValue(profession.getText().toString());
                        ref.child("Phone Number").setValue(phoneNumber.getText().toString());
                        ref.child("Email").setValue(currentUser.getEmail());
                        Toast.makeText(getApplicationContext(), R.string.toast_dataUpdated, Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    // Add picture to user
    public void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {
        StorageReference storageRef = storageReference.child("images/" + currentUser.getUid());
        storageRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplication(), R.string.toast_uploadSuccess, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplication(), R.string.toast_uploadFailure, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Toast.makeText(getApplication(), "Upload is " + progress + "% done", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void viewProfilePic() {
        try {
            File localFile = File.createTempFile("temp","jpg");
            StorageReference imageRef = storageReference.child("images/"+currentUser.getUid());
            imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    profilePic.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplication(), exception.getMessage(), Toast.LENGTH_LONG).show();
                }});
        } catch (IOException e) {
            Toast.makeText(this, "No profile picture", Toast.LENGTH_LONG).show();
        }
    }

    // Navigation Bar
    public void navigationBar() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayoutProfile);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = findViewById(R.id.navigationProfile);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}