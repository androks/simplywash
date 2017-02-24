package androks.simplywash.DirectionsApi.Models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by androks on 2/24/2017.
 */

public class ResponseDirection {

    @SerializedName("status")
    public String status;

    @SerializedName("routes")
    public List<Route> routes;

}
