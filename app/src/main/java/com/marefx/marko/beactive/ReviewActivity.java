package com.marefx.marko.beactive;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class ReviewList {
    public String picture_url;
    public String title;

    public ReviewList(String picture_url, String title) {
        this.picture_url = picture_url;
        this.title = title;
    }

}

public class ReviewActivity extends AppCompatActivity {

    public static final String SERVER_ADDRESS = "http://beactive.marefx.com";
    private final OkHttpClient client = new OkHttpClient();
    private String JWTToken;

    TextView title;
    TextView imageTitle;
    ImageView imageBody;
    Button btnReview;
    ArrayList<ReviewList> myList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Log.e("notifyCreated", "has been called");
        getToken();

        title = (TextView) findViewById(R.id.textViewTItle);
        imageTitle = (TextView) findViewById(R.id.textViewImageTitle);
        imageBody = (ImageView) findViewById(R.id.imageViewBody);
        btnReview = (Button) findViewById(R.id.buttonReviewConfirm);

        RequestBody body = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + JWTToken)
                .url(SERVER_ADDRESS + "/api/user/reviews/view")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(ReviewActivity.this, "Napaka " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() == 200) {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);

                    try {
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            String review_text = object.getString("review_text");
                            String picture_url = object.getString("picture_url");
                            Log.e("jsonObject", review_text + " " + picture_url);
                            ReviewList combo = new ReviewList(picture_url, review_text);
                            myList.add(combo);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new Thread() {
                        public void run() {
                            ReviewActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (myList.size() > 0) {
                                        title.setText("You have " + myList.size() + " pending reviews");
                                        ReviewList tmp = myList.get(0);
                                        imageTitle.setText(tmp.title);
                                        loadImageFromUrl(SERVER_ADDRESS + "/images/" + tmp.picture_url);
                                    }
                                }
                            });
                        }
                    }.start();
                }
            }

        });


        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
                builder.setMessage("Approve this image?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //ReviewActivity.this.finish();
                                // send YES to server
                                if (myList.size() > 1) {
                                    myList.remove(0);
                                    ReviewList tmp = myList.get(0);
                                    imageTitle.setText(tmp.title);
                                    loadImageFromUrl(SERVER_ADDRESS + "/images/" + tmp.picture_url);
                                } else {
                                    btnReview.setEnabled(false);
                                    imageTitle.setText("");
                                    imageBody.setImageResource(0);
                                    myList.remove(0);
                                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                                    intent.putExtra("username", "testing");
                                    ReviewActivity.this.startActivity(intent);
                                }
                                if (myList.size() > 0) {
                                    title.setText("You have " + myList.size() + " pending reviews");
                                } else {
                                    title.setText("No pending reviews");
                                }

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //dialog.cancel();
                                // send NO to server
                                if (myList.size() > 1) {
                                    myList.remove(0);
                                    ReviewList tmp = myList.get(0);
                                    imageTitle.setText(tmp.title);
                                    loadImageFromUrl(SERVER_ADDRESS + "/images/" + tmp.picture_url);
                                } else {
                                    btnReview.setEnabled(false);
                                    imageTitle.setText("");
                                    imageBody.setImageResource(0);
                                    myList.remove(0);
                                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                                    intent.putExtra("username", "testing");
                                    ReviewActivity.this.startActivity(intent);
                                }
                                if (myList.size() > 0) {
                                    title.setText("You have " + myList.size() + " pending reviews");
                                } else {
                                    title.setText("No pending reviews");
                                }
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    private void sendAlertChoice(String choice) {

    }

    public void getToken() {
        SharedPreferences pref = getSharedPreferences("userToken", Context.MODE_PRIVATE);
        JWTToken = pref.getString("token", "");
    }

    private void loadImageFromUrl(String url) {
        Log.e("imgUrl", url);
        OkHttpClient client2 = new OkHttpClient.Builder()
                .addInterceptor(new okhttp3.Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + JWTToken)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Picasso picasso2 = new Picasso.Builder(getApplicationContext())
                .downloader(new OkHttp3Downloader(client2))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        exception.printStackTrace();
                    }
                })
                .build();

        picasso2.load(url).placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imageBody, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.e("succ", "success");
                    }

                    @Override
                    public void onError() {
                        Log.e("err", "error");
                    }
                });
    }
}
