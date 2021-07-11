package com.example.mobilebptesting;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ContactDetails extends AppCompatActivity implements View.OnClickListener {

    ImageView imageView;
    Button btn_save;
    EditText editTextEmail, editTextCell;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_details);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_save = findViewById(R.id.btn_submit);
        btn_save.setOnClickListener(this);

        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextCell = findViewById(R.id.editTextPhone);

    }

    //Data transferred to Google Drive
    private void addItemToSheet() {
        final ProgressDialog loading = ProgressDialog.show(this, "Saving data", "Please wait");
        final String email = editTextEmail.getText().toString().trim();
        final String cell = editTextCell.getText().toString().trim();

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    loading.dismiss();
                    Toast.makeText(ContactDetails.this, "Done!",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);

                },
                error -> Toast.makeText(ContactDetails.this,error.getMessage(),Toast.LENGTH_LONG).show()
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "addEmail");
                params.put("email", email);
                params.put("cell", cell);

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
