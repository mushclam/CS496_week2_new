package com.example.q.cs496_week2_new.tabs.Gallery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GalleryClient {
    @GET("/gallery/getState/")
    Call<GalleryState> getStateList();

    @POST("/gallery")
    Call<GalleryItem> postGalleryList(
    );
}
