package com.example.mobilebptesting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MeasureBP extends AppCompatActivity implements View.OnClickListener {

    private LineGraphSeries<DataPoint> series1;
    ImageView imageView;
    Button btn_start;

    double [] x_arr = new double[30*20];
    double [] y_arr = new double[30*20];
    int frame_count = 0;
    String id, sbp_ref, dbp_ref, hr_ref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.measure_bp);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getString("id");
        sbp_ref = bundle.getString("sbp_ref");
        dbp_ref = bundle.getString("dbp_ref");
        hr_ref = bundle.getString("hr_ref");

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);

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
    }

    @Override
    public void onClick(View v) {
        if (v == imageView){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        if (v == btn_start){
            Intent intent = new Intent(getApplicationContext(), ViewResults.class);
            intent.putExtra("id", id);
            intent.putExtra("sbp_ref", sbp_ref);
            intent.putExtra("dbp_ref", dbp_ref);
            intent.putExtra("hr_ref", hr_ref);
            intent.putExtra("sbp_app", sbp_ref);
            intent.putExtra("dbp_app", dbp_ref);
            intent.putExtra("hr_app", hr_ref);
            startActivity(intent);
        }

    }

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
}
