package com.example.q.cs496_week2_new.tabs.Gallery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GalleryClient {
    @GET("/gallery/getState/")
    Call<GalleryState> getStateList();

    @GET("/gallery")
    Call<List<BaseItem>> getSharedList();

    @POST("/gallery")
    Call<GalleryItem> postGalleryList(
    );
}
