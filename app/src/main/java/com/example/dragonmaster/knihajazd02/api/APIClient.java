package com.example.dragonmaster.knihajazd02.api;

/**
 * Created by Dragon Master on 16.3.2018.
 */

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    public static final String GOOGLE_PLACE_API_KEY = "AIzaSyApPCrNoPDiB4Pvx3kiapZMQp3NUMqvexI";
    private final ApiInterface mInterface;


    public APIClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).addInterceptor(interceptor).build();

        String base_url = "https://maps.googleapis.com/maps/api/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(base_url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        mInterface = retrofit.create(ApiInterface.class);
    }

    public ApiInterface getInterface() {
        return mInterface;
    }

}