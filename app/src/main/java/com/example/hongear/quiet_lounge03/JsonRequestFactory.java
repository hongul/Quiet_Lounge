package com.example.hongear.quiet_lounge03;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

class JsonRequestFactory {

    private final static String GET_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/getLoungeData";
    private final static String POST_URL = "http://quietlounge.us-east-1.elasticbeanstalk.com/inputSound";
    private final Activity activity;              // Placeholder for context
    private final TypedArray loungeIds;           // Resource IDs for views that hold the sound data
    private final TypedArray loungeColors;        // Resource IDs for views that show colors if lounges

    JsonRequestFactory(Activity activity) {
        this.activity = activity;

        loungeIds = activity.getResources().obtainTypedArray(R.array.LoungeDataIds);      // Get Lounge Ids
        loungeColors = activity.getResources().obtainTypedArray(R.array.LoungeColorIds);
    }

    /**
     * Creates a POST request to send to the web server. This request contains
     * the current location and sound levels of the device.
     *
     * @param local - The location info to be sent to the server
     * @return The JsonObjectRequest with the location info
     */
    JsonObjectRequest insertSoundData(final LocationInfo local) {

        return new JsonObjectRequest(Request.Method.POST, POST_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(final JSONObject response) {

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Toast.makeText(activity, response.getString("msg"), Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

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
     * @param main - Is it the MainActivity or not
     * @return The JsonObjectRequest with updated sound data
     */
    JsonObjectRequest getLoungeData(final boolean main) {
        return new JsonObjectRequest(Request.Method.GET, GET_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (main)
                        updateSoundLevels(response);
                    else {
                        HeatMapUpdaterInterface heatMapUpdaterInterface = (HeatMapUpdaterInterface) activity;
                        heatMapUpdaterInterface.updateHeatMap(response);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    /**
     * Creates an identical GET request to getLoungeData(). Used to fill up lounge labels
     * in the heat map
     *
     * @return The JsonObjectRequest with coordinates and label names
     */
    JsonObjectRequest getLoungeCoords() {
        return new JsonObjectRequest(Request.Method.GET, GET_URL,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                HeatMapUpdaterInterface heatMapUpdaterInterface = (HeatMapUpdaterInterface) activity;
                try {
                    heatMapUpdaterInterface.insertLabels(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    /**
     * Changes the main pages data. This includes the sound levels and the color view
     *
     * @param response - GET request response
     * @throws JSONException
     */
    private void updateSoundLevels(JSONObject response) throws JSONException {
        JSONArray dataJsonArray = response.getJSONArray("lounges");
        JSONObject data;
        TextView textView;
        View view;
        double soundLevel;
        DecimalFormat df = new DecimalFormat("#.000");

        for (int i = 0; i < dataJsonArray.length(); i++) {
            data = dataJsonArray.getJSONObject(i);
            textView = (TextView) activity.findViewById(loungeIds.getResourceId(i,0));
            view = activity.findViewById(loungeColors.getResourceId(i,0));
            soundLevel = data.getDouble("lastSoundLevel");
            textView.setText(df.format(soundLevel) + " dB");
            updateLabelColors(view, soundLevel);
        }
    }

    /**
     * Changes the background color of the specified view based on the sound level
     *
     * @param v - The view
     * @param sound - The sound level
     */
    private void updateLabelColors(View v, double sound) {
        String[] colors = activity.getResources().getStringArray(R.array.LabelColors);

        if (sound <= 30) {
            v.setBackgroundColor(Color.parseColor(colors[0]));
        } else if (sound > 30 && sound <= 40) {
            v.setBackgroundColor(Color.parseColor(colors[1]));
        } else if (sound > 40 && sound <= 50) {
            v.setBackgroundColor(Color.parseColor(colors[2]));
        } else if (sound > 50 && sound <= 60) {
            v.setBackgroundColor(Color.parseColor(colors[3]));
        } else if (sound > 60 && sound <= 70) {
            v.setBackgroundColor(Color.parseColor(colors[4]));
        } else if (sound > 70 && sound <= 80) {
            v.setBackgroundColor(Color.parseColor(colors[5]));
        } else if (sound > 80) {
            v.setBackgroundColor(Color.parseColor(colors[6]));
        }
    }

    /**
     * Interface for HeatMapFullscreenActivity to require these methods
     */
    interface HeatMapUpdaterInterface {
        void updateHeatMap(JSONObject response) throws JSONException;
        void insertLabels(JSONObject response) throws JSONException;
    }
}
