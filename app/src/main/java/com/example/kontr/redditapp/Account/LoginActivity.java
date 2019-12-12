package com.example.kontr.redditapp.Account;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.kontr.redditapp.FeedApi;
import com.example.kontr.redditapp.R;
import com.example.kontr.redditapp.model.Feed;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

import static com.example.kontr.redditapp.URLS.BASE_URL;
import static com.example.kontr.redditapp.URLS.LOGIN_URL;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ProgressBar mProgressBar;
    private EditText mUsername;
    private EditText mPassword;
    private Button btnLogin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btn_login);
        mProgressBar = findViewById(R.id.loginRequestLoadingProgressBar);
        mUsername = findViewById(R.id.input_username);
        mPassword = findViewById(R.id.input_password);
        mProgressBar.setVisibility(View.GONE);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                if(!username.equals("") && !password.equals("")){

                    mProgressBar.setVisibility(View.VISIBLE);
                    login(username,password);
                }
            }
        });
    }

    private void login(final String username, String password) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(LOGIN_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FeedApi feedApi = retrofit.create(FeedApi.class);

        HashMap<String,String> headerMap = new HashMap<>();
        headerMap.put("Content-Type","application/json");

        Call<CheckLogin> call = feedApi.signIn(headerMap,username,username,password,"json");
        call.enqueue(new Callback<CheckLogin>() {
            @Override
            public void onResponse(Call<CheckLogin> call, Response<CheckLogin> response) {
                try {

                    Log.d(TAG,response.toString());

                    String modhash = response.body().getJson().getData().getModhash();
                    String cookie = response.body().getJson().getData().getCookie();

                    if (!modhash.equals("")) {

                        setSessionParams(username, modhash, cookie);
                        mProgressBar.setVisibility(View.GONE);
                        mUsername.setText("");
                        mPassword.setText("");
                        Toast.makeText(LoginActivity.this, "Login successfull", Toast.LENGTH_SHORT).show();

                        finish();
                    }

                }catch (NullPointerException e){

                    e.printStackTrace();

                }

            }

            @Override
            public void onFailure(Call<CheckLogin> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void setSessionParams(String username, String modhash, String cookie){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("@String/SessionUsername",username);
        editor.commit();
        editor.putString("@String/SessionModhash",modhash);
        editor.commit();
        editor.putString("@String/SessionCookie",cookie);
        editor.commit();

        Log.d(TAG,"setSessionsParams: Storing session variables: \n" +
                "username: " + username + "\n" +
                "modhash: " + modhash + "\n"+
                "cookie: " + cookie + "\n");
    }


}
