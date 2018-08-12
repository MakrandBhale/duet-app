package com.makarand.duet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.makarand.duet.Constants.Constants;

public class Splash extends AppCompatActivity {
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            Constants.myUid = auth.getCurrentUser().getUid();
            if(userConnected()) {
                startActivity(new Intent(this, WelcomeActivity.class));
                finish();
            }
            else {
                /*User is returning and have established the chatroom proceed to MainActivity*/

                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
        else {
            /*User isn't logged in*/
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    private boolean userConnected(){
        /*This function will check if the user has successfully established the chatroom and the partner is connected.*/
        return true;
    }

}
