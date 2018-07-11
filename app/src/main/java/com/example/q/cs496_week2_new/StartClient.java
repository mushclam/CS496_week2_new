package com.example.q.cs496_week2_new;

import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface StartClient {
    @GET("/login/")
    Call<String> login(@Query("token") String token);

    @Headers("Content-Type: application/json")
    @POST("/register/")
    Call<String> register(@Body String json);

    /*
    @Field("token") String token,
                          @Field("profile") String profile,
                          @Field("nickname") String nickname,
                          @Field("name") String name,
                          @Field("phoneNumber") String phoneNumber
     */

}

