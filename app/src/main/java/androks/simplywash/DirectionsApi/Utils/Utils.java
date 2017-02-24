package androks.simplywash.DirectionsApi.Utils;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by androks on 2/24/2017.
 */

public class Utils {
    public static final String BASE_URL = "https://maps.googleapis.com/";
    public static final String GOOGLE_API_KEY = "AIzaSyBFjK8UInAeNGfhx8attCH8UNY6xzNjuwU";
    public static final String STATUS_SUCCESS = "OK";
    public static final String KEY = "key";
    public static final String DESTINATION = "destination";
    public static final String ORIGIN = "origin";
    public static final String MODE = "mode";
    public static final String LANGUAGE = "language";
    public static final String UNIT = "unit";

    public static String getLocation(LatLng location){
        return (new StringBuilder(60)).append(location.latitude).append(",").append(location.longitude).toString();
    }
}
