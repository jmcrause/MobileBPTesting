package com.example.mobilebptesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class NewParticipant extends AppCompatActivity implements View.OnClickListener {

    EditText editTextSBP, editTextDBP, editTextHR;
    Button btn_save;
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_participant);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        editTextSBP = findViewById(R.id.editTextSBP);
        editTextDBP = findViewById(R.id.editTextDBP);
        editTextHR = findViewById(R.id.editTextHR);

        btn_save = findViewById(R.id.button_new);
        btn_save.setOnClickListener(this);
    }

    //Data transferred to Google Drive
    private void addItemToSheet() {
        final ProgressDialog loading = ProgressDialog.show(this, "Adding Item", "Please wait");
        final String SBP = editTextSBP.getText().toString().trim();
        final String DBP = editTextDBP.getText().toString().trim();
        final String HR = editTextHR.getText().toString().trim();

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    loading.dismiss();
                    Toast.makeText(NewParticipant.this,"ID: " + response,Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MeasureBP.class);
                    intent.putExtra("id", response);
                    intent.putExtra("sbp_ref", SBP);
                    intent.putExtra("dbp_ref", DBP);
                    intent.putExtra("hr_ref", HR);
                    startActivity(intent);

                },
                error -> Toast.makeText(NewParticipant.this,error.getMessage(),Toast.LENGTH_LONG).show()
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "addItem");
                params.put("sbp", SBP);
                params.put("dbp", DBP);
                params.put("hr", HR);

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
            addItemToSheet();
        }
    }
}
