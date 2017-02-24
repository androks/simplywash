package androks.simplywash.DirectionsApi.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by androks on 2/24/2017.
 */

public class Leg {
    @SerializedName("distance")
    public Distance distance;

    @SerializedName("duration")
    public Duration duration;

    @SerializedName("start_address")
    @Expose
    private String startAddress;

    @SerializedName("end_address")
    @Expose
    private String endAddress;

    @SerializedName("start_location")
    public SerializedLatLng startLocation;

    @SerializedName("end_location")
    public SerializedLatLng endLocation;

    public String getEndAddress() {
        return endAddress;
    }

    public String getStartAddress() {
        return startAddress;
    }
}
