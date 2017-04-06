package androks.simplywash.models.entity;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

/**
 * Created by androks on 4/5/2017.
 */

public class WasherPlace {
    private String id;
    private String address;
    private double latitude;
    private double longitude;
    private int priceLevel;
    private String phone;
    private String url;


    public WasherPlace(){}
    public WasherPlace(Place place){
        id = place.getId();
        address = place.getAddress().toString();
        latitude = place.getLatLng().latitude;
        longitude = place.getLatLng().longitude;
        priceLevel = place.getPriceLevel();
        phone = place.getPhoneNumber().toString();
        url = place.getWebsiteUri().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
