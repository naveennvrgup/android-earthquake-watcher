package com.example.earthquakewatcher.Model;

public class EarthQuake {
    private String place,detailLink,type;
    private long time;
    private double mangnitude, lat, lon;

    public EarthQuake(String place, String detailLink, String type, long time, double mangnitude, double lat, double lon) {
        this.place = place;
        this.detailLink = detailLink;
        this.type = type;
        this.time = time;
        this.mangnitude = mangnitude;
        this.lat = lat;
        this.lon = lon;
    }

    public EarthQuake() {
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getMangnitude() {
        return mangnitude;
    }

    public void setMangnitude(double mangnitude) {
        this.mangnitude = mangnitude;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
