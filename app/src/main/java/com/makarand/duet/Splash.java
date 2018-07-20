package com.makarand.duet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.SharedPrefs.SharedPrefs;
import com.makarand.duet.model.ChatroomOpen;

public class Splash extends AppCompatActivity {
    FirebaseAuth mAuth;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    DatabaseReference myPersonalInfo;
    SharedPrefs sharedPrefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            /*Check if user is logged in.*/
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            /* User is logged in and get uid, write to the the Constants.*/
            Constants.myUid = mAuth.getCurrentUser().getUid();
            myPersonalInfo = FirebaseDatabase.getInstance().getReference("users/"+Constants.myUid+"/personal/");
            /*sharedPreferences contains the shared preferences object.*/
            sharedPreferences = getApplicationContext().getSharedPreferences("duet_prefs", MODE_PRIVATE);
            /* Even if the data is not available sharedPrefs have 'default' value, which in this case is set to
            * "undef" which means calling initDefaultConstants() will populate the Constants with the data available in the
            * SharedPrefs, if there isn't any data, then Constants will contain 'undef' value.
            * This method will save calling SharedPrefs every time we require the sharedPrefs. */
            initDefaultConstants();

            /* if the sharedPrefs OR locally stored data is not "undef" the check if all the criteria
            (i.e. if partner is connected and partner has ID) are met if yes then proceed to the MainActivity
            * or proceed to grabbing the info*/
            if(Constants.partnerConnected && !Constants.partnerID.equals("undef") && !Constants.myChatRoomID.equals("undef")){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else {
                setupPersonalInfoListener();
            }
            /* a chatroom listener is used to check if any changes occur in the chatroom tree.
            * for ex. if one partner decides to bail out, that will effectively write "undef" and this
            * listener will make sure that other partner will be redirected to the Welcome activity.*/
            setupChatRoomListener();
        }
    }
    public void initDefaultConstants(){
        Log.i("littleSteps", "updating constants in splash screen");
        /*this method is used for getting data stored in shared preferences.
        * sharedPrefs are populated when a user signed in. else they are empty*/
        Constants.myChatRoomID = sharedPreferences.getString("myChatRoomID", "undef");
        Constants.partnerConnected = sharedPreferences.getBoolean("partnerConnected", false);
        Constants.partnerID = sharedPreferences.getString("partnerID", "undef");
    }

    public void storeUserInfoToLocalStorage(String myUid, String myChatRoomID, String partnerID, Boolean partnerConnected){
        Log.i("littleSteps", "Updating SharedPrefs");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("duet_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("myUid", myUid);
        editor.putString("myChatRoomID", myChatRoomID);
        editor.putString("partnerID", partnerID);
        editor.putBoolean("partnerConnected", partnerConnected);
        editor.apply();
    }

    public void setupChatRoomListener(){
        /* A listener for chatrooms.*/
        if(Constants.myChatRoomID.equals("undef"))
            // if the myChatRoomID returns "undef" then sharedPrefs are not yet initialised. Fall Back.
            return;
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms/"+ Constants.myChatRoomID + "/open");
        Log.i("littleSteps", "checking the chatroom has both participants");
        chatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatroomOpen open = dataSnapshot.getValue(ChatroomOpen.class);
                if(open.getP1().equals("undef") || open.getP2().equals("undef")){
                    Log.i("littleSteps", "one of the partner is missing, changing personal info tree.");
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                    myPersonalInfo.child("partner").setValue("undef");
                    myPersonalInfo.child("partnerConnected").setValue(false);
                    storeUserInfoToLocalStorage(Constants.myUid, Constants.myChatRoomID, "undef", false);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void setupPersonalInfoListener(){
        /*A listener for the personal info tree.*/

        myPersonalInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                /*Sometimes it might possible that the user is registered but without the
                * information part in the "Users" tree. So to avoid any null pointer exceptions
                * check if the snapshot is null, if it is null then populate the info tree with a "undef" value
                * this "undef" value is just arbitrary standard for empty data. */

                if(!dataSnapshot.child("chatRoom").exists() && !dataSnapshot.child("partnerConnected").exists() && !dataSnapshot.child("partner").exists())
                {
                    Toast.makeText(getApplicationContext(), "There is no information associated with your account. Please try creating new account with another email.", Toast.LENGTH_LONG).show();
//                    createProfile(Constants.myUid);
                }
                try {
                    /*editor is reference to the sharedPrefs. if sharedPrefs are not available then they are populated in here.
                    * THis should occur mostly when users uses a new device or uninstalls and re-installs the application.
                    * So get data form firebase and setup sharedPrefs.*/
                    editor = getSharedPreferences("duet_prefs", MODE_PRIVATE).edit();
                    editor.putString("myChatRoomID", (String) dataSnapshot.child("chatRoom").getValue());
                    editor.putString("partnerID", (String) dataSnapshot.child("partner").getValue());
                    editor.putBoolean("partnerConnected", (Boolean) dataSnapshot.child("partnerConnected").getValue());
                    editor.apply();
                    /*Once the data is into sharedPrefs, also write it to the Constants. which are accessible throughout the program life.*/
                    initDefaultConstants();

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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("LittleSteps", e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//    private void createProfile(final String uid) {
//        User user = new User(getContent(email), getContent(username), uid);
//        dbRef = FirebaseDatabase.getInstance().getReference("users/" + uid + "/");
//        dbRef.setValue(user)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        startActivity(new Intent(SignupActivity.this, WelcomeActivity.class));
//                        finish();
//                        toggleBar(false);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(getApplicationContext(), "Your account was created successfully but profile creation failed. Trying again.", Toast.LENGTH_LONG).show();
//                        createProfile(uid);
//                    }
//                });
//    }
}
