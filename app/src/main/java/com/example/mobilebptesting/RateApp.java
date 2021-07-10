package com.example.mobilebptesting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RateApp extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_rating_save;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rate_app);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_rating_save = findViewById(R.id.btn_rating_save);
        btn_rating_save.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_rating_save){
            Intent intent = new Intent(getApplicationContext(), ContactDetails.class);
            startActivity(intent);
        }

    }
}
