package com.example.mobilebptesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RateApp extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_rating_save, btn_rating_skip;
    RatingBar rating_q1, rating_q2, rating_q3, rating_q4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rate_app);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_rating_save = findViewById(R.id.btn_rating_save);
        btn_rating_save.setOnClickListener(this);

        btn_rating_skip = findViewById(R.id.btn_rating_skip);
        btn_rating_skip.setOnClickListener(this);

        rating_q1 = findViewById(R.id.rating_q1);
        rating_q2 = findViewById(R.id.rating_q2);
        rating_q3 = findViewById(R.id.rating_q3);
        rating_q4 = findViewById(R.id.rating_q4);


    }

    private void addRatingsToSheet() {
        final ProgressDialog loading = ProgressDialog.show(this, "Saving Rating", "Please wait");
        final String q1 = String.valueOf(rating_q1.getRating());
        final String q2 = String.valueOf(rating_q2.getRating());
        final String q3 = String.valueOf(rating_q3.getRating());
        final String q4 = String.valueOf(rating_q4.getRating());

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    loading.dismiss();
                    Toast.makeText(RateApp.this, response, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
                    startActivity(intent);

                },
                error -> {
                    loading.dismiss();
                    Toast.makeText(RateApp.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
        ) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "addRating");
                params.put("q1", q1);
                params.put("q2", q2);
                params.put("q3", q3);
                params.put("q4", q4);

                return params;
            }
        };

        int socketTimeOut = 30000; // 30s

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);
    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_rating_save){
            addRatingsToSheet();
        }
        if (v == btn_rating_skip){
            Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
            startActivity(intent);
        }

    }
}
