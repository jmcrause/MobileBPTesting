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
import android.util.Log;
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
    //private boolean active = false;

    private

    double [] x_arr = new double[30*20];
    double [] y_arr = new double[30*20];
    double [] x_resample = new double[30*20];
    double [] y_resample = new double[30*20];
    double [] y_filtered = new double[30*20];

    String id, sbp_ref, dbp_ref, hr_ref, sbp_app, dbp_app, hr_app;
    double time_start, t_0;
    int attempts = 1;
    double reference_line = 0.5;

    //ImageProcessing imageProcessing;

    float redAvg, greenAvg, blueAvg, redSD, greenSD, blueSD;

    int frame_count = 0;   // number of frames received
    int process_frame = 0; // number of frames processed
    int ppg_count = 0;    // used to track the number of ppg values calculated
    int sample_count = 0; // used to track the number of resampled values calculated

    FrameProcessor [] frameProcessors = new FrameProcessor[30];

    int state = 0;
    Boolean active = false;
    Boolean run_thread = true;

    //Signal validation variables
    int r_min = 128;
    int g_min = 10;
    int g_max = 128;
    int b_max = 128;
    int sd_max = 40;

    int filter_block = 4;

    /********************************************************************************************
    Override methods, onCreate, onClick *****************************************************
    ********************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.measure_bp);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        time_start = System.currentTimeMillis()/1000.0;

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

        for (int i = 0; i < 30; i++) {
            frameProcessors[i] = new FrameProcessor();
        }

        //Camera management

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);

        camera.addFrameProcessor(new com.otaliastudios.cameraview.frame.FrameProcessor() {
            @Override
            @WorkerThread
            public void process(@NonNull Frame frame) {
                double time =  System.currentTimeMillis()/1000.0;
                Size size = frame.getSize();
                //int format = frame.getFormat();
                //int userRotation = frame.getRotationToUser();
                //int viewRotation = frame.getRotationToView();
                if (frame.getDataClass() == byte[].class) {
                    byte[] data = frame.getData();

                    if (state >= getResources().getInteger(R.integer.state_active)  && state <= getResources().getInteger(R.integer.state_measuring)){
                        /*imageProcessing.decodeYUV420SPtoRGB(data.clone(), size.getHeight(), size.getWidth());
                        imageProcessing.calculateStandardDeviation();

                        float redAvg = imageProcessing.getRedMean();
                        float greenAvg = imageProcessing.getGreenMean();
                        float blueAvg = imageProcessing.getBlueMean();
                        float redSD = imageProcessing.getSdRed();
                        float greenSD = imageProcessing.getSdGreen();
                        float blueSD = imageProcessing.getSdBlue();*/

                        frameProcessors[frame_count%30].setFrame(data.clone(), size.getHeight(), size.getWidth(), time);
                        //updateSize(frame_count,process_frame);
                        frame_count++;

                        if (state == getResources().getInteger(R.integer.state_start)) {
                            startCalibrationState();
                        }


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
        graphTimer = new CountDownTimer(20000, 100) {

            public void onTick(long millisUntilFinished) {

                int progress = (int) (20000 - millisUntilFinished)/200;

                switch (state) {
                    case 0: //initial

                        break;

                    case 10: //active

                        break;

                    case 11: //start

                        break;

                    case 12: //restart
                        restartCalibrationState();
                        break;

                    case 20: //calibration
                        mTextField.setText(getText(R.string.calibrating) + " " + (millisUntilFinished / 1000 - 15) + "s");
                        if (millisUntilFinished <= 15000) {
                            startMeasuringState ();
                        }
                        progressBar.setProgress(progress);

                        series1.resetData(generateData());
                        break;
                    case 30: //measuring
                        mTextField.setText("HR: " + calculateHeartRate() + " BPM");

                        progressBar.setProgress(progress);

                        series1.resetData(generateData());

                        break;
                    case -1: //error
                        startErrorState();
                        break;
                }

                /*if (state == "calibration") {
                    mTextField.setText(getText(R.string.calibrating) + " " + (millisUntilFinished / 1000 - 15) + "s");
                    if (millisUntilFinished <= 15000) {
                        startMeasuringState ();
                    }
                }
                else if (state == "measuring") {
                    mTextField.setText("HR: " + calculateHeartRate() + " BPM");
                }


                if (state == "recalibration") {
                    restartCalibrationState();
                }
                else if (state == "error") {
                    startErrorState();
                }
                else {
                    int progress = (int) (20000 - millisUntilFinished)/200;
                    progressBar.setProgress(progress);

                    series1.resetData(generateData());
                }*/



            }

            public void onFinish() {
                sbp_app = sbp_ref;
                dbp_app = dbp_ref;
                hr_app = String.valueOf(calculateHeartRate());

                mTextField.setText("Done!");
                measurementComplete();
            }
        };


        myThread.start();
    }

    /********************************************************************************************
     Click Events ************************************************************************
     ********************************************************************************************/

    @Override
    public void onClick(View v) {

        if (v == imageView){
            run_thread = false;         //stop thread when going to different activity.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        if (v == btn_start){
            if (state == 0 || state == -1) {
                startActiveState();
            }
            else {
                state = getResources().getInteger(R.integer.state_initial);
                active = false;

                btn_start.setText(getString(R.string.start));
                mTextField.setText(getText(R.string.start_text));
                imageViewCamera.setVisibility(View.VISIBLE);

                camera.setFlash(Flash.OFF);

                frame_count = 0;
                graphTimer.cancel();
            }
        }

    }



    /********************************************************************************************
     API Integration - at end of measurement *************************************************
    ********************************************************************************************/

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

        double dt = System.currentTimeMillis()/1000.0 - time_start;
        final String t = Double.toString(dt);
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




    /********************************************************************************************
    Graph management ************************************************************************
    ********************************************************************************************/

    // Create DataPoint data for graph
    private DataPoint[] generateData() {

        int count = 30*5;
        double x,y;
        DataPoint[] values = new DataPoint[count];

        // Find Min and Max values
        double min = 255;
        double max = 0;


        for (int i = sample_count-count; i < sample_count; i++)
        {
            if (i >= 0) {
                if (y_filtered[i] > max) {
                    max = y_filtered[i];
                }
                if (y_filtered[i] < min) {
                    min = y_filtered[i];
                }
            }
        }

        for (int i = sample_count-count; i < sample_count; i++)
        {
            x = (i+count-sample_count)*0.033;
            if (i<0) {
                y = 0;
            }
            else {
                y = (y_filtered[i] - min)/(max-min); //Normalise value
            }
            DataPoint v = new DataPoint(x, y);
            values[i-(sample_count-count)] = v;
        }
        return values;
    }



    // To be called on every frame received to add to x and y global arrays
    private void addFrame(float color, double time) {
        if (ppg_count == 0) {
            t_0 = time;
        }
        x_arr[ppg_count] = time - t_0;
        y_arr[ppg_count] = color;

        // resample to 30 Hz
        if (ppg_count > 0) {
            int left_index = (int)(x_arr[ppg_count-1]*30);
            int right_index = (int)(x_arr[ppg_count]*30);

            // gradient
            double r = (y_arr[ppg_count]-y_arr[ppg_count-1])/(x_arr[ppg_count]-x_arr[ppg_count-1]);

            for (int i = left_index; i <= right_index; i++) {
                if (i < 601) {
                    x_resample[i] = i*0.033;
                    y_resample[i] = (x_resample[i]-x_arr[ppg_count-1])*r + y_arr[ppg_count-1];
                    y_filtered[i] = y_resample[i];

                    // Apply moving average filter
                    if (i > filter_block) {
                        double filt_sum = 0;
                        for (int f = i-filter_block+1;f<=i;f++) {
                            filt_sum += y_resample[f];
                        }
                        y_filtered[i] = filt_sum/filter_block;
                    }
                    sample_count = i;
                }
            }
        }


        ppg_count++;
    }

    private String arrayToString () {
        String arr_str = "" + y_arr[0];

        for (int i = 1; i < ppg_count; i++)
        {
            arr_str = arr_str + ", " + y_arr[i];
        }
        arr_str = arr_str + "\r\n" + x_arr[0];
        for (int i = 1; i < ppg_count; i++)
        {
            arr_str = arr_str + ", " + x_arr[i];
        }

        return arr_str;
    }

    private double centerValue () {
        double center = 0;
        // Find Min and Max values
        double min = 1;
        double max = 0;
        for (int i = 120; i < frame_count; i++)
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
        for (int i = 120; i < frame_count; i++)
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




    /*********************************************************************************************
     Thread 1 : Process frames
     This thread waits till the program is connected to the manager and first frame is obtained
     After a frame is received, it calculates the avg value for further processing.
    *********************************************************************************************/
    Thread myThread = new Thread(){
        @Override
        public void run(){
            int buffer = 0;
            while (run_thread) {

                if (!active) {
                    //Log.d("Thread", "Waiting for image");
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                if (active){

                    //thread_state = "Active";
                    buffer = frame_count - process_frame;

                    //We will wait till a new frame is received
                    while(buffer <= 0){
                        //Sleeping part may lead to timing problems
                        try {
                            Thread.sleep(11);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        buffer = frame_count - process_frame;
                    }

                    //thread_state = "Processing";
                    int frame = process_frame%30;

                    frameProcessors[frame].decodeYUV420SPtoRGB();
                    frameProcessors[frame].calculateStandardDeviation();

                    redAvg = frameProcessors[frame].getRedMean();
                    greenAvg = frameProcessors[frame].getGreenMean();
                    blueAvg = frameProcessors[frame].getBlueMean();
                    redSD = frameProcessors[frame].getSdRed();
                    greenSD = frameProcessors[frame].getSdGreen();
                    blueSD = frameProcessors[frame].getSdBlue();

                    //mean(R) − σR ≥ Rmin
                    //mean(G) − σG ≥ Gmin
                    //mean(G) + σG ≤ Gmax
                    //mean(B) + σB ≤ Bmax
                    //σR, σG, σB < σmax


                    switch (state) {
                        case 0: //initial

                            break;

                        case 10: //active
                            Log.d("Current state", "Active");
                            if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                                state = getResources().getInteger(R.integer.state_start);
                                Log.d("Next State", "Start");
                                //addFrame((float)255.0-redAvg, frameProcessors[frame].time);
                            }
                            else {
                                Log.d("Finger status", "No finger");
                            }
                            Log.d("RedAvg", Float.toString(redAvg));
                            break;

                        case 11: //start
                            Log.d("Current state", "Start");
                            if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                                //state = getResources().getInteger(R.integer.state_start);
                                Log.d("Next State", "Waiting for calibration state");
                                //addFrame((float)255.0-redAvg, frameProcessors[frame].time);
                            }
                            else {
                                Log.d("Finger status", "No finger");
                            }
                            Log.d("RedAvg", Float.toString(redAvg));
                            break;

                        case 12: //restart
                            Log.d("Current state", "Restart");
                            if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                                Log.d("Next State", "Waiting for active state");
                                //addFrame((float)255.0-redAvg, frameProcessors[frame].time);
                            }
                            else {
                                Log.d("Finger status", "No finger");
                            }
                            Log.d("RedAvg", Float.toString(redAvg));
                            break;

                        case 20: //calibration
                            Log.d("Current state", "Calibration");
                            if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                                //Log.d("Next State", "Waiting for calibration state");
                                addFrame(redAvg, frameProcessors[frame].time);
                            }
                            else {
                                Log.d("Finger status", "No finger");
                                Log.d("Next State", "Recalibration");
                                state = getResources().getInteger(R.integer.state_restart);
                                Log.d("RedAvg", Float.toString(redAvg));
                                Log.d("RedSD", Float.toString(redSD));
                                Log.d("GreenAvg", Float.toString(greenAvg));
                                Log.d("GreenSD", Float.toString(greenSD));
                                Log.d("BlueAvg", Float.toString(blueAvg));
                                Log.d("BlueSD", Float.toString(blueSD));
                            }
                            //Log.d("RedAvg", Float.toString(redAvg));

                            break;


                        case 30: //measuring
                            Log.d("Current state", "Measuring");
                            if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                                //Log.d("Next State", "Waiting for calibration state");
                                addFrame(redAvg, frameProcessors[frame].time);
                            }
                            else {
                                Log.d("Finger status", "No finger");
                                Log.d("Next State", "Error");
                                state = getResources().getInteger(R.integer.state_error);
                            }
                            Log.d("RedAvg", Float.toString(redAvg));

                            break;


                        case -1: //error
                            Log.d("Current state", "Error");

                            break;
                    }

                    /*if (redAvg - redSD >= r_min &&  greenAvg + greenSD <= g_max && blueAvg + blueSD <= b_max) {
                        if (state == "active") {
                            //startCalibrationState();
                            state = "start-calibration";
                        }
                        Log.d("RedAvg", Float.toString(redAvg));
                        addFrame((float)255.0-redAvg, frameProcessors[frame].time);
                    }
                    else {
                        Log.d("RedAvg", Float.toString(redAvg));
                        if (state == "calibration") {
                            state = "recalibration";
                            Log.d("State", "Recalibration");
                        }
                        else if (state == "measuring") {
                            state = "error";
                            Log.d("State", "Error");
                        }
                    }*/

                    process_frame++;

                }
            }
        }
    };




    /********************************************************************************************
    State handling  *************************************************************************
    ********************************************************************************************/

    private void startActiveState() {
        state = getResources().getInteger(R.integer.state_active);
        active = true;


        mTextField.setText(getText(R.string.finger));
        btn_start.setText(getString(R.string.stop));

        imageViewCamera.setVisibility(View.INVISIBLE);

        camera.setFlash(Flash.TORCH);

        frame_count = 0;
        process_frame = 0;
        ppg_count = 0;

    }

    private void startCalibrationState() {
        state = getResources().getInteger(R.integer.state_calibration);
        graphTimer.start();

        // Set reference values according to dissertation

        //Rmin = 128, Gmin = 10, Gmax = 128, Bmax = 128 and
        //σmax = 40.

        //mean(R) − σR ≥ Rmin
        //mean(G) − σG ≥ Gmin
        //mean(G) + σG ≤ Gmax
        //mean(B) + σB ≤ Bmax
        //σR, σG, σB < σmax

    }

    private void restartCalibrationState() {
        startActiveState();
        graphTimer.cancel();
    }

    private void startMeasuringState() {
        state = getResources().getInteger(R.integer.state_measuring);
        reference_line = centerValue();

        // Change reference values according to dissertation

        //Get min and max R, G and B

        //mean(R) − σR ≥ Rmin
        //mean(G) − σG ≥ Gmin
        //mean(G) + σG ≤ Gmax
        //mean(B) + σB ≤ Bmax
        //σR, σG, σB < σmax


    }

    private void startErrorState() {
        //state = getResources().getInteger(R.integer.state_error);
        graphTimer.cancel();

        btn_start.setText(getString(R.string.start));
        mTextField.setText(getText(R.string.error));
        imageViewCamera.setVisibility(View.VISIBLE);

        camera.setFlash(Flash.OFF);


        attempts++;

    }

    private void measurementComplete() {
        //state = getResources().getInteger(R.integer.state_complete);
        loading = ProgressDialog.show(this, "Processing...", "Please wait");

        // Either calls showResults, when just measure was selected, or uploadData for participant
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

    /********************************************************************************************/
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        run_thread = false;  //stop thread when going to different activity.
    }
}