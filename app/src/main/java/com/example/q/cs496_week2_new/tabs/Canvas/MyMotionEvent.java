package com.example.q.cs496_week2_new.tabs.Canvas;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.util.ArrayList;

// only store "action_move"s
public class MyMotionEvent {
    private ArrayList<JSONObject> jsons = null;

    MyMotionEvent(JSONArray jsons){
        ArrayList<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsons.length(); ++i){
            JSONObject obj = new JSONObject();
            try {
                obj = (JSONObject) jsons.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (obj.has("x") && obj.has("y") && obj.has("id")) {
                list.add(obj);
            }
        }
        this.jsons = list;
    }

    float getX (int i){
        if (this.jsons != null) {
            if (i < this.jsons.size()) {
                JSONObject obj = this.jsons.get(i);
                return BigDecimal.valueOf(obj.optDouble("x")).floatValue();
            }
            else {
                Log.d("event test", "getX : given index " + String.valueOf(i) + " is too big");
            }
        }
        return -1;
    }

    float getY (int i){
        if (this.jsons != null) {
            if (i < this.jsons.size()) {
                JSONObject obj = this.jsons.get(i);
                return BigDecimal.valueOf(obj.optDouble("y")).floatValue();
            }
            else {
                Log.d("event test", "getY : given index " + String.valueOf(i) + " is too big");
            }
        }
        return -1;
    }

    int getPointerCount() {
        return this.jsons != null ? this.jsons.size() : 0;
    }

    int getPointerId(int i){
        if (this.jsons != null) {
            if (i < this.jsons.size()) {
                JSONObject obj = this.jsons.get(i);
                return obj.optInt("id");
            }
            else {
                Log.d("event test", "getPointerId : given index " + String.valueOf(i) + " is too big");
            }
        }
        return -1;
    }
}
