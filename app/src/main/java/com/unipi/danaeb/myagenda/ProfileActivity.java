package com.unipi.danaeb.myagenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import java.io.File;
import java.io.IOException;
import java.util.UUID;


public class ProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference rootRef, ref;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EditText name, address, profession, phoneNumber;
    Button save;
    ImageView profilePic;
    public Uri imageUri;
    private StorageReference storageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        database = FirebaseDatabase.getInstance();
        rootRef = database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        ref = rootRef.child(currentUser.getUid());
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
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.config_settings);
        mFirebaseRemoteConfig.fetch(3).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mFirebaseRemoteConfig.fetchAndActivate();
            }
        });
        viewProfilePic();
    }

    private void checkForUserInfo(){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            //User info exists.
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

    public void save(View view){
        if (name.getText().equals("") || address.getText().equals("") || profession.getText().equals("") || phoneNumber.getText().equals("")){
            Toast.makeText(this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        } else{
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    //first login of user.
                    if (!snapshot.exists()) {
                        ref.child("Name").setValue(name.getText().toString());
                        ref.child("Address").setValue(address.getText().toString());
                        ref.child("Profession").setValue(profession.getText().toString());
                        ref.child("Phone Number").setValue(phoneNumber.getText().toString());
                        ref.child("Email").setValue(currentUser.getEmail());
                    }
                    else { //update data.
                        ref.child("Name").setValue(name.getText().toString());
                        ref.child("Address").setValue(address.getText().toString());
                        ref.child("Profession").setValue(profession.getText().toString());
                        ref.child("Phone Number").setValue(phoneNumber.getText().toString());
                        ref.child("Email").setValue(currentUser.getEmail());
                    }
                    Toast.makeText(getApplicationContext(), R.string.toast_ProfileSave, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public void choosePicture(){
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

    private void uploadPicture(){

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

    public void viewProfilePic(){
        try {
            String s = mFirebaseRemoteConfig.getString(currentUser.getUid());
            File localFile = File.createTempFile("temp","jpg");
            StorageReference imageRef = storageReference.child("images/"+s);
            imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    profilePic.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                    Toast.makeText(getApplication(), "Success", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    Toast.makeText(getApplication(), "Download is " + progress + "% done", Toast.LENGTH_LONG).show();
                }});
        } catch (IOException e) {
            Toast.makeText(this, "No profile picture", Toast.LENGTH_LONG).show();
        }
    }
}