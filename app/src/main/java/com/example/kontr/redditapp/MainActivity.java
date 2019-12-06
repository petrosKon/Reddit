package com.example.kontr.redditapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.kontr.redditapp.model.Feed;
import com.example.kontr.redditapp.model.entry.Entry;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String BASE_URL = "https://www.reddit.com/r/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedApi feedApi = retrofit.create(FeedApi.class);

        Call<Feed> call = feedApi.getFeed();

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                Log.d(TAG,response.toString());

                List<Entry> entries = response.body().getEntrys();

                Log.d(TAG, String.valueOf(response.body().getEntrys()));

                Log.d("Author",entries.get(0).getAuthor().getName());

                Log.d("Updated",entries.get(0).getUpdated());

                Log.d("Title",entries.get(0).getTitle());

            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, t.getMessage());

            }
        });

    }
}
