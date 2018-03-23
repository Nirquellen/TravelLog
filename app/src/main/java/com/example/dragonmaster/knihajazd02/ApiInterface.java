package com.example.dragonmaster.knihajazd02;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ApiInterface {

    @GET("distancematrix/json")
    Call<ResultDistanceMatrix> getDistance(@QueryMap Map<String, String> params);
}
