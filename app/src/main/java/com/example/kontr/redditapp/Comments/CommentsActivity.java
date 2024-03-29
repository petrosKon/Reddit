package com.example.kontr.redditapp.Comments;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kontr.redditapp.Account.LoginActivity;
import com.example.kontr.redditapp.ExtractXML;
import com.example.kontr.redditapp.FeedApi;
import com.example.kontr.redditapp.MainActivity;
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
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.example.kontr.redditapp.URLS.BASE_URL;
import static com.example.kontr.redditapp.URLS.COMMENT_URL;
import static com.example.kontr.redditapp.URLS.LOGIN_URL;

public class CommentsActivity extends AppCompatActivity {

    private static final String TAG = "CommentsActivity";

    private static String postTitle;
    private static String postAuthor;
    private static String postUpdated;
    private static String postURL;
    private static String postThumbnailURL;
    private static String postID;

    private String modhash;
    private String cookie;
    private String username;

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

        getSessionParams();

        setupToolbar();

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

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            getUserComment(mComments.get(position).getId());
                        }
                    });

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

    private void setupToolbar(){

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        toolbar.setOnMenuItemClickListener(new android.support.v7.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case  R.id.navLogin:
                        Intent intent = new Intent(CommentsActivity.this,LoginActivity.class);
                        startActivity(intent);
                }

                return false;
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
        postID = incomingIntent.getStringExtra("@string/post_id");

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

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserComment(postID);
            }
        });

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CommentsActivity.this,WebViewActivity.class);
                intent.putExtra("url",postURL);
                startActivity(intent);
            }
        });

    }

    private void getUserComment(final String post_id) {

        final Dialog dialog = new Dialog(CommentsActivity.this);
        dialog.setTitle("Dialog");
        dialog.setContentView(R.layout.comment_input_dialog);

        int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.95f);
        int height = (int)(getResources().getDisplayMetrics().heightPixels * 0.6f);

        dialog.getWindow().setLayout(width,height);
        dialog.show();

        Button btnPostComment = dialog.findViewById(R.id.btnPostComment);
        final EditText comment = dialog.findViewById(R.id.dialogComment);

        btnPostComment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(COMMENT_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                FeedApi feedApi = retrofit.create(FeedApi.class);

                HashMap<String,String> headerMap = new HashMap<>();
                headerMap.put("User-Agent",username);
                headerMap.put("X-Modhash",modhash);
                headerMap.put("cookie","reddit_session=" + cookie);

                String theComment = comment.getText().toString();
                Call<CheckComment> call = feedApi.submitComment(headerMap,"comment",post_id,theComment);

                call.enqueue(new Callback<CheckComment>() {
                    @Override
                    public void onResponse(Call<CheckComment> call, Response<CheckComment> response) {


                        try {
                            Log.d(TAG,response.toString());

                            String success = response.body().getSuccess();

                            if(success.equals("true")){

                                Toast.makeText(CommentsActivity.this,"Post successful",Toast.LENGTH_SHORT).show();

                            } else {

                                Toast.makeText(CommentsActivity.this,"Error occurred",Toast.LENGTH_SHORT).show();

                            }

                        }catch (NullPointerException e){

                        }

                    }

                    @Override
                    public void onFailure(Call<CheckComment> call, Throwable t) {

                    }
                });

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

    /*
     * Get the params stored in memory from logging in
     */
    private void getSessionParams(){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(CommentsActivity.this);

        username = sharedPreferences.getString("@string/SessionUsername","");
        modhash = sharedPreferences.getString("@string/SessionModhash","");
        cookie = sharedPreferences.getString("@string/SessionCookie","");

        Log.d(TAG,"modhash='" + modhash + '\'' +
                    ", cookie='" + cookie + '\'' +
                    ", username='" + username + '\'' +
                    '}');

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getSessionParams();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu,menu);
        return true;
    }
}
