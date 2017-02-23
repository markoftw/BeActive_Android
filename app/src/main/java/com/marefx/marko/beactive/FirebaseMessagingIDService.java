package com.marefx.marko.beactive;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class FirebaseMessagingIDService extends FirebaseInstanceIdService {

    private static final String TAG = FirebaseMessagingIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "onTokenRefresh called" + token);
        registerToken(token);
    }

    private void registerToken(String token) {
        Log.e("onRefresh", "registerToken called");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("device_token", token)
                .build();
        DataService.getToken(FirebaseMessagingIDService.this);
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + DataService.JWTToken)
                .url(DataService.SERVER_ADDRESS + "/api/user/register/device")
                .post(body)
                .build();

        storeRegIdInPref(token);

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeRegIdInPref(String token) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("deviceToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.apply();
    }

}
