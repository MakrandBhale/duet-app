package com.makarand.duet;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makarand.duet.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {
    @BindView(R.id.email) EditText email;
    @BindView(R.id.password) EditText password;
    @BindView(R.id.username) EditText username;
    @BindView(R.id.login_text) TextView loginLink;
    @BindView(R.id.signup_button) Button signupButton;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

    }

    @OnClick(R.id.signup_button)
    public void signup(Button button){
        if(getContent(email).length() > 0 && getContent(password).length() > 0 && getContent(username).length() > 0){
            mAuth.createUserWithEmailAndPassword(getContent(email), getContent(password))
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String uid = mAuth.getCurrentUser().getUid();
                            createProfile(uid);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        }
        else {
            Toast.makeText(getApplicationContext(), "Oups! You forgot to write something.", Toast.LENGTH_LONG).show();
        }
    }

    private void createProfile(String uid) {
        User user = new User(getContent(email), getContent(username), uid);
        dbRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/");
        if(dbRef.setValue(user).isSuccessful()){
            Toast.makeText(getApplicationContext(), "Welcome!",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(), "Your account was created successfully. However Profile Creation wasn't successful. Try again.",Toast.LENGTH_LONG).show();
            createProfile(uid);
        }
    }

    public String getContent(EditText editText){
        return editText.getText().toString().trim();
    }
}

