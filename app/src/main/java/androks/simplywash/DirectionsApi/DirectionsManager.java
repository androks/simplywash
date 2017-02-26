package androks.simplywash.DirectionsApi;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import androks.simplywash.DirectionsApi.Data.Direction;
import androks.simplywash.DirectionsApi.Models.Route;

/**
 * Created by androks on 2/24/2017.
 */

public class DirectionsManager implements ApiManager.Listener {

    private Listener listener;
    private String tag;

    public interface Listener {
        void onDirectionFindStart();
        void onDirectionReady(Direction direction);
    }

    private DirectionsManager(Listener listener) {
        this.listener = listener;
    }

    public static DirectionsManager with(Listener listener) {
        return new DirectionsManager(listener);
    }

    public void buildDirection(LatLng origin, LatLng destination, String tag) {
        listener.onDirectionFindStart();
        this.tag = tag;
        ApiManager.with(this).getDirection(origin, destination);
    }

    @Override
    public void onDirectionReady(List<Route> routes) {
        if (listener == null)
            return;

        if(routes != null && !routes.isEmpty())
            listener.onDirectionReady(new Direction(routes, tag));
        else
            listener.onDirectionReady(null);
    }

    @Override
    public void onDirectionError() {
        listener.onDirectionReady(null);
    }

}
