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


    public WasherPlace(){}
    public WasherPlace(Place place){
        id = place.getId();
        address = place.getAddress().toString();
        latitude = place.getLatLng().latitude;
        longitude = place.getLatLng().longitude;
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

    @Exclude
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }
}
