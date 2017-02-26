package androks.simplywash.DirectionsApi.Data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.List;

import androks.simplywash.DirectionsApi.Models.Distance;
import androks.simplywash.DirectionsApi.Models.Duration;
import androks.simplywash.DirectionsApi.Models.Leg;
import androks.simplywash.DirectionsApi.Models.Route;

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
    public String mode;

    public List<LatLng> points;

    public Direction(List<Route> routes, String mode){
        Route route = routes.get(0);
        Leg leg = route.legs.get(0);
        startAddress = leg.getStartAddress();
        endAddress = leg.getEndAddress();
        startLocation = new LatLng(leg.startLocation.lat, leg.startLocation.lng);
        endLocation = new LatLng(leg.endLocation.lat, leg.endLocation.lng);
        distance = leg.distance;
        duration = leg.duration;
        points = PolyUtil.decode(route.polyline.getPoints());
        this.mode = mode;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }
}
