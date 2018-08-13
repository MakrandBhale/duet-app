package com.makarand.duet.Constants;

import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Constants {

    public static String sharedPrefName = "duetPrefs";
    public static DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms/");
    public static String myUid = null;
    public static String partnerID;
    public static String myChatRoomID = null;
    public static Boolean partnerConnected = false;
    public static String globalChatroomID;
}
