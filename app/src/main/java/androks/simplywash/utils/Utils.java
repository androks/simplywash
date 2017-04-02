package androks.simplywash.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.List;

import androks.simplywash.R;
import androks.simplywash.directionsApi.Data.Direction;
import androks.simplywash.models.Washer;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static DatabaseReference getCityLocation(String city){
        return FirebaseDatabase.getInstance().getReference().child("cityLocation").child(city);
    }

    public static DatabaseReference getWasher(){
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getWashersInCity(String city){
        return FirebaseDatabase.getInstance().getReference().child("washers_info").child(city).child("washers");
    }

    public static DatabaseReference getWasherInCity(String city, String id){
        return FirebaseDatabase.getInstance().getReference().child("washers_info").child(city).child("washers").child(id);
    }

    public static DatabaseReference getPhotos(String id){
        return FirebaseDatabase.getInstance().getReference().child("photos").child(id);
    }

    public static DatabaseReference getListOfCities(){
        return FirebaseDatabase.getInstance().getReference().child("cities");
    }

    public static StorageReference getPhotoStorageRef(String id){
        return FirebaseStorage.getInstance().getReference().child("washer_images").child(id);
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

    public static String distanceDurationToString(Direction direction){
        return direction.distance.getText() + " " + "(" + direction.duration.getText() + ") ";
    }

    public static int getServiceAvailableColor(boolean available){
        return available? R.color.colorAccent: R.color.mdtp_dark_gray;
    }

    public static boolean isWasherOpenAtTheTime(Washer washer){
        if(washer.isRoundTheClock())
            return true;
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);
        String workingHours = washer.getSchedule().getScheduleForToday();
        String[] temp = workingHours.split("-");
        if(temp.length < 2)
            return false;
        int workHoursFrom = getHourFromString(temp[0]);
        int workHoursTo = getHourFromString(temp[1]);

        return hours >= workHoursFrom && hours < workHoursTo;
    }

    private static int getHourFromString(String time){
        try{
            return Integer.valueOf(time.split(":")[0]);
        }catch (NumberFormatException e){
            return 0;
        }
    }

    public static boolean isWasherFits(Washer washer,
                                Context context,
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
        boolean onlyOpen =
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_OPEN, false);

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
        if(onlyOpen && !isWasherOpenAtTheTime(washer))
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

        return true;
    }

    public static int castMinuteToFormat(int minute){
        minute = (minute + 14)/15 * 15;
        if(minute == 60)
            minute = 0;
        return minute;
    }
}
