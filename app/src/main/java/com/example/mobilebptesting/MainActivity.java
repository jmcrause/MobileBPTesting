package com.example.mobilebptesting;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
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
            intent.putExtra("id", "0");
            intent.putExtra("sbp_ref", "0");
            intent.putExtra("dbp_ref", "0");
            intent.putExtra("hr_ref", "0");
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        //finish();
        //System.exit(0);

    }
}