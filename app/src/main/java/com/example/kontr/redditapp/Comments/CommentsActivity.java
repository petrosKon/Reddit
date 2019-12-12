package com.example.kontr.redditapp.Comments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.kontr.redditapp.ExtractXML;
import com.example.kontr.redditapp.FeedApi;
import com.example.kontr.redditapp.R;
import com.example.kontr.redditapp.WebViewActivity;
import com.example.kontr.redditapp.model.Feed;
import com.example.kontr.redditapp.model.entry.Entry;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.example.kontr.redditapp.URLS.BASE_URL;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private static String postTitle;
    private static String postAuthor;
    private static String postUpdated;
    private static String postURL;
    private static String postThumbnailURL;

    private int defaultImage;

    private String currentFeed;

    private ArrayList<Comment> mComments;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private TextView progressText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        mProgressBar = findViewById(R.id.commentsLoadingProgressBar);
        progressText = findViewById(R.id.progressText);
        mProgressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);

        setupImageLoader();

        initPost();
        
        init();

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

                mComments = new ArrayList<>();
                List<Entry> entrys = response.body().getEntrys();
                Log.d(TAG,String.valueOf(entrys.size()));

                for(int i = 0; i < entrys.size() ; i++){

                    ExtractXML extractXML = new ExtractXML("<div class=\"md\"><p>", entrys.get(i).getContent(), "</p>");
                    Log.d(TAG,"content: " + entrys.get(i).getContent() + "\n-------------------------------------------------------------------");
                    List<String> commentDetails = extractXML.start();
                  //  Log.d(TAG,commentDetails.get(0));
                    Log.d(TAG,entrys.get(i).toString() + "\n-------------------------------------------------------------------");

                    try {

                        mComments.add(new Comment(
                                commentDetails.get(0),
                                entrys.get(i).getAuthor().getName(),
                                entrys.get(i).getUpdated(),
                                entrys.get(i).getId()
                        ));

                    }catch (IndexOutOfBoundsException e){

                        mComments.add(new Comment(
                               "Error reading comment",
                               "None",
                                "None",
                                "None"
                        ));

                        Log.e(TAG,e.getMessage());

                    } catch (NullPointerException e){

                        mComments.add(new Comment(
                                commentDetails.get(0),
                                "None",
                                entrys.get(i).getUpdated(),
                                entrys.get(i).getId()
                        ));

                        Log.e(TAG,e.getMessage());
                    }

                    mListView = findViewById(R.id.commentsListView);
                    CommentsListAdapter commentsListAdapter = new CommentsListAdapter(CommentsActivity.this, R.layout.comments_layout, mComments);
                    mListView.setAdapter(commentsListAdapter);

                    mProgressBar.setVisibility(View.GONE);
                    progressText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<Feed> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void initPost() {

        final Intent incomingIntent = getIntent();
        postURL = incomingIntent.getStringExtra("@string/post_url");
        postThumbnailURL = incomingIntent.getStringExtra("@string/post_thumbnail");
        postUpdated = incomingIntent.getStringExtra("@string/post_updated");
        postAuthor = incomingIntent.getStringExtra("@string/post_author");
        postTitle = incomingIntent.getStringExtra("@string/post_title");

        TextView author = findViewById(R.id.postAuthor);
        TextView title = findViewById(R.id.postTitle);
        TextView updated = findViewById(R.id.postUpdated);
        ImageView thumbnail = findViewById(R.id.postThumbnail);
        Button btnReply = findViewById(R.id.btnPostReply);
        ProgressBar progressBar = findViewById(R.id.postLoadingProgressBar);

        title.setText(postTitle);
        author.setText(postAuthor);
        updated.setText(postUpdated);
        displayImage(postThumbnailURL,thumbnail,progressBar);


        //NSFW posts will cause an error
        try {

            String[] splitURL = postURL.split(BASE_URL);
            currentFeed = splitURL[1];
            Log.d(TAG,"initPost: current feed: " + currentFeed);

        }catch (ArrayIndexOutOfBoundsException e){

            e.printStackTrace();

        }

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommentsActivity.this,WebViewActivity.class);
                intent.putExtra("url",postURL);
                startActivity(intent);
            }
        });

    }

    private void displayImage(String imageURL, ImageView imageView,final ProgressBar progressBar){

        //create the imageloader object
        ImageLoader imageLoader = ImageLoader.getInstance();

        //create display options
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage)
                .build();

        //download and display image from url
        imageLoader.displayImage(imageURL, imageView, options , new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {

                progressBar.setVisibility(View.VISIBLE);

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

                progressBar.setVisibility(View.GONE);

            }

        });
    }

    private void setupImageLoader(){

        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024)
                .build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        defaultImage = this.getResources().getIdentifier("@drawable/reddit_alien",null,this.getPackageName());
    }
}
