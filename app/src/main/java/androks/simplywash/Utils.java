package androks.simplywash;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

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
        return  FirebaseDatabase.getInstance().getReference().child("priceList").child(washerId);
    }

    public static DatabaseReference getFavourites(String userId){
        return FirebaseDatabase.getInstance().getReference().child("favourites").child(userId);
    }

    public static String workHoursToString(Washer washer){
        return (washer.workHoursFrom + ":00" + " - " + washer.workHoursTo + ":00");
    }

    public static int getServiceAvailableColor(boolean available){
        return available? R.color.colorAccent: R.color.mdtp_dark_gray;
    }

    public static boolean isWasherOpenAtTheTime(Washer washer){
        if(washer.roundTheClock)
            return true;
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);

        return hours >= washer.workHoursFrom && hours < washer.workHoursTo;
    }

    public static boolean isWasherFits(Washer washer,
                                Context context,
                                boolean displayAllStates,
                                List<String> favouriteWashers){

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(Constants.FILTERS_PREFERENCES, Context.MODE_PRIVATE);
        int[] priceCategoriesInt = context.getResources().getIntArray(R.array.priceCategoriesInt);

        boolean restRoom = sharedPreferences.getBoolean(Constants.FILTER_REST_ROOM, false);
        boolean wifi = sharedPreferences.getBoolean(Constants.FILTER_WIFI, false);
        boolean wc = sharedPreferences.getBoolean(Constants.FILTER_TOILET, false);
        boolean coffee = sharedPreferences.getBoolean(Constants.FILTER_COFFEE, false);
        boolean grocery = sharedPreferences.getBoolean(Constants.FILTER_SHOP, false);
        float rating = sharedPreferences.getFloat(Constants.FILTER_MINIMUM_RATING, 0.0f);
        int priceCategory = sharedPreferences.getInt(Constants.FILTER_PRICE_CATEGORY, priceCategoriesInt.length-1);
        boolean cardPayment =
                sharedPreferences.getBoolean(Constants.FILTER_CARD_PAYMENT, false);
        boolean serviceStation =
                sharedPreferences.getBoolean(Constants.FILTER_SERVICE_STATION, false);
        boolean onlyFavourites =
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_FAVOURITES, false);

        if(restRoom && !washer.restRoom)
            return false;
        if(wifi && !washer.wifi)
            return false;
        if(wc && !washer.toilet)
            return false;
        if(coffee && !washer.coffee)
            return false;
        if(cardPayment && !washer.cardPayment)
            return false;
        if(serviceStation && !washer.serviceStation)
            return false;
        if(grocery && !washer.shop)
            return false;
        if(rating != 0.0f && rating < washer.rating)
            return false;
        if(onlyFavourites && !favouriteWashers.contains(washer.id))
            return false;
        if(priceCategoriesInt[priceCategory] < washer.defaultPrice)
            return false;
        if(!displayAllStates && !washer.state.equals(Constants.AVAILABLE) )
            return false;

        return true;
    }
}
