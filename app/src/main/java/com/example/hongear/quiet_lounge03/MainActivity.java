package com.example.hongear.quiet_lounge03;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "DecibelTest";
    public static double REFERENCE = 0.00002;
    private final static String GET_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/getLoungeData";
    private final static String POST_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/inputSound";
    private final static int TIME_BETWEEN_HTTP_REQUESTS = 2000;
    private double sound;
    private LocationInfo locationInfo;
    private RequestQueue queue;
    private JSONObject loungeResponseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);
        locationInfo = new LocationInfo();

        // Runtime permission requests - SDK 23 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        // Code needed to get Latitude and Longitude coordinates of phone
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationInfo.setLat(location.getLatitude());
                locationInfo.setLng(location.getLongitude());
                Log.d("Update", "Location Updated");
                Log.d("New Coordinates", "Lat: " + locationInfo.getLat() + " Lng: " + locationInfo.getLng());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        // Gets the Coordinates from phone, if it has permissions
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // Create Timer to Constantly send new sound Data
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                locationInfo.setSound(getNoiseLevel());
                queue.add(insertSoundData(locationInfo));        // Use with emulator
                queue.add(getLoungeData());
            }
        }, new Date(), TIME_BETWEEN_HTTP_REQUESTS);
    }


    public void refreshData(View view) {
        Log.d("get Data", "Pressed Refresh");
        queue.add(getLoungeData());
    }

    public void heatMap(View view) {
        Toast.makeText(this, "switching view...", Toast.LENGTH_SHORT).show();
        Intent homeIntent = new Intent(MainActivity.this, HeatMap.class);
        startActivity(homeIntent);
        finish();
    }

    public JsonObjectRequest insertSoundData(final LocationInfo local) {

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, POST_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String responseMsg = response.getString("msg");
                            Log.d("API Response", "Response Message: " + responseMsg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        })
        {
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    String bodyStr;
//                    bodyStr = "lat=" + local.getLat() + "&lng=" + local.getLng() + "&sound=" + local.getSound();
                    bodyStr = "lat=" + local.getLat() + "&lng=" + local.getLng() + "&sound=" + String.valueOf(Math.random() * 30);
                    return bodyStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        return request;
    }

    @SuppressWarnings("unused")
    public JsonObjectRequest getLoungeData() {
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, GET_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray dataJsonArray = response.getJSONArray("lounges");
                    JSONObject data;
                    TextView textView;
                    String soundLevel;
                    DecimalFormat df = new DecimalFormat("#.###");

                    // Update Data for SERC [0]
                    data = dataJsonArray.getJSONObject(0);
                    textView = (TextView) findViewById(R.id.serc_data);
                    soundLevel = df.format(data.getDouble("lastSoundLevel"));
                    textView.setText(soundLevel);

                    // Update Data for Tech Center [1]
                    data = dataJsonArray.getJSONObject(1);
                    textView = (TextView) findViewById(R.id.tech_data);
                    soundLevel = df.format(data.getDouble("lastSoundLevel"));
                    textView.setText(soundLevel);

                    // Update Data for Student Center [2]
                    data = dataJsonArray.getJSONObject(2);
                    textView = (TextView) findViewById(R.id.student_center_data);
                    soundLevel = df.format(data.getDouble("lastSoundLevel"));
                    textView.setText(soundLevel);

                    // Update Data for Library [3]
                    data = dataJsonArray.getJSONObject(3);
                    textView = (TextView) findViewById(R.id.library_data);
                    soundLevel = df.format(data.getDouble("lastSoundLevel"));
                    textView.setText(soundLevel);

                    // Update Data for Wachman Hall [4]
                    data = dataJsonArray.getJSONObject(4);
                    textView = (TextView) findViewById(R.id.wachman_data);
                    soundLevel = df.format(data.getDouble("lastSoundLevel"));
                    textView.setText(soundLevel);

                    Log.d("Update Sound levels","Updated");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        return getRequest;
    }

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
}
