package com.example.moviemate.main;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.moviemate.R;
import com.example.moviemate.home.HomeFragment;
import com.facebook.drawee.backends.pipeline.Fresco;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

public class DetailActivity extends YouTubeBaseActivity {

    public static final String DEVELOPER_KEY = "AIzaSyCeFPp30DTPY2amyGTRMbaPEDNZFaESMIM";
    ImageView mButtonPlay;
    String API_KEY = "b8f745c2d43033fd65ce3af63180c3c3";
    static String API_VIDEO;
    String videoLink;
    String key;
    private YouTubePlayerView youTubePlayerView;
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    YouTubePlayer.OnInitializedListener onInitializedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_detail);

        youTubePlayerView = findViewById(R.id.video_view);

        mButtonPlay = findViewById(R.id.button_play);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra(HomeFragment.EXTRA_IMAGE);
        String title = intent.getStringExtra(HomeFragment.EXTRA_TITLE);
        int popularity = intent.getIntExtra(HomeFragment.EXTRA_POPULARITY, 0);
        int id = intent.getIntExtra(HomeFragment.EXTRA_ID, 0);

        ImageView imageView = findViewById(R.id.image_view_detail);
        TextView textViewTitle = findViewById(R.id.text_view_title_detail);
        TextView textViewPopularity = findViewById(R.id.text_view_popularity_detail);

        imageView.setImageURI(Uri.parse(imageUrl));
        textViewTitle.setText(title);
        textViewPopularity.setText("Popularity : " + popularity);

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(String.valueOf(id))
                .appendPath("videos")
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", "en-US");

        API_VIDEO = builder.build().toString();
        URL searchURL = buildUrl();
        new DetailActivity.queryTask().execute(searchURL);

        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                youTubePlayer.loadVideo(key);
                youTubePlayer.play();
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                youTubePlayerView.initialize(API_KEY, onInitializedListener);
            }
        });

    }

    public class queryTask extends AsyncTask<URL, Void, String> { //<params, progress, result>

            //this function works in background thread
            @Override
            protected String doInBackground(URL... urls) {
                URL searchURL = urls[0]; // what does this mean
                String searchResults = null;
                try {
                    searchResults = getResponseFromHttpUrl(searchURL);
                } catch (IOException e) {
                    e.printStackTrace();//what does this mean
                }
                return searchResults;
            }

            //this function works in main thread
            @Override
            protected void onPostExecute(String s) {
                if (s != null && !s.equals("")) {
                    try {
                        JSONObject ob = new JSONObject(s);
                        onResponse(ob);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //this function is made to fetch values from API using JSON
            public void onResponse(JSONObject response) throws JSONException {
                JSONArray jsonArray = response.getJSONArray("results");//puts api result array in jason array named jsonArray
                JSONObject result = jsonArray.getJSONObject(0);
                key = result.getString("key");

                Uri.Builder builder = new Uri.Builder();
                builder.scheme("https")
                        .authority("www.youtube.com")
                        .appendPath("watch")
                        .appendQueryParameter("v", key)
                        .appendQueryParameter("feature", "youtu.be");
                videoLink = builder.build().toString();
                Log.d(videoLink, "onResponse: ");
            }
        }

        //this function converts the API url into formatted url
        public static URL buildUrl() {
//        Uri builtUri = Uri.parse(API_URL_POPULAR); //why do we convert string to URI
            URL url = null;
            try {
//            url = new URL(builtUri.toString());
                url = new URL(API_VIDEO);
            } catch (MalformedURLException e) {
                e.printStackTrace(); // prints class name and error line of Throwable object
            }
            return url;
        }

        //this function is responsible for getting response from API
        public static String getResponseFromHttpUrl(URL url) throws IOException {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();//what does this mean
            try {
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");//what does this Delimiter mean
                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
        }
    }
