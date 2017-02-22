package com.marefx.marko.beactive;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    public static final String SERVER_ADDRESS = "http://beactive.marefx.com";

    public static String DeviceToken;


    NotificationCompat.Builder notification;
    private static final int uniqID = 54416;

    TextView welcomeMsg;
    TextView uploadImgName;
    Button cameraButton;
    Button uploadButton;
    Button notifButton;
    Button buttonReview;
    ImageView uploadImg;

    String mCurrentPhotoPath;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        FirebaseMessaging.getInstance().subscribeToTopic("test");
        DeviceToken = FirebaseInstanceId.getInstance().getToken();
        checkIfSameToken();

        Log.e("CurrDeviceToken", "current device token: " + DeviceToken);
        Log.e("CurrJWTToken", "current JWT token: " + LoginActivity.JWTToken);

        cameraButton = (Button) findViewById(R.id.buttonTakephoto);
        uploadButton = (Button) findViewById(R.id.buttonUploadphoto);
        notifButton = (Button) findViewById(R.id.notifyButton);
        buttonReview = (Button) findViewById(R.id.buttonReviews);
        welcomeMsg = (TextView) findViewById(R.id.prijavljenKot);
        uploadImgName = (TextView) findViewById(R.id.uploadImageName);
        uploadImg = (ImageView) findViewById(R.id.imageToUpload);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        welcomeMsg.setText("Prijavljeni kot " + username);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == cameraButton) {
                    try {
                        dispatchTakePictureIntent();
                    } catch (IOException e) {
                        Log.d("Ex_click_camera" , e.getMessage());
                    }
                }
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == uploadButton) {
                    try {
                        dispatchUploadIntent();
                    } catch (IOException e) {
                        Log.d("Ex_click_upload" , e.getMessage());
                    }
                }
            }
        });
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == notifButton) {
                    try {
                        dispatchNotification();
                    } catch (IOException e) {
                        Log.d("Ex_click_notify" , e.getMessage());
                    }
                }
            }
        });
        buttonReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == buttonReview) {
                    Intent intent = new Intent(UserActivity.this, ReviewActivity.class);
                    UserActivity.this.startActivity(intent);
                }
            }
        });
    }

    public void saveDeviceToken() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("deviceToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", DeviceToken);
        editor.apply();
    }

    public void getDeviceToken() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("deviceToken", Context.MODE_PRIVATE);
        DeviceToken = pref.getString("token", "");
    }

    private void checkIfSameToken() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("deviceToken", Context.MODE_PRIVATE);
        String token = pref.getString("token", "");
        Log.e("checkTok", token);
        if(token.equals(DeviceToken)) {
            // Token is same, do nothing
            Log.e("userDeviceToken", "Token is the same");
        } else {
            // Update token in DB and SharedPreferences
            Log.e("userDeviceToken", "Token is NOT the same");
            saveDeviceToken();

            RequestBody body = new FormBody.Builder()
                    .add("device_token", DeviceToken)
                    .build();

            Request request = new Request.Builder()
                    .addHeader("Authorization", "Bearer " + LoginActivity.JWTToken)
                    .url(UserActivity.SERVER_ADDRESS + "/api/user/register/device")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(UserActivity.this, "Napaka " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    if (response.code() == 200) {
                        Log.e("response", response.body().string());
                        /*try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                    }
                }

            });
        }
    }

    private void dispatchNotification() throws IOException {
        notification.setSmallIcon(R.drawable.ic_action_name);
        notification.setTicker("Ticker test text");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Title test text");
        notification.setContentText("Body context test message");
        notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent intent = new Intent(this, ReviewActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(uniqID, notification.build());
    }

    private void dispatchUploadIntent() throws IOException {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Show the thumbnail on ImageView
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return;
            }
            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(UserActivity.this,
                    new String[]{imageUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
            String tempPath = getPath(imageUri);
            uploadImg.setImageURI(imageUri);
            Bitmap image = ((BitmapDrawable) uploadImg.getDrawable()).getBitmap();
            new UploadImage(image, tempPath, tempPath).execute();
        } else if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String tempPath = getPath(selectedImage);
            /*File f = new File(selectedImage.getPath());
            String selectedImageName = f.getName();
            uploadImgName.setText("Uploading path: " + tempPath);*/
            uploadImg.setImageURI(selectedImage);
            Bitmap image = ((BitmapDrawable) uploadImg.getDrawable()).getBitmap();
            new UploadImage(image, tempPath, tempPath).execute();
        }
    }

    private class UploadImage extends AsyncTask<Void, Void, Void> {
        Bitmap image;
        String name;
        String path;
        public UploadImage(Bitmap image, String name, String path) {
            this.image = image;
            this.name = name;
            this.path = path;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imgBytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(imgBytes, Base64.DEFAULT);
            try {
                RequestBody req = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        //.addFormDataPart("image", "test.jpg", RequestBody.create(MediaType.parse("image/jpeg"), new File(path)))
                        .addFormDataPart("name", name)
                        .addFormDataPart("image", encodedImage)
                        .build();

                Request request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + LoginActivity.JWTToken)
                        .url(SERVER_ADDRESS + "/api/user/store/image")
                        .post(req)
                        .build();

                Response response = client.newCall(request).execute();

                Log.d("token", "JWT:" + LoginActivity.JWTToken);
                Log.d("file", "test: " + new File(path).toString());
                Log.d("response", "uploadImage: " + response.body().string());
            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e("Error_1", "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e("Error_2", "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Upload complete!", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File storageDir = new File(path, "Camera");
        /*File path;
        File storageDir = null;
        if (VERSION.SDK_INT >= 24) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            storageDir = new File(path, "Camera");
        } else {
            Log.d("NotSupported", "Not Supported");
        }*/
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Exception_creating" , ex.getMessage());
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(UserActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                /*Uri photoURI;
                if (VERSION.SDK_INT >= 24) {
                    photoURI = FileProvider.getUriForFile(UserActivity.this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }*/
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public String getPath(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }
}
