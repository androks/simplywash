package androks.simplywash.DirectionsApi;

import java.util.Map;

import androks.simplywash.DirectionsApi.Models.ResponseDirection;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

/**
 * Created by androks on 2/23/2017.
 */

public interface ApiService {
    @GET("maps/api/directions/json")
    Call<ResponseDirection> buildDirection(@QueryMap Map<String, String> options);
}
