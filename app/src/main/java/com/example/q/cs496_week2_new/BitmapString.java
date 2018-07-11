package com.example.q.cs496_week2_new;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class BitmapString {

    public static String bitmapToBase64(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 10, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();

        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String enc){
        byte[] dec = Base64.decode(enc, Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(dec, 0, dec.length);
        return decodedBitmap;
    }
}
