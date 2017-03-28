package androks.simplywash.directionsApi.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Mai Thanh Hiep on 4/3/2016.
 */
public class Distance {
    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("value")
    @Expose
    private int value;

    public String getText() {
        return text;
    }

    public int getValue() {
        return value;
    }
}
