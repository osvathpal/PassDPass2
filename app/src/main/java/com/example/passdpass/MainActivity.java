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
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public EditText emailId, passwd;
    Button btnSignUp;
    Button btnAllLogIn;
    TextView signIn;
    FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.ETemail);
        passwd = findViewById(R.id.ETpassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnAllLogIn = findViewById(R.id.button2);
        signIn = findViewById(R.id.TVSignIn);

        authStateListener = new FirebaseAuth.AuthStateListener() {//Login checker
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Toast.makeText(MainActivity.this, "User logged in ", Toast.LENGTH_SHORT).show();
                    Intent I = new Intent(MainActivity.this, UserActivity.class);
                    startActivity(I);
                } else {
                    Toast.makeText(MainActivity.this, "Login to continue", Toast.LENGTH_SHORT).show();
                }

            }

        };


        btnSignUp.setOnClickListener(new View.OnClickListener() {

        @Override
            public void onClick(View view){
            String emailID = emailId.getText().toString();
            String paswd = passwd.getText().toString();
            if (emailID.isEmpty()) {
                emailId.setError("Provide your Email first!");
                emailId.requestFocus();
            } else if (paswd.isEmpty()) {
                passwd.setError("Set your password");
                passwd.requestFocus();
            } else if (emailID.isEmpty() && paswd.isEmpty()) {
                Toast.makeText(MainActivity.this, "Fields Empty!", Toast.LENGTH_SHORT).show();
            } else if (!(emailID.isEmpty() && paswd.isEmpty())) {
                firebaseAuth.createUserWithEmailAndPassword(emailID, paswd).addOnCompleteListener(MainActivity.this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this.getApplicationContext(),
                                    "SignUp unsuccessful: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(MainActivity.this, UserActivity.class));
                        }
                    }
                });
            } else {
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
        });

        /*---btnSignUp.setOnClickListener---*/


        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchIntent = new Intent(MainActivity.this, ActivityLogin.class);
                startActivity(switchIntent);
            }
        });
        btnAllLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent switchIntent = new Intent(MainActivity.this, GoogleSignInActivity.class);
                startActivity(switchIntent);
            }
        });

        }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener); //Login checker - --- -
    }

}


