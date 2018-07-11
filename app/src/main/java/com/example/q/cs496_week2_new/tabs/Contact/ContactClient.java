package com.example.q.cs496_week2_new.tabs.Contact;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ContactClient {
    @GET("/contact/")
    Call<List<ContactItem>> getContactLIst(@Query("token") String token);
}
