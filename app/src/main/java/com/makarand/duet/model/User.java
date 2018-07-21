package com.makarand.duet.model;

public class User {
    Personal personal;
    Open open;
    public User() {
    }

    public User(String email, String username, String uid){
        personal = new Personal("undef", "undef");
        open = new Open(email, uid, username);
    }

    public Personal getPersonal(){
        return personal;
    }

    public Open getOpen() {
        return open;
    }

    public void setPersonal(Personal personal) {
        this.personal = personal;
    }

    public void setOpen(Open open) {
        this.open = open;
    }
}

class Personal{
    String chatRoom;
    String partner;


    public Personal(){}

    public Personal(String chatRoom, String partner){
        this.chatRoom = chatRoom;
        this.partner = partner;
    }
    public String getChatRoom() {
        return chatRoom;
    }

    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }
}

class Open{
    public Open(){

    }

    public Open(String email, String uid, String username){
        this.email = email;
        this.uid = uid;
        this.username = username;
    }
    String email, uid, username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}