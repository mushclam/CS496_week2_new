package com.example.q.cs496_week2_new;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import retrofit2.Call;

public class UserProfile extends AppCompatActivity {
    private ShareDialog shareDialog;

    public static String id = "";
    public static String name = "";
    public static String profile = null;

    public static String nickname = "";
    public EditText nickView;

    public static String phoneNumber = "";
    public EditText numberView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_userprofile);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        shareDialog = new ShareDialog(this);


        Bundle inBundle = getIntent().getExtras();
        name = inBundle.get("name").toString() + " " + inBundle.get("surname").toString();
        String imageUrl = inBundle.get("imageUrl").toString();
        id = inBundle.get("id").toString();


        nickView = (EditText) findViewById(R.id.nickname_info);
        numberView = (EditText) findViewById(R.id.phoneNumber);

        numberView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        TextView nameView = (TextView) findViewById(R.id.nameAndSurname);
        nameView.setText(name);

        Button start = (Button) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Send request to /login/ -> 있는 계정 -> main으로
                //                         -> 없는 계정 -> nickname, phonenumber edittext 보여주고 register 버튼 활성화
                new LoginTask().execute();
            }
        });
        new UserProfile.DownloadImage((ImageView)findViewById(R.id.profileImage)).execute(imageUrl);
    }

    public class LoginTask extends AsyncTask<Void, Void, String> {
        public LoginTask() {}

        @Override
        protected String doInBackground(Void... params) {
            StartClient client = ServiceGenerator.createService(StartClient.class);
            Call<String> call = client.login(id);
            Log.d("Network test", "/login/");
            String response = "";
            try {
                response = call.execute().body().toString();
                Log.d("Network test", "/login/ after execute");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                if (response.equals("login success")) {
                    Intent login = new Intent(UserProfile.this, MainActivity.class);
                    startActivity(login);
                    finish();
                }
                else if (response.equals("register please")) {
                    nickView.setVisibility(View.VISIBLE);
                    numberView.setVisibility(View.VISIBLE);
                    findViewById(R.id.start).setVisibility(View.GONE);
                    Button register_but = findViewById(R.id.register);
                    register_but.setVisibility(View.VISIBLE);
                    register_but.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            nickname =  nickView.getText().toString();
                            phoneNumber = numberView.getText().toString();
                            new RegisterTask().execute();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Login fail...", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class RegisterTask extends AsyncTask<Void, Void, String> {
        public RegisterTask() {}

        @Override
        protected String doInBackground(Void... params) {
            StartClient client = ServiceGenerator.createService(StartClient.class);
            JsonObject param = new JsonObject();

            param.addProperty("token", id);
            param.addProperty("profile", profile);
            param.addProperty("nickname", nickname);
            param.addProperty("name", name);
            param.addProperty("phoneNumber", phoneNumber);

            Gson gson = new Gson();
            Call<String> call = client.register(gson.toJson(param));

            String response = "";
            try {
                response = call.execute().body().toString();
                Log.d("Response test", response);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            Log.d("Response test", "onPostExecute response : " + response);
            if (response != null) {
                if (response.equals("register success")) {
                    Toast.makeText(getApplicationContext(), "Register success!", Toast.LENGTH_SHORT).show();
                    Intent login = new Intent(UserProfile.this, MainActivity.class);
                    startActivity(login);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "Register fail...", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImage(ImageView bmImage){
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls){
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try{
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            }catch (Exception e){
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result){
            profile = BitmapString.bitmapToBase64(result);
            bmImage.setImageBitmap(result);
            bmImage.setBackground(new ShapeDrawable(new OvalShape()));
        }

    }
}
