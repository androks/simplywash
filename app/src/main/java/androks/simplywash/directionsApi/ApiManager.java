package androks.simplywash.directionsApi;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androks.simplywash.directionsApi.Models.ResponseDirection;
import androks.simplywash.directionsApi.Models.Route;
import androks.simplywash.directionsApi.Utils.LanguageCodes;
import androks.simplywash.directionsApi.Utils.TravelModes;
import androks.simplywash.directionsApi.Utils.UnitModes;
import androks.simplywash.directionsApi.Utils.DirectionsApiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by androks on 2/24/2017.
 */

public class ApiManager {

    private Retrofit retrofit;
    private ApiService service;
    private Listener listener;

    public interface Listener {
        void onDirectionReady(List<Route> routes);
        void onDirectionError();
    }

    private ApiManager(Listener listener) {
        this.listener = listener;
        retrofit = new Retrofit.Builder()
                .baseUrl(DirectionsApiUtils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
    }

    public static ApiManager with(Listener listener) {
        return new ApiManager(listener);
    }

    public void getDirection(LatLng origin, LatLng destination) {

        Map<String, String> options = new HashMap<>();
            options.put(DirectionsApiUtils.KEY, DirectionsApiUtils.GOOGLE_API_KEY);
            options.put(DirectionsApiUtils.ORIGIN, DirectionsApiUtils.getLocation(origin));
            options.put(DirectionsApiUtils.DESTINATION, DirectionsApiUtils.getLocation(destination));
            options.put(DirectionsApiUtils.LANGUAGE, LanguageCodes.ENGLISH);
            options.put(DirectionsApiUtils.UNIT, UnitModes.IMPERIAL);
            options.put(DirectionsApiUtils.MODE, TravelModes.DRIVING);

        Call<ResponseDirection> call = service.buildDirection(options);

        call.enqueue(new Callback<ResponseDirection>() {
            @Override
            public void onResponse(Call<ResponseDirection> call, Response<ResponseDirection> response) {

                ResponseDirection responseDirection = response.body();
                if (responseDirection != null && responseDirection.status.equals(DirectionsApiUtils.STATUS_SUCCESS)) {
                    listener.onDirectionReady(responseDirection.routes);
                } else {
                    listener.onDirectionError();
                }

            }

            @Override
            public void onFailure(Call<ResponseDirection> call, Throwable t) {
                listener.onDirectionError();
            }
        });
    }
}
