package com.example.q.cs496_week2_new;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceGenerator {

    private static final String BASE_URL = "http://52.231.69.137:8080";
//    private static final String BASE_URL = "http://52.231.66.99:8000";
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit retrofit =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

    public static <S> S createService(
            Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
