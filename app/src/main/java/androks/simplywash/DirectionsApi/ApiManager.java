package androks.simplywash.DirectionsApi;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androks.simplywash.DirectionsApi.Models.ResponseDirection;
import androks.simplywash.DirectionsApi.Models.Route;
import androks.simplywash.DirectionsApi.Utils.LanguageCodes;
import androks.simplywash.DirectionsApi.Utils.TravelModes;
import androks.simplywash.DirectionsApi.Utils.UnitModes;
import androks.simplywash.DirectionsApi.Utils.Utils;
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
                .baseUrl(Utils.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);
    }

    public static ApiManager with(Listener listener) {
        return new ApiManager(listener);
    }

    public void getDirection(LatLng origin, LatLng destination) {

        Map<String, String> options = new HashMap<>();
            options.put(Utils.KEY, Utils.GOOGLE_API_KEY);
            options.put(Utils.ORIGIN, Utils.getLocation(origin));
            options.put(Utils.DESTINATION, Utils.getLocation(destination));
            options.put(Utils.LANGUAGE, LanguageCodes.ENGLISH);
            options.put(Utils.UNIT, UnitModes.IMPERIAL);
            options.put(Utils.MODE, TravelModes.DRIVING);

        Call<ResponseDirection> call = service.buildDirection(options);

        call.enqueue(new Callback<ResponseDirection>() {
            @Override
            public void onResponse(Call<ResponseDirection> call, Response<ResponseDirection> response) {

                ResponseDirection responseDirection = response.body();
                if (responseDirection != null && responseDirection.status.equals(Utils.STATUS_SUCCESS)) {
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
