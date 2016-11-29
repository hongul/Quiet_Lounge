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
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is the main activity of the the application. Displays the sound data collected from the
 * web server. Shows the name of each of the lounges and the cooresponding sound levels. These
 * values are being updated every specified interval (TIME_BETWEEN_HTTP_REQUESTS). In the
 * background, this activity is also constantly sending its current location and ambient sound
 * levels to the server.
 */
public class MainActivity extends AppCompatActivity {

    // Constants
    private final String TAG = "DecibelTest";
    public static double REFERENCE = 0.00002;

    // Fields
    private double sound;                   // Decibel Level
    private LocationInfo locationInfo;      // Holds info about current phone location and sound level
    private RequestQueue queue;             // Sends the HTTP requests to the web server
    private int timeBetweenRequests;
    private JsonRequestFactory jsonRequestFactory;      // Generates HTTP request

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Access resources to get static value
        timeBetweenRequests = getResources().getInteger(R.integer.TimeBetweenRequests);

        // Set up queue to send HTTP request
        queue = Volley.newRequestQueue(this);

        // Temporary Object to hold location info from phone
        locationInfo = new LocationInfo();

        // Creates jsonRequests
        jsonRequestFactory = new JsonRequestFactory(this);

        // Runtime permission requests - SDK 23 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        // Code needed to get Latitude and Longitude coordinates of phone
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final LocationListener locationListener = setUpLocationListener();

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
                Log.d("Location", "Lat: " + locationInfo.getLat() + "Lng: " + locationInfo.getLng());
                locationInfo.setSound(getNoiseLevel());                       // Use with Phone
                queue.add(jsonRequestFactory.insertSoundData(locationInfo));
                queue.add(jsonRequestFactory.getLoungeData(true));
            }
        }, new Date(), timeBetweenRequests);
    }

    /**
     * Listen for "Refresh" button click. Maunally refreshes the sound data
     * @param view - The view object that was pressed
     */
    public void refreshData(View view) {
        Log.d("get Data", "Pressed Refresh");
        queue.add(jsonRequestFactory.getLoungeData(true));
    }

    /**
     * Listen for "Refresh" button click. Opens up Heat Map Activity
     * @param view - The view object that was pressed
     */
    public void heatMap(View view) {
        Toast.makeText(this, "switching view...", Toast.LENGTH_SHORT).show();
        Intent homeIntent = new Intent(MainActivity.this, HeatMap.class);
        startActivity(homeIntent);
        finish();
    }

    /**
     * Returns the location listener for the phone. Whenever the location of the device
     * changes it updates the LocationInfo object to the current location of the phone.
     *
     * @return LocationListener that updates LocationInfo to current latitude and longitude coords
     */
    public LocationListener setUpLocationListener() {
        return new LocationListener() {



            @Override
            public void onLocationChanged(Location location) {
                locationInfo.setLat(location.getLatitude());
                locationInfo.setLng(location.getLongitude());
                Log.d("Update", "Location Updated");
                Log.d("New Coordinates", "Lat: " + locationInfo.getLat() + " Lng: " + locationInfo.getLng());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    /**
     * Access the devices microphone and generates the current decibel levels from the devices
     * surroundings
     *
     * @return The decibel level of the phones surroundings
     */
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
