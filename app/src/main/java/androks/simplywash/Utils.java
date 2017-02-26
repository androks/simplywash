package androks.simplywash;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static final int REQUEST_CHECK_LOCATION_SETTINGS = 11;
    public static final String BUSY = "busy";
    public static final String AVAILABLE = "available";
    public static final String OFFLINE = "offline";

    public static void setMarkerIcon(Marker marker, String state){
        switch (state){
            case AVAILABLE:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_green));
                break;

            case OFFLINE:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_gray));
                break;

            case BUSY:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_red));
                break;
        }
    }

    public static void setMarkerIcon(MarkerOptions marker, String state){
        switch (state){
            case AVAILABLE:
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_green));
                break;

            case OFFLINE:
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_gray));
                break;

            case BUSY:
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_red));
                break;
        }
    }

    public static DatabaseReference getWasher(){
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getWasher(String id){
        return FirebaseDatabase.getInstance().getReference().child("washers").child(id);
    }

    public static DatabaseReference getStatesOfWashers(){
        return  FirebaseDatabase.getInstance().getReference().child("states");
    }

    public static DatabaseReference getReviewsFor(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("reviews").child(washerId);
    }

    public static DatabaseReference getUserInfo(String userId){
        return  FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public static DatabaseReference getPricesFor(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("prices").child(washerId);
    }
}
