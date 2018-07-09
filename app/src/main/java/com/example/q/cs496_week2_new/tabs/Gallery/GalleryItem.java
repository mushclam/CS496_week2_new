package com.example.q.cs496_week2_new.tabs.Gallery;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Date;

public class GalleryItem implements Parcelable, Comparable{
    String filePath;
    long dateTaken;
    float longitude;
    float latitude;

    public GalleryItem(String filePath, long dateTaken, float longitude, float latitude) {
        this.filePath = filePath;
        this.dateTaken = dateTaken;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected GalleryItem(Parcel in) {
        filePath = in.readString();
        dateTaken = in.readLong();
        longitude = in.readFloat();
        latitude = in.readFloat();
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel parcel) {
            return new GalleryItem(parcel);
        }

        @Override
        public GalleryItem[] newArray(int i) {
            return new GalleryItem[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(filePath);
        parcel.writeLong(dateTaken);
        parcel.writeFloat(longitude);
        parcel.writeFloat(latitude);
    }

    public String getFilePath() {
        return filePath;
    }

    public long getDateTaken() {
        return dateTaken;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getFileName() {
        String[] temp = filePath.split("/");
        return temp[temp.length - 1];
    }

    public File getFile() {
        return new File(filePath);
    }

    public String getDir() {
        String dir = filePath.substring(0, filePath.lastIndexOf('/'));
        return dir;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        Date mDate = new Date(this.getDateTaken());
        Date compareDate = new Date(((GalleryItem)o).getDateTaken());
        return mDate.compareTo(compareDate);
    }
}
