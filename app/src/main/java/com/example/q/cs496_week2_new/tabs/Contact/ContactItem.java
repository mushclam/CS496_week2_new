package com.example.q.cs496_week2_new.tabs.Contact;

public class ContactItem {
    private String _id;
    private String profile;
    private String nickname;
    private String phoneNumber;
    //private String emailAddress;

    public ContactItem(String _id, String profile, String nickname, String phoneNumber) {
        this._id = _id;
        this.profile = profile;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
    }

    public String get_id() {
        return _id;
    }

    public String getProfile() {
        return profile;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    /*
    public String getEmailAddress() {
        return emailAddress;
    }
    */
}
