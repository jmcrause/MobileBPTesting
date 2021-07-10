package com.example.mobilebptesting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_new, btn_measure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_new = findViewById(R.id.btn_new_participant);
        btn_measure = findViewById(R.id.btn_just_measure);

        btn_new.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NewParticipant.class);
            startActivity(intent);
        });

        btn_measure.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MeasureBP.class);
            startActivity(intent);
        });
    }
}