package com.example.hongear.quiet_lounge03;

import android.app.Activity;
import android.content.res.TypedArray;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;

/**
 * Created by kyleoneill on 11/17/16.
 */

public class JsonRequestFactory {

    private final static String GET_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/getLoungeData";
    private final static String POST_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/inputSound";
    private Activity activity;
    private TypedArray loungeIds;           // Resource IDs for views that hold the sound data

    public JsonRequestFactory(Activity activity) {
        this.activity = activity;

        // Get Lounge Ids
        loungeIds = activity.getResources().obtainTypedArray(R.array.LoungeDataIds);
    }

    /**
     * Creates a POST request to send to the web server. This request contains
     * the current location and sound levels of the device.
     *
     * @param local - The location info to be sent to the server
     * @return The JsonObjectRequest with the location info
     */
    public JsonObjectRequest insertSoundData(final LocationInfo local) {

        return new JsonObjectRequest(Request.Method.POST, POST_URL, null,
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
                    bodyStr = "lat=" + local.getLat() + "&lng=" + local.getLng() + "&sound=" + local.getSound();      // Use with phone
//                    bodyStr = "lat=" + local.getLat() + "&lng=" + local.getLng() + "&sound=" + String.valueOf(Math.random() * 30); // Use only with emulator
                    return bodyStr.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    /**
     * Creates a GET request to send to the web server. The request returns the latest update
     * sound levels for the tracked lounges
     *
     * @return The JsonObjectRequest with updated sound data
     */
    public JsonObjectRequest getLoungeData(final boolean main) {
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, GET_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (main)
                        updateSoundLevels(response);
                    else
                        updateHeatMap(response);
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

    public void updateSoundLevels(JSONObject response) throws JSONException {
        JSONArray dataJsonArray = response.getJSONArray("lounges");
        JSONObject data;
        TextView textView;
        String soundLevel;
        DecimalFormat df = new DecimalFormat("#.###");

        for (int i = 0; i < dataJsonArray.length(); i++) {
            data = dataJsonArray.getJSONObject(i);
            textView = (TextView) activity.findViewById(loungeIds.getResourceId(i,0));
            soundLevel = df.format(data.getDouble("lastSoundLevel"));
            textView.setText(soundLevel);
        }
    }

    public void updateHeatMap(JSONObject response) throws JSONException {
        // TODO Update Heat Map Markers with data from response
    }
}
