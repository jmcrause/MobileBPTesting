package com.example.mobilebptesting;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;
import com.otaliastudios.cameraview.size.Size;

import java.util.HashMap;
import java.util.Map;

public class MeasureBP extends AppCompatActivity implements View.OnClickListener {

    ProgressDialog loading;
    private LineGraphSeries<DataPoint> series1;
    ImageView imageView;
    Button btn_start;
    TextView mTextField;
    CountDownTimer countDownTimer, graphTimer;
    ProgressBar progressBar;
    ImageView imageViewCamera;


    //Camera Declarations
    CameraView camera;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    private boolean active = false;

    double [] x_arr = new double[30*20];
    double [] y_arr = new double[30*20];
    int frame_count = 0;
    String id, sbp_ref, dbp_ref, hr_ref, sbp_app, dbp_app, hr_app;
    Long time_start;
    int attempts = 1;
    double reference_line = 0.5;

    // ** Override methods, onCreate, onClick, onRequestPermissionResults (Camera) ****************

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.measure_bp);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        time_start = System.currentTimeMillis()/1000;

        mTextField = findViewById(R.id.textViewMeasure);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        sbp_ref = bundle.getString("sbp_ref");
        dbp_ref = bundle.getString("dbp_ref");
        hr_ref = bundle.getString("hr_ref");

        imageViewCamera = findViewById(R.id.imageViewCamera);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_start = findViewById(R.id.btn_start);
        btn_start.setText(getString(R.string.start));
        btn_start.setOnClickListener(this);

        progressBar = findViewById(R.id.progressBar);

        //Camera management

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        camera.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {
                long time = frame.getTime();
                Size size = frame.getSize();
                //int format = frame.getFormat();
                //int userRotation = frame.getRotationToUser();
                //int viewRotation = frame.getRotationToView();
                if (frame.getDataClass() == byte[].class) {
                    byte[] data = frame.getData();

                    if (active){
                        /*imageProcessing.decodeYUV420SPtoRGB(data.clone(), size.getHeight(), size.getWidth());
                        float redAvg = imageProcessing.getRed();
                        float greenAvg = imageProcessing.getGreen();
                        float blueAvg = imageProcessing.getBlue();

                        updateColor(redAvg, greenAvg, blueAvg);

                        if (redAvg > 200 && greenAvg < 10) {
                            addEntry((float)255.0-redAvg, time);
                        }*/
                    }
                } /*else if (frame.getDataClass() == Image.class) {
                                                 Image data = frame.getData();
                                                 // Process android.media.Image...
                                             }*/

            }
        });

        //Graph details
        GraphView graph = findViewById(R.id.graph);
        series1 = new LineGraphSeries<>(generateData());

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(5);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);

        graph.setTitle("PPG Waveform");

        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        series1.setColor(R.color.design_default_color_primary);

        graph.addSeries(series1);

        //Create 20 CountDownTimer that refreshes screen every 100 ms
        countDownTimer = new CountDownTimer(20000, 33) {

            public void onTick(long millisUntilFinished) {
                addFrame(millisUntilFinished);
            }

            public void onFinish() {
            }
        };


        graphTimer = new CountDownTimer(20000, 100) {

            public void onTick(long millisUntilFinished) {

                if (millisUntilFinished > 15000) {
                    mTextField.setText(getText(R.string.calibrating) + " " + (millisUntilFinished / 1000 - 15) + "s");
                }
                else {
                    mTextField.setText("HR: " + calculateHeartRate() + " BPM");
                }
                if (millisUntilFinished == 15000) {
                    reference_line = centerValue ();
                }

                int progress = (int) (20000 - millisUntilFinished)/200;

                progressBar.setProgress(progress);

                series1.resetData(generateData());
            }

            public void onFinish() {
                sbp_app = sbp_ref;
                dbp_app = dbp_ref;
                hr_app = String.valueOf(calculateHeartRate());

                mTextField.setText("Done!");
                measurementComplete();
            }
        };
    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_start){
            if (btn_start.getText() == getString(R.string.start)) {

                active = true;

                btn_start.setText(getString(R.string.stop));

                imageViewCamera.setVisibility(View.INVISIBLE);

                camera.setFlash(Flash.TORCH);

                frame_count = 0;
                countDownTimer.start();
                graphTimer.start();
            }
            else {
                active = false;

                btn_start.setText(getString(R.string.start));

                imageViewCamera.setVisibility(View.VISIBLE);

                camera.setFlash(Flash.OFF);

                frame_count = 0;
                countDownTimer.cancel();
                graphTimer.cancel();
            }
        }

    }

    // ********************************************************************************************


    // ** API Integration - at end of measurement *************************************************

    //Data transferred to Google Drive
    private void uploadData() {

        final String signal = arrayToString();

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    //Toast.makeText(MeasureBP.this,"PPG data uploaded",Toast.LENGTH_LONG).show();
                    addAttemptsToSheet();

                },
                error -> Toast.makeText(MeasureBP.this,error.getMessage(),Toast.LENGTH_LONG).show()
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "saveData");
                params.put("id", id);
                params.put("signal", signal);

                return params;
            }
        };

        int socketTimeOut = 10000; // 10s

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);

    }
    private void addAttemptsToSheet() {

        Long dt = System.currentTimeMillis()/1000 - time_start;
        final String t = dt.toString();
        final String n = Integer.toString(attempts);

        String api_url = getString(R.string.api);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api_url,
                response -> {

                    //Toast.makeText(MeasureBP.this,"Attempt data uploaded",Toast.LENGTH_LONG).show();
                    showResults();

                },
                error -> Toast.makeText(MeasureBP.this,error.getMessage(),Toast.LENGTH_LONG).show()
        ){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("action", "addAttempts");
                params.put("id", id);
                params.put("n", n);
                params.put("t", t);

                return params;
            }
        };

        int socketTimeOut = 10000; // 10s

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(this);

        queue.add(stringRequest);

    }

    // Runs when measurement is completed
    // Either calls showResults, when just measure was selected, or uploadData for participant
    private void measurementComplete() {
        loading = ProgressDialog.show(this, "Processing...", "Please wait");

        if (id.equals("0")) {
            showResults();
        }
        else {
            uploadData();
        }
    }

    //Go to results activity
    private void showResults() {
        loading.dismiss();
        Intent intent = new Intent(getApplicationContext(), ViewResults.class);
        intent.putExtra("id", id);
        intent.putExtra("sbp_ref", sbp_ref);
        intent.putExtra("dbp_ref", dbp_ref);
        intent.putExtra("hr_ref", hr_ref);
        intent.putExtra("sbp_app", sbp_app);
        intent.putExtra("dbp_app", dbp_app);
        intent.putExtra("hr_app", hr_app);
        startActivity(intent);
    }

    // ********************************************************************************************


    // ** Graph management ************************************************************************

    // Create DataPoint data for graph
    private DataPoint[] generateData() {

        int count = 30*5;
        double x,y;
        DataPoint[] values = new DataPoint[count];

        for (int i = frame_count-count; i < frame_count; i++)
        {
            x = (i+count-frame_count)*0.033;
            if (i<0) {
                y = 0;
            }
            else {
                y = y_arr[i];
            }
            DataPoint v = new DataPoint(x, y);
            values[i-(frame_count-count)] = v;
        }
        return values;
    }

    // To be called on every frame received to add to x and y global arrays
    private void addFrame(long millisUntilFinished) {
        x_arr[frame_count] = (20000-millisUntilFinished)/1000.0;
        y_arr[frame_count] = (Math.sin(x_arr[frame_count]*2*3.14)+1)/2.0;
        frame_count++;
    }

    private String arrayToString () {
        String arr_str = "";

        for (int i = 0; i < frame_count; i++)
        {
            arr_str = arr_str + ", " + y_arr[i];
        }

        return arr_str;
    }

    private double centerValue () {
        double center = 0;
        // Find Min and Max values
        double min = 1;
        double max = 0;
        for (int i = 0; i < frame_count; i++)
        {
            if (y_arr[i] > max) {
                max = y_arr[i];
            }
            if (y_arr[i] < min) {
                min = y_arr[i];
            }
        }

        center = (max - min)/2.0;

        return center;
    }

    private int calculateHeartRate () {
        int rolling_hr = 0;

        // Count rising edges
        double count = 0;
        int start = 0;
        int end = 0;
        for (int i = 30; i < frame_count; i++)
        {
            if (y_arr[i] >= reference_line && y_arr[i-1] < reference_line) {
                count++;
                if (count==1) {
                    start = i;
                }
                end = i;
            }
        }

        double dt = (end - start)/30.0;
        rolling_hr = (int) Math.round((count-1)/dt * 60);

        return rolling_hr;
    }
}
