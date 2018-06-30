package com.makarand.duet;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    @BindView(R.id.progress_bar) ProgressBar waiter;
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
        toggleBar(true);
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
                        toggleBar(false);
                    }
                });
        }
        else {
            Toast.makeText(getApplicationContext(), "Oups! You forgot to write something.", Toast.LENGTH_LONG).show();
            toggleBar(false);
        }
    }

    private void createProfile(final String uid) {
        User user = new User(getContent(email), getContent(username), uid);
        dbRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/");
        dbRef.setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(SignupActivity.this, WelcomeActivity.class));
                        finish();
                        toggleBar(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Your account was created successfully but profile creation failed. Trying again.", Toast.LENGTH_LONG).show();
                        createProfile(uid);
                    }
                });
    }

    public String getContent(EditText editText){
        return editText.getText().toString().trim();
    }

    public void toggleBar(Boolean bool){
        if(bool)
            waiter.setVisibility(View.VISIBLE);
        else
            waiter.setVisibility(View.GONE);

        waiter.setIndeterminate(bool);
        email.setEnabled(!bool);
        password.setEnabled(!bool);
        username.setEnabled(!bool);
        signupButton.setEnabled(!bool);
    }

}
