package androks.simplywash;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.List;

import androks.simplywash.Enums.WasherStatus;
import androks.simplywash.Models.Washer;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static DatabaseReference getWasher(){
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getScheduleFor(String washerId){
        return FirebaseDatabase.getInstance().getReference().child("schedule").child(washerId);
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

    public static DatabaseReference getOrder(String id){
        return FirebaseDatabase.getInstance().getReference().child("orders").child(id);
    }

    public static String workHoursToString(Washer washer){
        return (washer.getWorkHoursFrom() + ":00" + " - " + washer.getWorkHoursTo() + ":00");
    }

    public static int getServiceAvailableColor(boolean available){
        return available? R.color.colorAccent: R.color.mdtp_dark_gray;
    }

    public static boolean isWasherOpenAtTheTime(Washer washer){
        if(washer.isRoundTheClock())
            return true;
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);

        return hours >= washer.getWorkHoursFrom() && hours < washer.getWorkHoursTo();
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

        if(restRoom && !washer.isRestRoom())
            return false;
        if(wifi && !washer.isWifi())
            return false;
        if(wc && !washer.isWc())
            return false;
        if(coffee && !washer.isCoffee())
            return false;
        if(cardPayment && !washer.isCardPayment())
            return false;
        if(serviceStation && !washer.isServiceStation())
            return false;
        if(grocery && !washer.isShop())
            return false;
        if(rating != 0.0f && rating < washer.getRating())
            return false;
        if(onlyFavourites && !favouriteWashers.contains(washer.getId()))
            return false;
        if(priceCategoriesInt[priceCategory] < washer.getDefaultPrice())
            return false;
        if(!displayAllStates && !(washer.getStatusAsEnum() == WasherStatus.Available))
            return false;

        return true;
    }

    public static int castMinuteToFormat(int minute){
        minute = (minute + 14)/15 * 15;
        if(minute == 60)
            minute = 0;
        return minute;
    }
}
