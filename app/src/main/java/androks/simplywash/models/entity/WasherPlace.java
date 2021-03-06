package androks.simplywash.models.entity;

import android.net.Uri;

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
    private String street;


    public WasherPlace(){}
    public WasherPlace(Place place){
        setId(place.getId());
        setAddress(place.getAddress().toString());
        setLatitude(place.getLatLng().latitude);
        setLongitude(place.getLatLng().longitude);
        setPriceLevel(place.getPriceLevel());
        setPhone(place.getPhoneNumber());
        setUrl(place.getWebsiteUri());
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
        if(phone != null)
            this.phone = phone;
        else
            this.phone = "";
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Exclude
    public void setPhone(CharSequence phone) {
        if(phone != null)
            this.phone = phone.toString();
        else
            this.phone = "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if(url != null)
            this.url = url;
        else
            this.url = "";
    }

    @Exclude
    private void setUrl(Uri url) {
        if(url != null)
            this.url = url.getPath();
        else
            this.url = "";
    }

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
