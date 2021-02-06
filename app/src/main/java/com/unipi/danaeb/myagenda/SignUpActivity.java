package com.unipi.danaeb.myagenda;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EditText email, password1, password2;
    Button signUp;
    FloatingActionButton back_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.editText_emailAddress);
        password1 = findViewById(R.id.editText_password1);
        password2 = findViewById(R.id.editText_password2);
        signUp = findViewById(R.id.button_signUp);
        back_bt = findViewById(R.id.back_bt);

        back_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

    }

    //Sign up new user to firebase, while checking the activities condition.
    public void signUp(View view){
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password1.getText()) || TextUtils.isEmpty(password2.getText())){
            Toast.makeText(this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        }
        else if (!password1.getText().toString().equals(password2.getText().toString())){  //Different Passwords.
            password1.setText("");
            password2.setText("");
            Toast.makeText(this, R.string.toast_Password, Toast.LENGTH_LONG).show();
        }
        else if (password1.getText().toString().equals(password2.getText().toString())){
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), password1.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),R.string.toast_SignUpSuccessful,Toast.LENGTH_LONG).show();
                                currentUser = mAuth.getCurrentUser();
                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }
}