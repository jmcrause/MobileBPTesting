package com.example.mobilebptesting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewParticipant extends AppCompatActivity implements View.OnClickListener {

    Button btn_save;
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_participant);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_save = findViewById(R.id.button_new);
        btn_save.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_save){
            Intent intent = new Intent(getApplicationContext(), MeasureBP.class);
            startActivity(intent);
        }
    }
}
