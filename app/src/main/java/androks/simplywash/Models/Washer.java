package androks.simplywash.Models;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private double langtitude;
    private double longtitude;
    private int boxes;
    private int freeBoxes;
    private int stars;
    private boolean cafe;
    private boolean wifi;
    private boolean restRoom;
    private boolean lunchRoom;
    private boolean wc;
    private boolean tire;
    private String status;
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

    public int getStars() {
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

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    Washer(){}

    public Washer(double langtitude, double longtitude, String id){
        this.langtitude = langtitude;
        this.longtitude = longtitude;
        this.id = id;
    }

    public Washer(double langtitude, double longtitude, int boxes, int freeBoxes, int stars, String status, boolean cafe, boolean wifi, boolean restRoom, boolean lunchRoom, boolean wc, boolean tire, String hours, String id, String name, String phone, String uid, String location, String description) {
        this.langtitude = langtitude;
        this.longtitude = longtitude;
        this.boxes = boxes;
        this.freeBoxes = freeBoxes;
        this.stars = stars;
        this.status = status;
        this.cafe = cafe;
        this.wifi = wifi;
        this.restRoom = restRoom;
        this.lunchRoom = lunchRoom;
        this.wc = wc;
        this.tire = tire;
        this.hours = hours;
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.uid = uid;
        this.location = location;
        this.description = description;
    }
}
