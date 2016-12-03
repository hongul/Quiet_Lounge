package com.example.hongear.quiet_lounge03;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//TODO Add Labels for Heatmap "spots"

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HeatMapFullscreenActivity extends Activity implements OnMapReadyCallback, JsonRequestFactory.HeatMapUpdaterInterface {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private TimerTask timerTask;
    private RequestQueue queue;             // Sends the HTTP requests to the web server
    private JsonRequestFactory jsonRequestFactory;
    private LocationInfo locationInfo;      // Holds info about current phone location and sound level
    private GoogleMap googleMap;
    private TileOverlay tileOverlay;
    private HeatmapTileProvider heatmapTileProvider;

    @Override
    protected void onPause() {
        super.onPause();
        timerTask.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerTask.run();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_heat_map_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.map_view);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // Access resources to get static value
        int timeBetweenRequests = getResources().getInteger(R.integer.TimeBetweenRequests);

        queue = Volley.newRequestQueue(this);

        // Creates jsonRequests
        jsonRequestFactory = new JsonRequestFactory(this);

        // Temporary Object to hold location info from phone
        locationInfo = new LocationInfo();

        MapFragment mapFragment = MapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_view, mapFragment);
        fragmentTransaction.commit();
        Log.d("mapFragment", mapFragment.toString());
        mapFragment.getMapAsync(this);

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

        Timer timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("Location", "Lat: " + locationInfo.getLat() + "Lng: " + locationInfo.getLng());
                //                locationInfo.setSound(getNoiseLevel());                       // Use with Phone
                queue.add(jsonRequestFactory.insertSoundData(locationInfo));
                queue.add(jsonRequestFactory.getLoungeData(false));
            }
        };

        timer.schedule(timerTask, new Date(), timeBetweenRequests);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        UiSettings uiSettings;

        this.googleMap = map;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        googleMap.setBuildingsEnabled(true);

        uiSettings = googleMap.getUiSettings();

        uiSettings.setAllGesturesEnabled(false);
//        uiSettings.setRotateGesturesEnabled(true);

        LatLng temple = new LatLng(39.980682, -75.154814);
        //mMap.addMarker(new MarkerOptions().position(temple).title("Marker in Temple U"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(temple));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(temple)
                .zoom(17)
                .bearing(260)
                .tilt(25)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private LocationListener setUpLocationListener() {
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

    @Override
    public void updateHeatMap(JSONObject response) throws JSONException {
        JSONArray dataJsonArray = response.getJSONArray("lounges");
        JSONObject data;
        double lat, lng, soundLevel;
        List<WeightedLatLng> list = new ArrayList<>();

        for (int i = 0; i < dataJsonArray.length(); i++) {
            data = dataJsonArray.getJSONObject(i);
            lat = data.getDouble("lat");
            lng = data.getDouble("lng");
            soundLevel = data.getDouble("lastSoundLevel");
            list.add(new WeightedLatLng(new LatLng(lat,lng), soundLevel));
        }

        if (heatmapTileProvider == null) {
            heatmapTileProvider = new HeatmapTileProvider.Builder()
                    .weightedData(list)
                    .radius(50)
                    .build();
        } else {
            heatmapTileProvider.setWeightedData(list);
        }

        // Clear heatmap overlay to redraw
        if (tileOverlay != null)
            tileOverlay.clearTileCache();

        if (googleMap != null && tileOverlay == null)
            tileOverlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));

        list.clear();
    }
}
