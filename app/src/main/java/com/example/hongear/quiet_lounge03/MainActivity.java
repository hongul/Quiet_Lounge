package com.example.hongear.quiet_lounge03;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "DecibelTest";
    public static double REFERENCE = 0.00002;

    public double getNoiseLevel() {
        Log.e(TAG, "start new recording process");
        int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
        //making the buffer bigger....
        bufferSize = bufferSize * 4;
        AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        short data[] = new short[bufferSize];
        double average = 0.0;
        recorder.startRecording();
        //recording data;
        recorder.read(data, 0, bufferSize);

        recorder.stop();
        Log.e(TAG, "stop");
        for (short s : data) {
            if (s > 0) {
                average += Math.abs(s);
            } else {
                bufferSize--;
            }
        }
        //x=max;
        double x = average / bufferSize;
        Log.e(TAG, "" + x);
        recorder.release();
        Log.d(TAG, "getNoiseLevel() ");
        double db = 0;
        if (x == 0) {
            Log.e(TAG, "No valid noise level");
        }
        // calculating the pascal pressure based on the idea that the max amplitude (between 0 and 32767) is
        // relative to the pressure
        double pressure = x / 51805.5336; //the value 51805.5336 can be derived from asuming that x=32767=0.6325 Pa and x=1 = 0.00002 Pa (the reference value)
        Log.d(TAG, "x=" + pressure + " Pa");
        db = (20 * Math.log10(pressure / REFERENCE));
        Log.d(TAG, "db=" + db);
        if (db > 0) {
            return db;
        } else {
            return 0;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void addData(View view) {
        Toast.makeText(this, "capture and send data to db", Toast.LENGTH_SHORT).show();
        //double db = getNoiseLevel();
        //// TODO: 10/25/2016 send decibel level to manager with post request for storage

    }


    public void refreshData(View view) {
        //Toast.makeText(this, "refreshed...", Toast.LENGTH_LONG).show();

        ///// TODO: 10/25/2016 send get request to manager for data to populate table

        Random r = new Random();
        int rand = r.nextInt(69 - 28) + 28;
        String random = Integer.toString(rand);

        TextView tv = (TextView) findViewById(R.id.row2_tv2);
        tv.setText(random);
        if (rand > 45) {
            tv.setBackgroundColor(Color.RED);
        } else {
            tv.setBackgroundColor(Color.GREEN);
        }

        rand = r.nextInt(52 - 28) + 28;
        random = Integer.toString(rand);
        tv = (TextView) findViewById(R.id.row3_tv2);
        tv.setText(random);
        if (rand > 45) {
            tv.setBackgroundColor(Color.RED);
        } else {
            tv.setBackgroundColor(Color.GREEN);
        }

        rand = r.nextInt(56 - 28) + 28;
        random = Integer.toString(rand);
        tv = (TextView) findViewById(R.id.row4_tv2);
        tv.setText(random);
        if (rand > 45) {
            tv.setBackgroundColor(Color.RED);
        } else {
            tv.setBackgroundColor(Color.GREEN);
        }

        rand = r.nextInt(46 - 28) + 28;
        random = Integer.toString(rand);
        tv = (TextView) findViewById(R.id.row5_tv2);
        tv.setText(random);
        if (rand > 45) {
            tv.setBackgroundColor(Color.RED);
        } else {
            tv.setBackgroundColor(Color.GREEN);
        }

        rand = r.nextInt(49 - 28) + 28;
        random = Integer.toString(rand);
        tv = (TextView) findViewById(R.id.row6_tv2);
        tv.setText(random);
        if (rand > 45) {
            tv.setBackgroundColor(Color.RED);
        } else {
            tv.setBackgroundColor(Color.GREEN);
        }


    }

    public void heatMap(View view) {
        Toast.makeText(this, "switching view...", Toast.LENGTH_SHORT).show();
        Intent homeIntent = new Intent(MainActivity.this, HeatMap.class);
        startActivity(homeIntent);
        finish();
    }
}
