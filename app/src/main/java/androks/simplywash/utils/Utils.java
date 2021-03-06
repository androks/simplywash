package androks.simplywash.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.places.Place;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import androks.simplywash.R;
import androks.simplywash.directionsApi.Data.Direction;
import androks.simplywash.models.Washer;
import androks.simplywash.models.entity.Features;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static DatabaseReference getCityLocation(String city) {
        return FirebaseDatabase.getInstance().getReference().child("cityLocation").child(city);
    }

    public static DatabaseReference getWasher() {
        return FirebaseDatabase.getInstance().getReference().child("washers");
    }

    public static DatabaseReference getListOfCities() {
        return FirebaseDatabase.getInstance().getReference().child("cities");
    }

    public static StorageReference getPhotoStorageRef(String id) {
        return FirebaseStorage.getInstance().getReference().child("washer_images").child(id);
    }

    public static DatabaseReference getPhotoDatabaseReference(String id){
        return Utils.getWasher(id).child("photos");
    }

    public static StorageReference getPhotoStorageRef(String id, String photoName) {
        return FirebaseStorage.getInstance().getReference().child("washer_images").child(id).child(photoName);
    }

    public static DatabaseReference getSuggestedWasher(){
        return FirebaseDatabase.getInstance().getReference().child("suggested_washers");
    }

    public static DatabaseReference getScheduleFor(String washerId) {
        return FirebaseDatabase.getInstance().getReference().child("schedule").child(washerId);
    }

    public static DatabaseReference getWasher(String id) {
        return FirebaseDatabase.getInstance().getReference().child("washers").child(id);
    }

    public static DatabaseReference getStatesOfWashers() {
        return FirebaseDatabase.getInstance().getReference().child("states");
    }

    public static DatabaseReference getReviewsFor(String washerId) {
        return FirebaseDatabase.getInstance().getReference().child("reviews").child(washerId);
    }

    public static DatabaseReference getUserReview(String washerId, String userPhone) {
        return FirebaseDatabase.getInstance().getReference().child("reviews").child(washerId).child(userPhone);
    }

    public static DatabaseReference getUserInfo(String userId) {
        return FirebaseDatabase.getInstance().getReference().child("users").child(userId);
    }

    public static DatabaseReference getPricesFor(String washerId) {
        return FirebaseDatabase.getInstance().getReference().child("priceList").child(washerId);
    }

    public static DatabaseReference getFavourites(String userId) {
        return FirebaseDatabase.getInstance().getReference().child("favourites").child(userId);
    }

    public static DatabaseReference getOrder(String id) {
        return FirebaseDatabase.getInstance().getReference().child("orders").child(id);
    }

    public static String distanceDurationToString(Direction direction) {
        return direction.distance.getText() + " " + "(" + direction.duration.getText() + ") ";
    }

    public static int getServiceAvailableColor(boolean available) {
        return available ? R.color.colorAccent : R.color.mdtp_dark_gray;
    }

    public static boolean isWasherOpenAtTheTime(Washer washer) {
        if (washer.isRoundTheClock())
            return true;
        Calendar now = Calendar.getInstance();
        int hours = now.get(Calendar.HOUR_OF_DAY);
        String workingHours = washer.getSchedule().getScheduleForToday();
        String[] temp = workingHours.split("-");
        if (temp.length < 2)
            return false;
        try{
            int workHoursFrom = Integer.valueOf(temp[0]);
            int workHoursTo = Integer.valueOf(temp[1]);
            return hours >= workHoursFrom && hours < workHoursTo;
        }catch (NumberFormatException e){
            return false;
        }
    }

    public static String servicesToString() {
        String temp = "";

        return temp;
    }

    public static boolean isWasherFits(Washer washer,
                                       Context context,
                                       List<String> favouriteWashers) {

        SharedPreferences sharedPreferences = context
                .getSharedPreferences(Constants.FILTERS_PREFERENCES, Context.MODE_PRIVATE);
        int[] priceCategoriesInt = context.getResources().getIntArray(R.array.priceCategoriesInt);

        boolean restRoom = sharedPreferences.getBoolean(Constants.FILTER_REST_ROOM, false);
        boolean wifi = sharedPreferences.getBoolean(Constants.FILTER_WIFI, false);
        boolean wc = sharedPreferences.getBoolean(Constants.FILTER_TOILET, false);
        boolean coffee = sharedPreferences.getBoolean(Constants.FILTER_COFFEE, false);
        boolean grocery = sharedPreferences.getBoolean(Constants.FILTER_SHOP, false);
        float rating = sharedPreferences.getFloat(Constants.FILTER_MINIMUM_RATING, 0.0f);
        int priceCategory = sharedPreferences.getInt(Constants.FILTER_PRICE_CATEGORY, priceCategoriesInt.length - 1);
        boolean cardPayment =
                sharedPreferences.getBoolean(Constants.FILTER_CARD_PAYMENT, false);
        boolean serviceStation =
                sharedPreferences.getBoolean(Constants.FILTER_SERVICE_STATION, false);
        boolean onlyFavourites =
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_FAVOURITES, false);
        boolean onlyOpen =
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_OPEN, false);

        if (restRoom && !washer.getFeatures().isRestRoom())
            return false;
        if (wifi && !washer.getFeatures().isWifi())
            return false;
        if (wc && !washer.getFeatures().isWc())
            return false;
        if (coffee && !washer.getFeatures().isCoffee())
            return false;
        if (cardPayment && !washer.getFeatures().isCardPayment())
            return false;
        if (onlyOpen && !isWasherOpenAtTheTime(washer))
            return false;
        if (serviceStation && !washer.getFeatures().isServiceStation())
            return false;
        if (grocery && !washer.getFeatures().isShop())
            return false;
        if (rating != 0.0f && rating < washer.getRating())
            return false;
        if (onlyFavourites && !favouriteWashers.contains(washer.getId()))
            return false;
        if (priceCategoriesInt[priceCategory] < washer.getDefaultPrice())
            return false;

        return true;
    }

    public static int castMinuteToFormat(int minute) {
        minute = (minute + 14) / 15 * 15;
        if (minute == 60)
            minute = 0;
        return minute;
    }

    public static String featuresToString(Features features, Resources resources) {
        String temp = "";
        if (features.isWc())
            temp += resources.getString(R.string.wc) + " ";
        if (features.isRestRoom())
            temp += resources.getString(R.string.rest_room) + " ";
        if (features.isWifi())
            temp += resources.getString(R.string.wifi) + " ";
        if (features.isCoffee())
            temp += resources.getString(R.string.coffee) + " ";
        if (features.isShop())
            temp += resources.getString(R.string.shop) + " ";
        if (features.isCardPayment())
            temp += resources.getString(R.string.card_payment) + " ";
        if (features.isServiceStation())
            temp += resources.getString(R.string.service_station) + " ";

        return temp;
    }

    public static String getCityFromPlace(Place place, Context context) {
        Geocoder geocoder = new Geocoder(context);
        String city = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
            city = addresses.get(0).getAddressLine(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return city;
    }

    public static String getStreetFromPlace(Place place, Context context) {
        Geocoder geocoder = new Geocoder(context);
        String address = place.getAddress().toString();
        try {
            List<Address> addresses = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }
}
