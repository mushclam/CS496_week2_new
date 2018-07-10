package com.example.q.cs496_week2_new.tabs.Gallery;

import java.io.File;

public class SharedItem {
    private String _id;
    private String imagePath;

    public SharedItem(String _id, String imagePath) {
        this._id = _id;
        this.imagePath = imagePath;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public File getFile() {
        return new File(imagePath);
    }
}
