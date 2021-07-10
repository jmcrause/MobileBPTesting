package com.example.mobilebptesting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewResults extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_save, btn_try_again;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_results);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_save = findViewById(R.id.btn_save_measurements);
        btn_save.setOnClickListener(this);

        btn_try_again = findViewById(R.id.btn_try_again);
        btn_try_again.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_save){
            Intent intent = new Intent(getApplicationContext(), RateApp.class);
            startActivity(intent);
        }
        if (v == btn_try_again){
            Intent intent = new Intent(getApplicationContext(), MeasureBP.class);
            startActivity(intent);
        }

    }
}
