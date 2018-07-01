package com.makarand.duet;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duet.Constants.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.email) EditText email;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.signup_link) TextView signupLink;
    @BindView(R.id.login_button) Button loginButton;
    @BindView(R.id.waiter) ProgressBar waiter;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setTitle(null);
        loginButton = findViewById(R.id.login_button);
        signupLink = findViewById(R.id.signup_link);
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
            }
        });
    }

    @OnClick(R.id.login_button)
    void login(){
        waiter.setVisibility(View.VISIBLE);
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        if(emailText.length() > 0 && passwordText.length() > 0){
            mAuth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_LONG).show();
                                waiter.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),"Error: " + e.getMessage()  ,Toast.LENGTH_LONG).show();
                            Log.e("Login error", e.getMessage());
                            waiter.setVisibility(View.GONE);
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "Oups! You forgot to write something.", Toast.LENGTH_LONG).show();
        }
    }
}
