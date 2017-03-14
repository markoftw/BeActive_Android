package com.marefx.marko.beactive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

class ReviewList {
    Integer post_id;
    String picture_url;
    String title;

    ReviewList(Integer post_id, String picture_url, String title) {
        this.post_id = post_id;
        this.picture_url = picture_url;
        this.title = title;
    }

}

class DataService {
    static String JWTToken = null;
    static String DeviceToken = null;
    static String Username = null;
    static String Device_Type = null;
    static ArrayList<ReviewList> myList = new ArrayList<>();
    static String SERVER_ADDRESS = "http://beactive.marefx.com";
    private static final OkHttpClient client = new OkHttpClient();

    static void saveDeviceType(Context context, String type) {
        Device_Type = type;
        SharedPreferences pref =  context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("device_type", type);
        editor.apply();
    }

    static void getDeviceType(Context context) {
        SharedPreferences pref = context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        Device_Type = pref.getString("device_type", "");
    }

    static void saveUsername(Context context, String username) {
        Username = username;
        SharedPreferences pref =  context.getSharedPreferences("userName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("Username", username);
        editor.apply();
    }

    static void getUsername(Context context) {
        SharedPreferences pref = context.getSharedPreferences("userName", Context.MODE_PRIVATE);
        Username = pref.getString("Username", "");
    }

    static void exitApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    static void logout(final Context context) {
        SharedPreferences prefName = context.getSharedPreferences("userName", Context.MODE_PRIVATE);
        SharedPreferences prefToken = context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        SharedPreferences prefDeviceToken = context.getSharedPreferences("deviceToken", Context.MODE_PRIVATE);
        DeviceToken = prefToken.getString("token_device", "");
        JWTToken = prefToken.getString("token", "");
        SharedPreferences.Editor editorName = prefName.edit();
        SharedPreferences.Editor editorToken = prefToken.edit();
        SharedPreferences.Editor editorDeviceToken = prefDeviceToken.edit();
        editorName.remove("Username");
        editorName.apply();
        editorToken.remove("token");
        editorToken.remove("token_device");
        editorToken.remove("device_type");
        editorToken.apply();
        editorDeviceToken.remove("token");
        editorDeviceToken.apply();

        RequestBody formBody = new FormBody.Builder()
                .add("device_token", DeviceToken)
                .build();

        final okhttp3.Request request = new okhttp3.Request.Builder()
                .addHeader("Authorization", "Bearer " + JWTToken)
                .url(SERVER_ADDRESS + "/api/user/delete/device")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Napaka " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        boolean success = jsonResponse.has("success") ? jsonResponse.getBoolean("success") : false;
                        if (success) {

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    static void saveDeviceToken(Context context, String token) {
        DeviceToken = token;
        SharedPreferences pref =  context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token_device", token);
        editor.apply();
    }

    static void getDeviceToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        DeviceToken = pref.getString("token_device", "");
    }

    static void saveToken(Context context, String token) {
        JWTToken = token;
        SharedPreferences pref =  context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.apply();
    }

    static void getToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences("userToken", Context.MODE_PRIVATE);
        JWTToken = pref.getString("token", "");
    }

    static void handleNoAnswer(Context context, TextView title, TextView imageTitle, ImageView imageBody, Button btnReview) {
        /*ReviewList tmp = DataService.myList.get(0);
        Intent intentNo = new Intent(context, ReviewMessageActivity.class);
        intentNo.putExtra("post_id", tmp.post_id);
        context.startActivity(intentNo);*/
        if (DataService.myList.size() > 1) {
            DataService.myList.remove(0);
            ReviewList tmp = DataService.myList.get(0);
            imageTitle.setText(tmp.title);
            DataService.loadImageFromUrl(context, DataService.SERVER_ADDRESS + "/images/" + tmp.picture_url, imageBody);
        } else {
            btnReview.setEnabled(false);
            imageTitle.setText("");
            imageBody.setImageResource(0);
            DataService.myList.remove(0);
            Intent intent = new Intent(context, UserActivity.class);
            intent.putExtra("username", "testing");
            context.startActivity(intent);
        }
        if (DataService.myList.size() > 0) {
            title.setText("Imate " + DataService.myList.size() + " čakajočih pregledov slik");
        } else {
            title.setText("Ni čakajočih slik za odobritev");
        }

    }

    static void sendAlertChoice(final Context context, String choice, Integer post_id, String reason) {
        RequestBody body = new FormBody.Builder()
                .add("post_id", post_id.toString())
                .add("choice", choice)
                .add("reason", reason)
                .build();

        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + DataService.JWTToken)
                .url(SERVER_ADDRESS + "/api/user/reviews/store")
                .post(body)
                .build();
        final OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Napaka " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.code() == 200) {
                    String jsonData = response.body().string();
                    Log.e("response", jsonData);
                }
            }
        });
    }

    static void loadImageFromUrl(Context context, String url, ImageView img) {
        Log.e("imgUrl", url);
        OkHttpClient client2 = new OkHttpClient.Builder()
                .addInterceptor(new okhttp3.Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + DataService.JWTToken)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Picasso picasso2 = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client2))
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        exception.printStackTrace();
                    }
                })
                .build();

        picasso2.load(url).placeholder(R.drawable.perm_group_sync_settings)
                .error(R.mipmap.ic_launcher)
                .into(img, new com.squareup.picasso.Callback() {
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

    static boolean isNetworkAvailable(Context context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();

            if (info != null)
            {
                for (int i = 0; i < info.length; i++)
                {
                    Log.i("Class", info[i].getState().toString());
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static String isFirstRun(Context context) {
        SharedPreferences pref = context.getSharedPreferences("userFirstRun", Context.MODE_PRIVATE);
        String result = pref.getString("firstRun", "");
        if (result.equals("FALSE")) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("firstRun", "FALSE");
            editor.apply();
            return "FALSE";
        }
        return "TRUE";
    }
}
