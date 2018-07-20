package com.makarand.duet.SharedPrefs;

import android.content.SharedPreferences;

import com.makarand.duet.Constants.Constants;

public class SharedPrefs {
    private String myUid, myChatRoomID, partnerID;
    private Boolean partnerConnected;

    public SharedPrefs(){}

    public SharedPrefs(String myUid, String myChatRoomID, String partnerID, Boolean partnerConnected) {
        this.myUid = myUid;
        this.myChatRoomID = myChatRoomID;
        this.partnerID = partnerID;
        this.partnerConnected = partnerConnected;
    }

    public void setAllPrefs(String myUid, String myChatRoomID, String partnerID, Boolean partnerConnected, SharedPreferences.Editor editor){
        this.myUid = myUid;
        this.myChatRoomID = myChatRoomID;
        this.partnerID = partnerID;
        this.partnerConnected = partnerConnected;

        editor.putString("myUid", myUid);
        editor.putString("myChatRoomID", myChatRoomID);
        editor.putString("partnerID", partnerID);
        editor.putBoolean("partnerConnected", partnerConnected);
        editor.apply();
    }

    public String getMyUid() {
        return myUid;
    }

    public void setMyUid(String myUid, SharedPreferences.Editor editor) {
        this.myUid = myUid;
        editor.putString("myUid", myUid);
        editor.apply();

    }

    public String getMyChatRoomID() {
        return myChatRoomID;
    }

    public void setMyChatRoomID(String myChatRoomID, SharedPreferences.Editor editor) {
        this.myChatRoomID = myChatRoomID;
        editor.putString("myChatRoomID", myChatRoomID);
        editor.apply();

    }

    public String getPartnerID() {
        return partnerID;
    }

    public void setPartnerID(String partnerID, SharedPreferences.Editor editor) {
        this.partnerID = partnerID;
        editor.putString("partnerID", partnerID);
        editor.apply();

    }

    public Boolean getPartnerConnected() {
        return partnerConnected;
    }

    public void setPartnerConnected(Boolean partnerConnected, SharedPreferences.Editor editor) {
        this.partnerConnected = partnerConnected;
        editor.putBoolean("partnerConnected", partnerConnected);
        editor.apply();

    }
}
