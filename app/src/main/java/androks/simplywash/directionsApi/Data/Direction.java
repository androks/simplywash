package androks.simplywash.directionsApi.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

import androks.simplywash.directionsApi.Models.Distance;
import androks.simplywash.directionsApi.Models.Duration;
import androks.simplywash.directionsApi.Models.Leg;
import androks.simplywash.directionsApi.Models.Route;

/**
 * Created by androks on 2/24/2017.
 */

public class Direction {
    private String startAddress;
    private String endAddress;
    public Distance distance;
    public Duration duration;
    public LatLng startLocation;
    public LatLng endLocation;
    private String tag;

    public List<LatLng> points;

    public Direction(List<Route> routes, String tag){
        Route route = routes.get(0);
        Leg leg = route.legs.get(0);
        startAddress = leg.getStartAddress();
        endAddress = leg.getEndAddress();
        startLocation = new LatLng(leg.startLocation.lat, leg.startLocation.lng);
        endLocation = new LatLng(leg.endLocation.lat, leg.endLocation.lng);
        distance = leg.distance;
        duration = leg.duration;
        points = PolyUtil.decode(route.polyline.getPoints());
        this.tag = tag;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public String getTag() {
        return tag;
    }
}
