package com.example.passdpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;

public class blank extends AppCompatActivity {

    public EditText loginEmailId, logInpasswd;
    Button btnLogIn;
    TextView signup;
    FirebaseAuth firebaseAuth;
    private AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        firebaseAuth = FirebaseAuth.getInstance();
        loginEmailId = findViewById(R.id.username);
        logInpasswd = findViewById(R.id.password);
        btnLogIn = findViewById(R.id.login);
        signup = findViewById(R.id.TVSignIn);
        authStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(blank.this, "User logged in ", Toast.LENGTH_SHORT).show();
                    Intent I = new Intent(blank.this, UserActivity.class);
                    startActivity(I);
                } else {
                    Toast.makeText(blank.this, "Login to continue", Toast.LENGTH_SHORT).show();
                }
            }
        };


    }
}
