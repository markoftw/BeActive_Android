package com.marefx.marko.beactive;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ReviewActivity extends AppCompatActivity {

    private final OkHttpClient client = new OkHttpClient();

    public TextView title;
    public TextView imageTitle;
    public ImageView imageBody;
    public Button btnReview;
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Log.e("notifyCreated", "has been called");
        DataService.getToken(this);
        title = (TextView) findViewById(R.id.textViewTItle);
        imageTitle = (TextView) findViewById(R.id.textViewImageTitle);
        imageBody = (ImageView) findViewById(R.id.imageViewBody);

        btnReview = (Button) findViewById(R.id.buttonReviewConfirm);
        btnReview.setEnabled(false);

        RequestBody body = new FormBody.Builder()
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + DataService.JWTToken)
                .url(DataService.SERVER_ADDRESS + "/api/user/reviews/view")
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
                            Integer post_id = object.getInt("id");
                            Log.e("jsonObject", review_text + " " + picture_url);
                            ReviewList combo = new ReviewList(post_id, picture_url, review_text);
                            DataService.myList.add(combo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new Thread() {
                        public void run() {
                            ReviewActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (DataService.myList.size() > 0) {
                                        btnReview.setEnabled(true);
                                        title.setText("Imate " + DataService.myList.size() + " čakajočih pregledov slik");
                                        ReviewList tmp = DataService.myList.get(0);
                                        imageTitle.setText(tmp.title);
                                        DataService.loadImageFromUrl(ReviewActivity.this, DataService.SERVER_ADDRESS + "/images/" + tmp.picture_url, imageBody);
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
                builder.setMessage("Odobri sliko in naslov?")
                        .setCancelable(false)
                        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //ReviewActivity.this.finish();
                                // send YES to server
                                if (DataService.myList.size() > 1) {
                                    ReviewList tmp = DataService.myList.get(0);
                                    DataService.sendAlertChoice(ReviewActivity.this, "Yes", tmp.post_id, "none");
                                    DataService.myList.remove(0);
                                    tmp = DataService.myList.get(0);
                                    imageTitle.setText(tmp.title);
                                    DataService.loadImageFromUrl(ReviewActivity.this, DataService.SERVER_ADDRESS + "/images/" + tmp.picture_url, imageBody);
                                } else {
                                    ReviewList tmp = DataService.myList.get(0);
                                    DataService.sendAlertChoice(ReviewActivity.this, "Yes", tmp.post_id, "none");
                                    btnReview.setEnabled(false);
                                    imageTitle.setText("");
                                    imageBody.setImageResource(0);
                                    DataService.myList.remove(0);
                                    Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                                    ReviewActivity.this.startActivity(intent);
                                }
                                if (DataService.myList.size() > 0) {
                                    title.setText("Imate " + DataService.myList.size() + " čakajočih pregledov slik");
                                } else {
                                    Toast.makeText(ReviewActivity.this, "Hvala za ocene!", Toast.LENGTH_SHORT).show();
                                    title.setText("Ni čakajočih slik za odobritev");
                                }

                            }
                        })
                        .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //dialog.cancel();
                                //DataService.handleNoAnswer(ReviewActivity.this, title, imageTitle, imageBody, btnReview);
                                AlertDialog.Builder builder = new AlertDialog.Builder(ReviewActivity.this);
                                builder.setTitle("Vnesite razlog");
                                final EditText input = new EditText(ReviewActivity.this);
                                input.setInputType(InputType.TYPE_CLASS_TEXT);
                                builder.setView(input);
                                builder.setPositiveButton("Pošlji", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(input.getText().length() > 0) {
                                            m_Text = input.getText().toString();
                                            if (DataService.myList.size() > 1) {
                                                ReviewList tmp = DataService.myList.get(0);
                                                DataService.sendAlertChoice(ReviewActivity.this, "No", tmp.post_id, m_Text);
                                                DataService.myList.remove(0);
                                                tmp = DataService.myList.get(0);
                                                imageTitle.setText(tmp.title);
                                                DataService.loadImageFromUrl(ReviewActivity.this, DataService.SERVER_ADDRESS + "/images/" + tmp.picture_url, imageBody);
                                            } else {
                                                ReviewList tmp = DataService.myList.get(0);
                                                DataService.sendAlertChoice(ReviewActivity.this, "No", tmp.post_id, m_Text);
                                                btnReview.setEnabled(false);
                                                imageTitle.setText("");
                                                imageBody.setImageResource(0);
                                                DataService.myList.remove(0);
                                                Intent intent = new Intent(ReviewActivity.this, UserActivity.class);
                                                ReviewActivity.this.startActivity(intent);
                                            }
                                            if (DataService.myList.size() > 0) {
                                                title.setText("Imate " + DataService.myList.size() + " čakajočih pregledov slik");
                                            } else {
                                                Toast.makeText(ReviewActivity.this, "Hvala za ocene!", Toast.LENGTH_SHORT).show();
                                                title.setText("Ni čakajočih slik za odobritev");
                                            }
                                        } else {
                                            Toast.makeText(ReviewActivity.this, "Vnesite razlog", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                builder.setNegativeButton("Prekliči", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();

                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}
