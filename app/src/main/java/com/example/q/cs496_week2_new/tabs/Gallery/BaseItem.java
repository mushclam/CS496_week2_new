package com.example.q.cs496_week2_new.tabs.Gallery;

import android.util.Base64;

public class BaseItem {
    private String _id;
    private String base64;

    public BaseItem() {
    }

    public BaseItem(String _id, String base64) {
        this._id = _id;
        this.base64 = base64;
    }

    public String get_id() {
        return _id;
    }

    public String getBase64() {
        return base64;
    }
}
