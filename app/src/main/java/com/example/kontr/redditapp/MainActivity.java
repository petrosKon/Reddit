package com.example.kontr.redditapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.kontr.redditapp.model.Feed;
import com.example.kontr.redditapp.model.entry.Entry;

import java.util.ArrayList;
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

                ArrayList<Post> posts = new ArrayList<>();

                for(int i = 0; i < entries.size(); i++) {

                    ExtractXML extractXML1 = new ExtractXML("<a href=", entries.get(0).getContent());
                    List<String> postContent = extractXML1.start();

                    ExtractXML extractXML2 = new ExtractXML("<img src=", entries.get(0).getContent());

                    try{
                        postContent.add(extractXML2.start().get(0));

                    }catch (NullPointerException e){

                        postContent.add(null);
                        e.printStackTrace();

                    }catch (IndexOutOfBoundsException e){

                        postContent.add(null);
                        e.printStackTrace();

                    }

                    int lastPosition = postContent.size() - 1;
                    posts.add(new Post(entries.get(i).getTitle(),
                            entries.get(i).getAuthor().getName(),
                            entries.get(i).getUpdated(),
                            postContent.get(0),
                            postContent.get(lastPosition)
                    ));

                }

                for(int j = 0; j < posts.size(); j++){

                    Log.d(TAG,"PostURL: " + posts.get(j).getPostURL() + "ThumbnailURL" + posts.get(j).getThumbnailURL());
                }

            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, t.getMessage());

            }
        });

    }
}
