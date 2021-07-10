package com.example.mobilebptesting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ViewResults extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_save, btn_try_again;
    TextView text_sbp_app, text_dbp_app, text_hr_app, text_sbp_ref, text_dbp_ref, text_hr_ref;
    String id, sbp_ref, dbp_ref, hr_ref, sbp_app, dbp_app, hr_app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_results);

        //Get BP and HR data
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        sbp_ref = bundle.getString("sbp_ref");
        dbp_ref = bundle.getString("dbp_ref");
        hr_ref = bundle.getString("hr_ref");
        sbp_app = bundle.getString("sbp_app");
        dbp_app = bundle.getString("dbp_app");
        hr_app = bundle.getString("hr_app");


        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_save = findViewById(R.id.btn_save_measurements);
        btn_save.setOnClickListener(this);

        btn_try_again = findViewById(R.id.btn_try_again);
        btn_try_again.setOnClickListener(this);


        //Display application BP and HR data
        text_sbp_app = findViewById(R.id.text_sbp_app);
        text_sbp_app.setText(sbp_app);

        text_dbp_app = findViewById(R.id.text_dbp_app);
        text_dbp_app.setText(dbp_app);

        text_hr_app = findViewById(R.id.text_hr_app);
        text_hr_app.setText(hr_app);

        //Display reference BP and HR data
        text_sbp_ref = findViewById(R.id.text_sbp_ref);
        text_sbp_ref.setText(sbp_ref);

        text_dbp_ref = findViewById(R.id.text_dbp_ref);
        text_dbp_ref.setText(dbp_ref);

        text_hr_ref = findViewById(R.id.text_hr_ref);
        text_hr_ref.setText(hr_ref);


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
            intent.putExtra("id", id);
            intent.putExtra("sbp_ref", sbp_ref);
            intent.putExtra("dbp_ref", dbp_ref);
            intent.putExtra("hr_ref", hr_ref);
            startActivity(intent);
        }

    }
}
