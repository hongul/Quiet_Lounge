package com.example.hongear.quiet_lounge03;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Random;

public class HeatMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private Marker mAlter;
    private Marker mPaley;
    private Marker mSerc;
    private Marker mTech;
    private Marker mStudent;
    private Marker mWachman;

    private static final LatLng ALTER = new LatLng(39.9802845, -75.158018);
    private static final LatLng PALEY = new LatLng(39.9811121,-75.1550743);
    private static final LatLng SERC = new LatLng(39.9817836, -75.1530692);
    private static final LatLng TECH = new LatLng(39.9799703, -75.1533075);
    private static final LatLng STUDENT = new LatLng(39.9795807, -75.1552941);
    private static final LatLng WACHMAN = new LatLng(39.9809148,-75.1569864);

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setMaxZoomPreference(20.0f);
        mMap.setMinZoomPreference(15.0f);

        // Add a marker in temple and move the camera
        LatLng temple = new LatLng(39.9803336, -75.1575243);
        //mMap.addMarker(new MarkerOptions().position(temple).title("Marker in Temple U"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(temple));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(PALEY)
                .zoom(16)
                .bearing(90)
                .tilt(30)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mWachman = mMap.addMarker(new MarkerOptions()
                .position(WACHMAN)
                .title("Wachman Hall")
                .snippet("36 dB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        //mWachman.showInfoWindow();

        mPaley = mMap.addMarker(new MarkerOptions()
                .position(PALEY)
                .title("Paley Library")
                .snippet("32 dB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        //mPaley.showInfoWindow();

        mSerc = mMap.addMarker(new MarkerOptions()
                .position(SERC)
                .title("SERC")
                .snippet("33 dB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mSerc.showInfoWindow();

        mTech = mMap.addMarker(new MarkerOptions()
                .position(TECH)
                .title("Tech Center")
                .snippet("35 dB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        //mTech.showInfoWindow();

        mStudent = mMap.addMarker(new MarkerOptions()
                .position(STUDENT)
                .title("Student Center")
                .snippet("66 dB")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        //mStudent.showInfoWindow();
    }

    public void switchView(View view) {
        Toast.makeText(this, "switching view...", Toast.LENGTH_SHORT).show();
        Intent mainIntent = new Intent(HeatMap.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void refreshData(View view) {
        Random r = new Random();
        int rand = r.nextInt(46 - 28) + 28;
        String random = Integer.toString(rand);

        if(rand < 45) {
            mWachman = mMap.addMarker(new MarkerOptions()
                    .position(WACHMAN)
                    .title("Wachman Hall")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //mWachman.showInfoWindow();
        } else {
            mWachman = mMap.addMarker(new MarkerOptions()
                    .position(WACHMAN)
                    .title("Wachman")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //mWachman.showInfoWindow();
        }

        rand = r.nextInt(49 - 28) + 28;
        random = Integer.toString(rand);
        if(rand < 45) {
            mPaley = mMap.addMarker(new MarkerOptions()
                    .position(PALEY)
                    .title("Paley Library")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //mPaley.showInfoWindow();
        } else {
            mPaley = mMap.addMarker(new MarkerOptions()
                    .position(PALEY)
                    .title("Paley Library")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //mPaley.showInfoWindow();
        }

        rand = r.nextInt(54 - 28) + 28;
        random = Integer.toString(rand);
        if(rand < 45) {
            mSerc = mMap.addMarker(new MarkerOptions()
                    .position(SERC)
                    .title("SERC")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mSerc.showInfoWindow();
        } else {
            mSerc = mMap.addMarker(new MarkerOptions()
                    .position(SERC)
                    .title("SERC")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mSerc.showInfoWindow();
        }

        rand = r.nextInt(47 - 28) + 28;
        random = Integer.toString(rand);
        if(rand < 45) {
            mTech = mMap.addMarker(new MarkerOptions()
                    .position(TECH)
                    .title("Tech Center")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //mTech.showInfoWindow();
        } else {
            mTech = mMap.addMarker(new MarkerOptions()
                    .position(TECH)
                    .title("Tech Center")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //mTech.showInfoWindow();
        }

        rand = r.nextInt(69 - 28) + 28;
        random = Integer.toString(rand);
        if(rand < 45) {
            mStudent = mMap.addMarker(new MarkerOptions()
                    .position(STUDENT)
                    .title("Student Center")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //mStudent.showInfoWindow();
        } else {
            mStudent = mMap.addMarker(new MarkerOptions()
                    .position(STUDENT)
                    .title("Student Center")
                    .snippet(random+" dB")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            //mStudent.showInfoWindow();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "HeatMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.hongear.quiet_lounge03/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "HeatMap Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.hongear.quiet_lounge03/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
