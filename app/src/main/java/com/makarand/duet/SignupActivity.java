package com.makarand.duet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.model.User;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.signup_button)
    Button signupButton;
    @BindView(R.id.progress_bar)
    ProgressBar waiter;
    @BindView(R.id.login_link)
    TextView loginLink;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
                //createProfile("booooooo");
            }
        });
    }

    public void signup() {
        toggleInteraction(true);
        if (getContent(email).length() > 0 && getContent(password).length() > 0 && getContent(username).length() > 0) {
            mAuth.createUserWithEmailAndPassword(getContent(email), getContent(password))
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                createProfile(uid);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            toggleInteraction(false);
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "Oups! You forgot to write something.", Toast.LENGTH_LONG).show();
            toggleInteraction(false);
        }
    }


    private void createProfile(String uid) {
        User user = new User(getContent(email), getContent(username), uid);
        db
                .collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignupActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                        /*At this point a user is created new and yet has to complete the initial process.
                        * thus creating a shared preference where new USer will indicate.*/
//                        SharedPreferences localStorage = getSharedPreferences(Constants.sharedPrefName, Context.MODE_PRIVATE);
//                        SharedPreferences.Editor editor = localStorage.edit();
//                        editor.putBoolean("newUser", true);
//                        editor.apply();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this, "Error creating profile.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.toString());
                    }
                });
    }
    /*private void createProfile(final String uid) {
        User user = new User(getContent(email), getContent(username), uid);
        dbRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/");
        dbRef.setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(SignupActivity.this, Splash.class));
                        finish();
                        toggleInteraction(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Your account was created successfully but profile creation failed. Trying again.", Toast.LENGTH_LONG).show();
                        createProfile(uid);
                    }
                });
    }*/

    public String getContent(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void toggleInteraction(boolean show) {
        if (show) {
            waiter.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            waiter.setVisibility(View.GONE);

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}

