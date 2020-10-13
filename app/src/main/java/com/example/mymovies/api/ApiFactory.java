package com.example.mymovies.api;

import com.google.gson.Gson;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiFactory {
    private static ApiFactory apiFactory;
    private static Retrofit retrofit=null;
    private static final String BASE_URL= "https://api.themoviedb.org/3/";

    public ApiFactory() {
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
    }


    public synchronized static ApiFactory getInstance()
    {
        if (apiFactory==null)
            apiFactory=new ApiFactory();
        return apiFactory;
    }
}
