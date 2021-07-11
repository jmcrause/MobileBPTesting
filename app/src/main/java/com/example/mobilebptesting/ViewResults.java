package com.example.mobilebptesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

public class ViewResults extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_save, btn_try_again;
    TextView text_sbp_app, text_dbp_app, text_hr_app, text_sbp_ref, text_dbp_ref, text_hr_ref;
    String id, sbp_ref, dbp_ref, hr_ref, sbp_app, dbp_app, hr_app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_results);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_save = findViewById(R.id.btn_save_measurements);
        btn_save.setOnClickListener(this);

        btn_try_again = findViewById(R.id.btn_try_again);
        btn_try_again.setOnClickListener(this);

        //Get BP and HR data
        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        sbp_ref = bundle.getString("sbp_ref");
        dbp_ref = bundle.getString("dbp_ref");
        hr_ref = bundle.getString("hr_ref");
        sbp_app = bundle.getString("sbp_app");
        dbp_app = bundle.getString("dbp_app");
        hr_app = bundle.getString("hr_app");

        if (id.equals("0")) {
            btn_save.setText("Done");
        }

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

    //Data transferred to Google Drive
    private void addItemToSheet() {
        final ProgressDialog loading = ProgressDialog.show(this, "Saving measurements", "Please wait");

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    loading.dismiss();
                    Toast.makeText(ViewResults.this, response,Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getApplicationContext(), RateApp.class);
                    startActivity(intent);

                },
                error -> Toast.makeText(ViewResults.this,error.getMessage(),Toast.LENGTH_LONG).show()
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "saveBP");
                params.put("id", id);
                params.put("sbp", sbp_app);
                params.put("dbp", dbp_app);
                params.put("hr", hr_app);

                return params;
            }
        };

        int socketTimeOut = 10000; // 10s

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
        if (v == btn_save){
            if (id.equals("0")) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            else {
                addItemToSheet();
            }

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
