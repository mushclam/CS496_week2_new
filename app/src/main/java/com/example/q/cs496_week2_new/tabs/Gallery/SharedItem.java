package com.example.q.cs496_week2_new.tabs.Gallery;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

public class SharedItem implements Parcelable {
    private String _id;
    private String imagePath;

    public SharedItem(String _id, String imagePath) {
        this._id = _id;
        this.imagePath = imagePath;
    }

    protected SharedItem(Parcel in) {
        _id = in.readString();
        imagePath = in.readString();
    }

    public static final Creator<SharedItem> CREATOR = new Creator<SharedItem>() {
        @Override
        public SharedItem createFromParcel(Parcel parcel) {
            return new SharedItem(parcel);
        }

        @Override
        public SharedItem[] newArray(int i) {
            return new SharedItem[i];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(_id);
        parcel.writeString(imagePath);
    }
}
