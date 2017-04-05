package androks.simplywash.models;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import androks.simplywash.enums.WasherStatus;
import androks.simplywash.enums.WasherType;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private Schedule schedule;
    private WasherStatus state;
    private WasherType type;
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String location;
    private String description;
    private String placeId;
    private double latitude;
    private double longitude;
    private float rating;
    private int boxes;
    private int availableBoxes;
    private int votes;
    private int favorites;
    private int defaultPrice;
    private boolean restRoom;
    private boolean wifi;
    private boolean wc;
    private boolean coffee;
    private boolean shop;
    private boolean cardPayment;
    private boolean serviceStation;
    private boolean roundTheClock;

    public Washer(WasherType type, String id, String name, String phone, String description, int boxes, int defaultPrice, boolean restRoom, boolean wifi, boolean wc, boolean coffee, boolean shop, boolean cardPayment, boolean serviceStation, boolean roundTheClock, Place place) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.boxes = boxes;
        this.defaultPrice = defaultPrice;
        this.restRoom = restRoom;
        this.wifi = wifi;
        this.wc = wc;
        this.coffee = coffee;
        this.shop = shop;
        this.cardPayment = cardPayment;
        this.serviceStation = serviceStation;
        this.roundTheClock = roundTheClock;
        schedule = new Schedule();
        state = WasherStatus.Offline;
        userId = "";
        placeId = place.getId();
        latitude = place.getLatLng().latitude;
        longitude = place.getLatLng().longitude;
        rating = 0f;
        availableBoxes = 0;
        votes = 0;
        favorites = 0;
    }

    @Exclude
    public int decreaseCountOfFavourites(){
        if(favorites > 0)
            favorites--;
        return favorites;
    }

    @Exclude
    public int increaseCountOfFavourites(){
        return ++favorites;
    }

    @Exclude
    public int increaseCountOfVotes(){
        return ++votes;
    }

    @Exclude
    public void updateRate(float oldValue, float newValue){
        if(oldValue <= 0.1f)
            rating = ((rating* votes)+newValue)/++votes;
        else
            rating = ((rating* votes -oldValue) + newValue)/ votes;

        rating = (float) Math.floor(rating) + 0.5f;
    }

    public Washer() {}

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    @Exclude
    public WasherStatus getStatusAsEnum(){
        return state;
    }

    @Exclude
    public WasherType getTypeAsEnum(){
        return type;
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getState(){
        return state.name();
    }

    public void setState(String state) {
        this.state = WasherStatus.valueOf(state);
    }

    // these methods are just a Firebase 9.0.0 hack to handle the enum
    public String getType(){
        return type.name();
    }

    public void setType(String type) {
        this.type = WasherType.valueOf(type);
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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

    public int getVotes() {
        return votes;
    }

    public int getFavorites() {
        return favorites;
    }

    public int getDefaultPrice() {
        return defaultPrice;
    }

    public boolean isRestRoom() {
        return restRoom;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isWc() {
        return wc;
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

    public boolean isRoundTheClock() {
        return roundTheClock;
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

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public void setDefaultPrice(int defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public void setRestRoom(boolean restRoom) {
        this.restRoom = restRoom;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public void setWc(boolean wc) {
        this.wc = wc;
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

    public void setRoundTheClock(boolean roundTheClock) {
        this.roundTheClock = roundTheClock;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
}
