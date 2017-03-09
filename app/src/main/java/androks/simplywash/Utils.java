package androks.simplywash;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import androks.simplywash.Models.Washer;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static void setMarkerIcon(Marker marker, String state){
        switch (state){
            case Constants.AVAILABLE:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_green));
                break;

            case Constants.OFFLINE:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_gray));
                break;

            case Constants.BUSY:
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_red));
                break;
        }
    }

    public static void setMarkerIcon(MarkerOptions marker, String state){
        switch (state){
            case Constants.AVAILABLE:
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_green));
                break;

            case Constants.OFFLINE:
               // marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_gray));
                break;

            case Constants.BUSY:
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

    public static DatabaseReference getUserReview(String washerId, String userPhone){
        return  FirebaseDatabase.getInstance().getReference().child("reviews").child(washerId).child(userPhone);
    }

    public static DatabaseReference getExpandedReviews(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("full-reviews").child(washerId);
    }

    public static DatabaseReference getUserInfo(String userId){
        return  FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public static DatabaseReference getPricesFor(String washerId){
        return  FirebaseDatabase.getInstance().getReference().child("prices").child(washerId);
    }

    public static String workHoursToString(Washer washer){
        return (washer.getWorkHoursFrom() + ":00" + " - " + washer.getWorkHoursTo() + ":00");
    }

    public static int getServiceAvailabledColor(boolean available){
        return available? R.color.colorAccent: R.color.black_trans80;
    }

    public static boolean isWasherOpenAtTheTime(Washer washer){
        if(washer.isRoundTheClock())
            return true;
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);

        return hours >= washer.getWorkHoursFrom() && hours < washer.getWorkHoursTo();
    }
}
