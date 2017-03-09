package androks.simplywash.Models;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String location;
    private String description;
    private String placeId;
    private String state;
    private double langtitude;
    private double longtitude;
    private float rating;
    private int boxes;
    private int availableBoxes;
    private int votesCount;
    private int countOfFavourites;
    private int workHoursFrom;
    private int workHoursTo;
    private boolean restRoom;
    private boolean wifi;
    private boolean toilet;
    private boolean coffee;
    private boolean shop;
    private boolean cardPayment;
    private boolean serviceStation;

    public Washer() {}

    public int getCountOfFavourites() {
        return countOfFavourites;
    }

    public void setCountOfFavourites(int countOfFavourites) {
        this.countOfFavourites = countOfFavourites;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
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

    public float getRating() {
        return rating;
    }

    public int getBoxes() {
        return boxes;
    }

    public int getAvailableBoxes() {
        return availableBoxes;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public int getWorkHoursFrom() {
        return workHoursFrom;
    }

    public int getWorkHoursTo() {
        return workHoursTo;
    }

    public boolean isRestRoom() {
        return restRoom;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isToilet() {
        return toilet;
    }

    public boolean isCoffee() {
        return coffee;
    }

    public boolean isShop() {
        return shop;
    }

    public boolean isCardPayment() {
        return cardPayment;
    }

    public boolean isServiceStation() {
        return serviceStation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setLangtitude(double langtitude) {
        this.langtitude = langtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setBoxes(int boxes) {
        this.boxes = boxes;
    }

    public void setAvailableBoxes(int availableBoxes) {
        this.availableBoxes = availableBoxes;
    }

    public void setVotesCount(int votesCount) {
        this.votesCount = votesCount;
    }

    public void setWorkHoursFrom(int workHoursFrom) {
        this.workHoursFrom = workHoursFrom;
    }

    public void setWorkHoursTo(int workHoursTo) {
        this.workHoursTo = workHoursTo;
    }

    public void setRestRoom(boolean restRoom) {
        this.restRoom = restRoom;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public void setToilet(boolean toilet) {
        this.toilet = toilet;
    }

    public void setCoffee(boolean coffee) {
        this.coffee = coffee;
    }

    public void setShop(boolean shop) {
        this.shop = shop;
    }

    public void setCardPayment(boolean cardPayment) {
        this.cardPayment = cardPayment;
    }

    public void setServiceStation(boolean serviceStation) {
        this.serviceStation = serviceStation;
    }

    public LatLng getLatLng(){
        return new LatLng(langtitude, longtitude);
    }
}
