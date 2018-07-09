package com.example.q.cs496_week2_new.tabs.Gallery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GalleryClient {
    @GET("/gallery")
    Call<List<GalleryItem>> getGalleryList();

    @POST("/gallery")
    Call<List<GalleryItem>> postGalleryList();
}
