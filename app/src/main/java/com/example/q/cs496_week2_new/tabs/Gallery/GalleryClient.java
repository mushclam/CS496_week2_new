package com.example.q.cs496_week2_new.tabs.Gallery;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GalleryClient {
    @GET("/gallery/getState/")
    Call<GalleryState> getStateList();

    @POST("/gallery/getImages/")
    Call<List<BaseItem>> getUpdateList(
            @Body List<String> strings
    );

    @GET("/gallery")
    Call<List<BaseItem>> getSharedList();

    @POST("/gallery/postImage")
    Call<String> postGalleryList(
            @Body BaseItem baseItem
    );

    @POST("/gallery/addtocanvas")
    Call<String> addToCanvas(
            @Query("token") String token,
            @Query("image_id") String _id
    );
}
