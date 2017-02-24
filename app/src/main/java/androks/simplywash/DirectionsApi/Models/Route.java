package androks.simplywash.DirectionsApi.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class Route {

    @SerializedName("legs")
    public List<Leg> legs;

    @SerializedName("overview_polyline")
    public OverviewPolyline polyline;

}
