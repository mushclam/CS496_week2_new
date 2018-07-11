package com.example.q.cs496_week2_new.tabs.Canvas;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface CanvasClient {
    @GET("/canvas/getOpenList/")
    Call<List<String>> getOpenList();

    @GET("/canvas/register/")
    Call<List<String>> registerImage(@Query("image_id") String image_id);

}
