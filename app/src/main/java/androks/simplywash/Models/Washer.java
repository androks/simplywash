package androks.simplywash.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private double langtitude;
    private double longtitude;
    private int boxes;
    private int freeBoxes;
    private int votes;
    private float stars;
    private boolean cafe;
    private boolean wifi;
    private boolean restRoom;
    private boolean lunchRoom;
    private boolean wc;
    private boolean tire;
    private String state;
    private String hours;
    private String id;
    private String name;
    private String phone;
    private String uid;
    private String location;
    private String description;

    public boolean getTire() {
        return tire;
    }

    public boolean getWc() {
        return wc;
    }

    public float getStars() {
        return stars;
    }

    public String getDescription() {
        return description;
    }

    public boolean getLunchRoom() {
        return lunchRoom;
    }

    public int getBoxes() {
        return boxes;
    }

    public int getFreeBoxes() {
        return freeBoxes;
    }

    public boolean getRestRoom() {
        return restRoom;
    }

    public String getLocation() {
        return location;
    }

    public LatLng getLanLng(){
        return new LatLng(langtitude, longtitude);
    }

    public void setState(String status) {
        this.state = status;
    }

    public String getState() {
        return state;
    }

    public double getLangtitude() {
        return langtitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public boolean getCafe() {
        return cafe;
    }

    public boolean getWifi() {
        return wifi;
    }

    public String getHours() {
        return hours;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getUid() {
        return uid;
    }

    public int getVotes() {
        return votes;
    }

    Washer(){}
}
