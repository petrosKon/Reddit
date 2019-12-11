package com.example.kontr.redditapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.kontr.redditapp.Comments.CommentsActivity;
import com.example.kontr.redditapp.model.Feed;
import com.example.kontr.redditapp.model.entry.Entry;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.example.kontr.redditapp.URLS.BASE_URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Button btnRefreshFeed;
    private EditText mFeedName;
    private String currentFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRefreshFeed = findViewById(R.id.btnRefreshFeed);
        mFeedName = findViewById(R.id.etFeedName);


        init();

        btnRefreshFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedName = mFeedName.getText().toString();
                if(!feedName.equals("")){
                    currentFeed = feedName;
                    init();
                } else {
                    init();
                }
            }
        });


    }

    private void init() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();

        FeedApi feedApi = retrofit.create(FeedApi.class);

        Call<Feed> call = feedApi.getFeed(currentFeed);

        call.enqueue(new Callback<Feed>() {
            @Override
            public void onResponse(Call<Feed> call, Response<Feed> response) {
                Log.d(TAG,response.toString());

                List<Entry> entries = response.body().getEntrys();

                Log.d(TAG, String.valueOf(response.body().getEntrys()));

                Log.d("Author",entries.get(0).getAuthor().getName());

                Log.d("Updated",entries.get(0).getUpdated());

                Log.d("Title",entries.get(0).getTitle());

                final ArrayList<Post> posts = new ArrayList<>();

                for(int i = 0; i < entries.size(); i++) {

                    ExtractXML extractXML1 = new ExtractXML("<a href=", entries.get(i).getContent());
                    List<String> postContent = extractXML1.start();

                    ExtractXML extractXML2 = new ExtractXML("<img src=", entries.get(i).getContent());

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
                    try{

                        posts.add(new Post(entries.get(i).getTitle(),
                                entries.get(i).getAuthor().getName(),
                                entries.get(i).getUpdated(),
                                postContent.get(0),
                                postContent.get(lastPosition)
                        ));

                    }catch (NullPointerException e){
                        e.printStackTrace();

                        posts.add(new Post(entries.get(i).getTitle(),
                                "None",
                                entries.get(i).getUpdated(),
                                postContent.get(0),
                                postContent.get(lastPosition)
                        ));
                    }


                }

                /*for(int j = 0; j < posts.size(); j++){

                    Log.d(TAG,"PostURL: " + posts.get(j).getPostURL()
                            + "\n" + "ThumbnailURL: " + posts.get(j).getThumbnailURL()
                            + "\n" + "Title: " + posts.get(j).getTitle()
                            + "\n" + "Author: " + posts.get(j).getAuthor()
                            + "\n" + "Date updated: " + posts.get(j).getDateUpdated());
                }*/

                ListView listView = findViewById(R.id.listView);
                CustomListAdapter customListAdapter = new CustomListAdapter(MainActivity.this,R.layout.card_layout_main,posts);
                listView.setAdapter(customListAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(MainActivity.this,CommentsActivity.class);
                        intent.putExtra("@string/post_url",posts.get(position).getPostURL());
                        intent.putExtra("@string/post_thumbnail",posts.get(position).getThumbnailURL());
                        intent.putExtra("@string/post_author",posts.get(position).getAuthor());
                        intent.putExtra("@string/post_title",posts.get(position).getTitle());
                        intent.putExtra("@string/post_updated",posts.get(position).getDateUpdated());
                        startActivity(intent);

                    }
                });
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                Log.e(TAG, t.getMessage());

            }
        });

    }
}
