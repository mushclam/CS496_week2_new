package com.example.q.cs496_week2_new.tabs.Contact;

import android.net.Uri;

public class ProfilePath {
    private String _id;
    private String profilePath;

    public ProfilePath(String _id, String profilePath) {
        this._id = _id;
        this.profilePath = profilePath;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
