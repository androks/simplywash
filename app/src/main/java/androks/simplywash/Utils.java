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

    private static final String GRAY = "#9E9E9E";
    private static final String BUSY = "busy";
    private static final String AVAILABLE = "available";
    private static final String OFFLINE = "offline";

    public static void setMarkerIcon(Marker marker, String state){
        switch (state){
            case AVAILABLE:
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_GREEN
                ));
                break;

            case OFFLINE:
                marker.setIcon(getMarkerIcon(GRAY));
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
                marker.icon(getMarkerIcon(GRAY));
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
