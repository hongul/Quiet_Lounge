package com.example.hongear.quiet_lounge03;

/**
 * Created by kyleoneill on 11/7/16.
 */

class LocationInfo {

    private double lat;
    private double lng;
    private double sound;

    public LocationInfo() {
        this.lat = 0;
        this.lng = 0;
        this.sound = 0;
    }

    public LocationInfo(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
        this.sound = 0;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getSound() {
        return sound;
    }

    public void setSound(double sound) {
        this.sound = sound;
    }
}
