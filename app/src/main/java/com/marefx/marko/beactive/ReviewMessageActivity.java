package com.marefx.marko.beactive;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReviewMessageActivity extends AppCompatActivity {

    TextView naslov;
    EditText razlog;
    Button oddaj;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id = item.getItemId();
        if(res_id == R.id.action_exit) {
            Toast.makeText(getApplicationContext(), "You selected Exit", Toast.LENGTH_SHORT).show();
        } else if(res_id == R.id.action_logout) {
            Toast.makeText(getApplicationContext(), "You selected Logout", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_message);

        naslov = (TextView) findViewById(R.id.textViewNaslov);
        razlog = (EditText) findViewById(R.id.editTextRazlog);
        oddaj = (Button) findViewById(R.id.buttonOddaj);

        Intent intent = getIntent();
        final Integer post_id = intent.getIntExtra("post_id", 0);

        oddaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ReviewMessageActivity.this, post_id.toString(), Toast.LENGTH_LONG).show();
                if(razlog.getText().length() > 0) {
                    // send request
                } else {
                    Toast.makeText(ReviewMessageActivity.this, "Prosimo vnesite razlog", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
