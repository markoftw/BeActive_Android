package com.marefx.marko.beactive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity {

    public static String JWTToken = null;
    private static final int RESULT_PERMS_INITIAL = 1339;

    private static final String[] PERMS_ALL = {
            INTERNET,
            CAMERA,
            ACCESS_NETWORK_STATE,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE
    };

    private ProgressDialog progressDialog;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        // CHECK IF TOKEN EXISTS
        getToken();

        Log.e("token2", JWTToken);

        if(JWTToken != null && JWTToken.length() > 0) {
            RequestBody formBody = new FormBody.Builder()
                    .build();

            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .addHeader("Authorization", "Bearer " + JWTToken)
                    .url(UserActivity.SERVER_ADDRESS + "/api/user/login/user")
                    .post(formBody)
                    .build();

            progressDialog.setMessage("Poteka prijava...");
            progressDialog.show();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(LoginActivity.this, "Napaka " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.code() == 200) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            boolean success = jsonResponse.has("success") ? jsonResponse.getBoolean("success") : false;
                            if (success) {
                                String name = jsonResponse.getString("username");
                                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                                intent.putExtra("username", name);

                                new Thread() {
                                    public void run() {
                                        LoginActivity.this.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "Token valid, logged in", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }.start();

                                LoginActivity.this.startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (response.code() == 401) {
                        new Thread() {
                            public void run() {
                                LoginActivity.this.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(LoginActivity.this, "Token expired, sign in", Toast.LENGTH_SHORT).show();
                                        progressDialog.hide();
                                    }
                                });
                            }
                        }.start();
                    }
                }

            });
        }

        if (isFirstRun().equals("TRUE")) {
            ActivityCompat.requestPermissions(this, PERMS_ALL, RESULT_PERMS_INITIAL);
        }

        final EditText email = (EditText) findViewById(R.id.userLogin);
        final EditText password = (EditText) findViewById(R.id.userPassword);
        final Button login = (Button) findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = email.getText().toString();
                final String pass = password.getText().toString();

                if(user.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vnesite e-poštni naslov", Toast.LENGTH_SHORT).show();
                }

                if(pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vnesite geslo", Toast.LENGTH_SHORT).show();
                }

                final List<Pair<String, String>> params = new ArrayList<Pair<String, String>>() {{
                    add(new Pair<>("email", user));
                    add(new Pair<>("password", pass));
                }};
                progressDialog.setMessage("Poteka prijava...");
                progressDialog.show();
                Fuel.post("http://beactive.marefx.com/api/user/login/", params).responseString(new Handler<String>() {
                    @Override
                    public void failure(@NonNull Request request, @NonNull Response response, @NonNull FuelError error) {
                        Toast.makeText(LoginActivity.this, "Napaka " + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void success(@NonNull Request request, @NonNull Response response, String data) {
                        try {
                            JSONObject jsonResponse = new JSONObject(data);
                            boolean success = jsonResponse.has("success") ? jsonResponse.getBoolean("success") : false;

                            if (success) {
                                Toast.makeText(LoginActivity.this, "Uspešno prijavljeni!", Toast.LENGTH_SHORT).show();
                                String name = jsonResponse.getString("username");

                                Intent intent = new Intent(LoginActivity.this, UserActivity.class);
                                intent.putExtra("username", name);
                                JWTToken = jsonResponse.getString("token");
                                LoginActivity.this.saveToken();
                                LoginActivity.this.startActivity(intent);
                            } else {
                                Toast.makeText(LoginActivity.this, "Neuspešna prijava, poskusite ponovno!", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CHECK IF IT HAS NETWORK CONNECTION && IS AUTHORIZED = CONTINUE
        // ELSE CONNECT TO THE INTERNET
        // OR AUTHORIZE AGAIN
    }

    public void saveToken() {
        SharedPreferences pref = getSharedPreferences("userToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", JWTToken);
        editor.apply();
    }

    public void getToken() {
        SharedPreferences pref = getSharedPreferences("userToken", Context.MODE_PRIVATE);
        JWTToken = pref.getString("token", "");
    }

    private String isFirstRun() {
        SharedPreferences pref = getSharedPreferences("userFirstRun", Context.MODE_PRIVATE);
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