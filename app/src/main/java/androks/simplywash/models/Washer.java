package androks.simplywash.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

import androks.simplywash.enums.WasherStatus;
import androks.simplywash.enums.WasherType;
import androks.simplywash.models.entity.Features;
import androks.simplywash.models.entity.WasherPhoto;
import androks.simplywash.models.entity.WasherPlace;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private List<WasherPhoto> photos;
    private Features features;
    private Schedule schedule;
    private WasherStatus state;
    private WasherType type;
    private WasherPlace place;
    private String city;
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String description;
    private float rating;
    private int boxes;
    private int availableBoxes;
    private int votes;
    private int favorites;
    private int defaultPrice;
    private boolean roundTheClock;

    public Washer() {}

    @Exclude
    public void setDefaultValues(){
        features = new Features();
        schedule = new Schedule();
        state = WasherStatus.Offline;
        type = WasherType.Normal;
        place = new WasherPlace();
        id = "";
        userId = "";
        name = "";
        description = "";
        rating = 0f;
        boxes = 0;
        availableBoxes = 0;
        votes = 0;
        favorites = 0;
        defaultPrice = 0;
        roundTheClock = false;
        photos = new ArrayList<>();
    }

    @Exclude
    public void setState(WasherStatus state) {
        this.state = state;
    }

    @Exclude
    public void setType(WasherType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
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

    @Exclude
    public LatLng getLatLng(){
        return place.getLatLng();
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

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Features getFeatures() {
        return features;
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public boolean isRoundTheClock() {
        return roundTheClock;
    }

    public void setRoundTheClock(boolean roundTheClock) {
        this.roundTheClock = roundTheClock;
    }

    public WasherPlace getPlace() {
        return place;
    }

    public void setPlace(WasherPlace place) {
        this.place = place;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<WasherPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<WasherPhoto> photos) {
        this.photos = photos;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
