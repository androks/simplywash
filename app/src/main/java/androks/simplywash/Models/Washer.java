package androks.simplywash.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    public String id;
    public String userId;
    public String name;
    public String phone;
    public String location;
    public String description;
    public String placeId;
    public String state;
    public double latitude;
    public double longitude;
    public float rating;
    public int boxes;
    public int availableBoxes;
    public int votesCount;
    public int countOfFavourites;
    public int workHoursFrom;
    public int workHoursTo;
    public int defaultPrice;
    public boolean restRoom;
    public boolean wifi;
    public boolean toilet;
    public boolean coffee;
    public boolean shop;
    public boolean cardPayment;
    public boolean serviceStation;
    public boolean roundTheClock;

    @Exclude
    public void decreaseCountOfFavourites(){
        if(countOfFavourites > 0)
            countOfFavourites--;
    }

    @Exclude
    public void increaseCountOfFavourites(){
        countOfFavourites++;
    }

    @Exclude
    public void updateRate(float oldValue, float newValue){
        if(oldValue <= 0.1f)
            rating = ((rating*votesCount)+newValue)/++votesCount;
        else
            rating = ((rating*votesCount-oldValue) + newValue)/votesCount;

        rating = (float) Math.floor(rating) + 0.5f;
    }

    public Washer() {}

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
