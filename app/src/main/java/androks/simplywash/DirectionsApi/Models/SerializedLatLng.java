package androks.simplywash.DirectionsApi.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by androks on 2/24/2017.
 */

public class SerializedLatLng {
    @SerializedName("lat")
    public double lat;

    @SerializedName("lng")
    public double lng;
}
