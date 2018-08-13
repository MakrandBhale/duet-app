package com.makarand.duet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.FragmentContainer.WelcomeMessage;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            Constants.myUid = auth.getCurrentUser().getUid();
            if(!userConnected()) {
                startActivity(new Intent(this, WelcomeActivity.class));
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
        SharedPreferences preferences = getSharedPreferences(Constants.sharedPrefName, MODE_PRIVATE);
        Constants.globalChatroomID = preferences.getString("chatroom", "undef");
        String partner = preferences.getString("partner", "undef");
        //return true;
        return !Constants.globalChatroomID.equals("undef") && !partner.equals("undef");
    }
}
