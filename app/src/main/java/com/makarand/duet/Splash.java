package com.makarand.duet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duet.Constants.Constants;

public class Splash extends AppCompatActivity {
    FirebaseAuth mAuth;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseReference personalDbRef;
    String localUid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
//        Initializing the pref
        Constants.myUid = mAuth.getCurrentUser().getUid();
        /*pref contains the shared preferences object.*/
        pref = getApplicationContext().getSharedPreferences("myPref", MODE_PRIVATE);
//        check if data is stored in local stored i.e. shared preferences.
        localUid = pref.getString("myUid", "undef");
        if(localUid.equals("undef")){
            Toast.makeText(getApplicationContext(), "no uid",Toast.LENGTH_LONG).show();
            setupListener();
        }
        else {
            populateLocalStorage();
            startActivity(new Intent(this, MainActivity.class));
        }
    }
    public void populateLocalStorage(){
        Constants.myUid = localUid;
        Constants.myChatRoomID = pref.getString("myChatRoomID", "undef");
        Constants.partnerConnected = pref.getBoolean("partnerConnected", false);
        Constants.partnerID = pref.getString("partnerID", "undef");
    }

    public void setupListener(){
        DatabaseReference myPersonalInfo = FirebaseDatabase.getInstance().getReference("users/"+Constants.myUid+"/personal/");
        myPersonalInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    editor.putString("myChatRoomID", (String) dataSnapshot.child("chatRoom").getValue());
                    editor.putString("partnerID", (String) dataSnapshot.child("partner").getValue());
                    editor.putBoolean("partnerConnected",(Boolean) dataSnapshot.child("partnerConnected").getValue());
                    populateLocalStorage();

                    if ((Boolean) dataSnapshot.child("partnerConnected").getValue()) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                        finish();
                    }
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Error occurred, check your internet connection", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
