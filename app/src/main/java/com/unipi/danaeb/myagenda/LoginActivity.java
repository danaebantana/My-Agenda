package com.unipi.danaeb.myagenda;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    EditText email, password;
    Button signIn;
    TextView registerHere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.editText_email);
        password = findViewById(R.id.editText_password);
        signIn = findViewById(R.id.button_login);
        registerHere = findViewById(R.id.textView_register);
        registerHere.setPaintFlags(registerHere.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        currentUser = mAuth.getCurrentUser();

        //If user did not sign out, they go straight to the MainActivity. Else they need to sign in.
        if(currentUser!=null){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
        }
    }

    //sign in user using email and password.
    public void login(View view){
        if (TextUtils.isEmpty(email.getText()) || TextUtils.isEmpty(password.getText())){  //Check if textboxes are filled out.
            Toast.makeText(this, R.string.toast_FillBoxes, Toast.LENGTH_LONG).show();
        } else {
            mAuth.signInWithEmailAndPassword(
                    email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                currentUser = mAuth.getCurrentUser();
                                Toast.makeText(getApplicationContext(), R.string.toast_LoginSuccessful, Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void registerHere(View view){
        Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
        startActivity(intent);
    }
}