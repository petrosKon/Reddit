package com.example.kontr.redditapp;

import com.example.kontr.redditapp.Account.CheckLogin;
import com.example.kontr.redditapp.model.Feed;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FeedApi {

    @GET("{feed_name}/.rss")
    Call<Feed> getFeed(@Path("feed_name") String feed_name);


    @POST("{user}")
    Call<CheckLogin> signIn(
            @HeaderMap Map<String,String> headers,
            @Path("user") String username,
            @Query("user") String user,
            @Query("passwd") String password,
            @Query("api_type") String type);

}
