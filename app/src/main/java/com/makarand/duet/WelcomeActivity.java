package com.makarand.duet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.model.ChatroomOpen;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WelcomeActivity extends AppCompatActivity {
    /*
    * The main reason for this activity to exist is to get a
    * common chatroom for both the partners.
    * one of them can register on app and select that they don't
    * have ID. Now the firebase will give ID and we will request it to share
    * it with the partner.
    * Now the partner will select that he/she has the ID and insert it into the edittext.
    * connect.
    *
    * TIP: Following code is total VISIBILITY disaster. Enter at your own risk.
    * I dare you to try tweaking it without any UI support :P
    *
    * Don't change it I repeat don't change it.
    * If you really want to change the way it works, replace this activity completely.
    * */
    @BindView(R.id.step_one) LinearLayout stepOne;
    @BindView(R.id.step_two) LinearLayout stepTwo;
    @BindView(R.id.withID) LinearLayout withID;
    @BindView(R.id.withoutID) LinearLayout withoutID;
    @BindView(R.id.wait_page) LinearLayout waitPage;
    @BindView(R.id.back_button) ImageButton backButton;
    @BindView(R.id.newID) EditText newID;
    @BindView(R.id.try_again) Button tryAgain;
    @BindView(R.id.copy_button) Button copyButton;
    @BindView(R.id.waiter) ProgressBar waiter;
    @BindView(R.id.connect_button) Button connectButton;
    @BindView(R.id.have_id_edittext) EditText haveIDEdittext;
    @BindView(R.id.next_button) Button nextButton;
    //    chatroom is global reference to the chatroom. openChatroomRef is the respective chatroom public info branch.
    DatabaseReference openChatroomRef, personalChatroomRef, myPersonalInfoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
//        try {
//            myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        }
        /*This function tries to get the push ID from the firebase
         * this myChatRoomID effectively reflects to the chatrooms*/
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Constants.myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.i("littleSteps", "UserLogged in:" + Constants.myUid);
            waiter.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
            myPersonalInfoRef = FirebaseDatabase.getInstance().getReference("users/"+ Constants.myUid+"/personal");
            myPersonalInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("littleSteps", "Checking personal info tree.");
                    Constants.myChatRoomID = (String) dataSnapshot.child("chatRoom").getValue();
                    Constants.partnerID = (String) dataSnapshot.child("partner").getValue();
                    if(Constants.myChatRoomID == null || Constants.myChatRoomID.equals("undef")) {
                        Constants.myChatRoomID = Constants.chatRoomRef.push().getKey();
                        Log.i("littleSteps", "myChatRoomID is either null or undef so creating new.");
                    }
                    newID.setText(Constants.myChatRoomID);
                    Log.i("littleSteps", "myChatRoomID: "+ Constants.myChatRoomID);
                    personalChatroomRef = FirebaseDatabase.getInstance().getReference("users/"+ Constants.myUid +"/personal/chatRoom/");
                    openChatroomRef = FirebaseDatabase.getInstance().getReference("chatrooms/"+ Constants.myChatRoomID +"/open/");
                    setupListenerForPartner();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
        }
    }
    private void moveOn(){
        Log.i("littleSteps", "partner is connected, all good to go. Populating sharedPrefs.");
        Constants.partnerConnected = true;
        storeUserInfoToLocalStorage();
        myPersonalInfoRef.child("partnerConnected").setValue(true);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    public void storeUserInfoToLocalStorage(){
        Log.i("littleSteps", "Updating SharedPrefs");
        SharedPreferences pref = getApplicationContext().getSharedPreferences("duet_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("myUid", Constants.myUid);
        editor.putString("myChatRoomID", Constants.myChatRoomID);
        editor.putString("partnerID", Constants.partnerID);
        editor.putBoolean("partnerConnected", Constants.partnerConnected);
        editor.apply();
    }

    public void updateConstants(DataSnapshot data){
        Log.i("littleSteps", "Updating Constants");
        DatabaseReference personalChatRoom = FirebaseDatabase.getInstance().getReference("users/"+Constants.myUid+"/personal");
        personalChatRoom.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Constants.myChatRoomID = (String) dataSnapshot.child("chatRoom").getValue();
                Constants.partnerID = (String) dataSnapshot.child("partner").getValue();
                Constants.partnerConnected = (Boolean) dataSnapshot.child("partnerConnected").getValue();
                Log.i("littleSteps", "Constants updated now updating sharedPrefs");
                storeUserInfoToLocalStorage();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setupListenerForPartner() {
        /*A listener established when the partner accepts the request. */
        Log.i("littleSteps", "setting up listener");
        DatabaseReference partnerListener = FirebaseDatabase.getInstance().getReference("chatrooms/"+Constants.myChatRoomID+"/open/");
        partnerListener.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("littleSteps", "Data changed in firebase chatroom info tree");
                updateConstants(dataSnapshot);
                final ChatroomOpen chatroomOpen = dataSnapshot.getValue(ChatroomOpen.class);
                Log.i("littleSteps", "received chatroom details from firebase.");
                try {
                    final String p2 = chatroomOpen.getP2();
                    Log.i("littleSteps", "Partner is: " + p2);
                    if((p2.equals(Constants.myUid)) || (p2.equals("undef"))){
                        Log.i("littleSteps", "partner is not defined.");
                        stepOne.setVisibility(View.GONE);
                        waitPage.setVisibility(View.VISIBLE);
                    }
                    else {
                        Log.i("littleSteps", "partner id is valid & is !null");
                        myPersonalInfoRef.child("partner").setValue(p2)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("littleSteps", "successfully added the partner to personal info tree.");

                                        /*TODO : show the notification is partner accepted the request.*/
                                        DatabaseReference partnerInfo = FirebaseDatabase.getInstance().getReference("users/"+p2+"/open");
                                        Log.i("littleSteps", "getting partner info.");

                                        partnerInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Log.i("littleSteps", "partner connected.");
                                                Constants.partnerConnected = true;
                                                Toast.makeText(getApplicationContext(), "Congratulations! "+dataSnapshot.child("username").getValue() +" is on board.", Toast.LENGTH_LONG).show();
                                                moveOn();
                                                waiter.setVisibility(View.GONE);
                                                nextButton.setVisibility(View.VISIBLE);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Log.e("databaseError", databaseError.getDetails());
                                                waiter.setVisibility(View.GONE);
                                                nextButton.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
                    }

                }
                catch (Exception e){
                    //Toast.makeText(getApplicationContext(), "Error occurred, try again later.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    waiter.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("databaseError", databaseError.getDetails());
            }
        });
    }

    @OnClick (R.id.connect_button)
    public void connect(Button button){
        if(haveIDEdittext.getText().toString().trim().length() > 0){
            String id = haveIDEdittext.getText().toString();
            final DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms/"+id);

            chatRoomRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("open")){
                        /*Now this here is fairly complicated thing will explain one by one.
                        * the connect feature is accessible if your partner is already on duet. which means they have the chatroom id
                        * now when you insert it into the edit text get it from there and update the
                        * chatroom member list item i.e. p2 as p1 is already your partner.
                        * also update the partner key in your own personal user info branch.
                        * upon updation of the your local info your partner will update it automatically.
                        *
                        * Also write the chatRoom for ourselves*/
                        String partnerID = (String) dataSnapshot.child("open").child("p1").getValue();
                        chatRoomRef.child("open").child("p2").setValue(Constants.myUid);
                        myPersonalInfoRef.child("partner").setValue(partnerID);
                        myPersonalInfoRef.child("chatRoom").setValue(Constants.myChatRoomID);
                        moveOn();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    @OnClick (R.id.copy_button)
    public void copy(){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("chatRoomAddress", Constants.myChatRoomID);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), "ID Copied", Toast.LENGTH_SHORT).show();
    }
    public void createRoom(){
        waiter.setVisibility(View.VISIBLE);
        /*chatrooms contain all the chatrooms of all couples
         * the current chatroom is denoted by the ID String
         * while the myUid denotes the current user.
         * When a user registers he/she will get a new ID and a public branch will be created
         * under their couple chatroom.*/
        /*setting chatroom myChatRoomID in to user info branch*/
        ChatroomOpen op = new ChatroomOpen(Constants.myUid, "undef");
        /*the p1 refers to the current person, while p2 refers to the partner*/
        openChatroomRef.setValue(op)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        personalChatroomRef.setValue(Constants.myChatRoomID)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    copyButton.setVisibility(View.VISIBLE);
                                    waiter.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "All done!", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waiter.setVisibility(View.GONE);
                        e.printStackTrace();
                        tryAgain.setVisibility(View.VISIBLE);
                        copyButton.setVisibility(View.GONE);
                    }
                });
    }
    @OnClick({R.id.yes_button, R.id.no_button})
    public void stepper(Button button){
        backButton.setVisibility(View.VISIBLE);
        switch (button.getId()){
            case R.id.yes_button:
                stepTwo.setVisibility(View.GONE);
                withID.setVisibility(View.VISIBLE);
                break;
            case R.id.no_button:
                stepTwo.setVisibility(View.GONE);
                withoutID.setVisibility(View.VISIBLE);
                createRoom();
                break;
        }
    }

    @OnClick(R.id.try_again)
    public void tryAgain(){
//        call the setID function which sets the myChatRoomID to the edit Text.
        createRoom();
    }

    @OnClick(R.id.next_button)
    public void next(final Button button){
        if(Constants.partnerConnected){
            moveOn();
        }

        button.setVisibility(View.GONE);
        waiter.setVisibility(View.VISIBLE);
        personalChatroomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String val = (String) dataSnapshot.getValue();
                /*TODO : while in the personal info tree, make sure to validate the chatroom field in the firebase rule*/
                if(val == null){
                    Log.e("chatroom id deleted", "thus the error");
                }
                else if(!val.equals("undef")){
                    /*if chatroom Constants.myChatRoomID is not undef that means the users has been here already. Then just show the waiting page*/
                    stepOne.setVisibility(View.GONE);
                    waiter.setVisibility(View.GONE);
                    withoutID.setVisibility(View.VISIBLE);
//                    set the Constants.myChatRoomID to the previously generated value
                    Constants.myChatRoomID = val;
                    newID.setText(val);
                    copyButton.setVisibility(View.VISIBLE);
                }
                else {
                    /*if the chatroom is undef that means the users did not previously created the chatroom Constants.myChatRoomID*/
                    stepOne.setVisibility(View.GONE);
                    stepTwo.setVisibility(View.VISIBLE);
                    waiter.setVisibility(View.GONE);
                    withoutID.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @OnClick(R.id.back_button)
    public void back(ImageButton button){
        button.setVisibility(View.GONE);
        /*when user changes his mind and clicks back to select another option
        * we don't want the empty tree to be orphaned thus eliminate it.
        * Its done in following line.
        * also removing the chatroom Constants.myChatRoomID stored in the user private info tree*/
        openChatroomRef.setValue(null);
        personalChatroomRef.setValue(null);
        if(stepTwo.getVisibility() == View.GONE)
            stepTwo.setVisibility(View.VISIBLE);

        if(withID.getVisibility() == View.VISIBLE)
            withID.setVisibility(View.GONE);

        if(withoutID.getVisibility() == View.VISIBLE)
            withoutID.setVisibility(View.GONE);
    }
}
