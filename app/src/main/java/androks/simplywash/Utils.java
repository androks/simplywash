package androks.simplywash;

import android.graphics.Color;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by androks on 2/25/2017.
 */

public class Utils {

    public static final String BUSY = "busy";
    public static final String AVAILABLE = "available";
    public static final String OFFLINE = "offline";

    public static void setMarkerIcon(Marker marker, String state){
        switch (state){
            case AVAILABLE:
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                ));
                break;

            case OFFLINE:
                //TODO:Change color to gray
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                ));
                break;

            case BUSY:
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                ));
                break;
        }
    }

    public static void setMarkerIcon(MarkerOptions marker, String state){
        switch (state){
            case AVAILABLE:
                marker.icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                ));
                break;

            case OFFLINE:
                //TODO:Change color to gray
                marker.icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                ));
                break;

            case BUSY:
                marker.icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                ));
                break;
        }
    }

    private static BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[2]);
    }
}
