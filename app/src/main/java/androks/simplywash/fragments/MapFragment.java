package androks.simplywash.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androks.simplywash.R;
import androks.simplywash.activities.BaseActivity;
import androks.simplywash.activities.FiltersActivity;
import androks.simplywash.activities.PriceActivity;
import androks.simplywash.activities.WasherActivity;
import androks.simplywash.dialogs.FeaturesDialog;
import androks.simplywash.dialogs.ScheduleDialog;
import androks.simplywash.directionsApi.Data.Direction;
import androks.simplywash.directionsApi.DirectionsManager;
import androks.simplywash.models.CameraPosition;
import androks.simplywash.models.Washer;
import androks.simplywash.utils.Constants;
import androks.simplywash.utils.Utils;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        DirectionsManager.Listener {

    /**
     * Define constant values
     **/
    private static final String TAG_CALCULATE_DIS_DUR = "TAG_CALCULATE_DIS_DUR";
    private static final String TAG_BUILD_ROUTE = "TAG_BUILD_ROUTE";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /* End constant values  **/


    /**
     * Binding view with ButterKnife
     **/

    //Binding colors
    @BindColor(R.color.green) int green;
    @BindColor(R.color.red) int red;
    @BindColor(R.color.colorAccent) int colorAccent;
    @BindColor(android.R.color.black) int colorDark;
    @BindView(R.id.progress_bar_horizontal) ProgressBar progressBar;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingLayout;
    @BindView(R.id.tv_name) TextView mName;
    @BindView(R.id.rating_bar) RatingBar ratingBar;
    @BindView(R.id.tv_rating) TextView tvRating;
    @BindView(R.id.tv_count_of_rates) TextView tvCountOfRates;
    @BindView(R.id.tv_location) TextView tvLocation;
    @BindView(R.id.tv_phone) TextView tvPhone;
    @BindView(R.id.tv_schedule) TextView tvSchedule;
    @BindView(R.id.tv_default_price) TextView tvDefaultPrice;
    @BindView(R.id.tv_is_washer_open) TextView tvIsWasherOpen;
    @BindView(R.id.tv_duration) TextView tvDuration;
    @BindView(R.id.iv_wifi) ImageView ivWifi;
    @BindView(R.id.im_coffee) ImageView ivCoffee;
    @BindView(R.id.iv_restRoom) ImageView ivRestRoom;
    @BindView(R.id.im_grocery) ImageView imGrocery;
    @BindView(R.id.im_wc) ImageView imWC;
    @BindView(R.id.im_service_station) ImageView imServiceStation;
    @BindView(R.id.im_card_payment) ImageView imCardPayment;
    @BindView(R.id.fab_location_settings) FloatingActionButton fabLocationSettings;

    private Unbinder unbinder;

    /**
     * Google Maps field and values
     **/
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    /**
     * End Google Maps section
     **/


    //Reference for downloading all washers
    private Query washersReference;

    private FragmentActivity context;
    private static Resources resources;
    private static FirebaseUser user;

    private Washer showingWasher;
    private Washer theNearestFreeWasher;
    public LatLng currentLocation;

    //Relation between markers on map and list of washers
    private HashMap<String, Washer> washersList = new HashMap<>();
    private HashMap<String, Marker> markersList = new HashMap<>();
    private List<String> favouritesWashers = new ArrayList<>();

    private boolean flagFindMyCurrentLocation;
    private boolean flagFindTheNearestWasher;

    private String currentCity;
    private androks.simplywash.models.CameraPosition currentCityCameraPosition;

    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        // FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        currentCity = ((BaseActivity) getActivity()).getCurrentCity();
        washersReference = Utils.getWasher().orderByChild("city").equalTo(currentCity);
        showProgress();

        context = getActivity();
        resources = context.getResources();
        user = ((BaseActivity) context).getCurrentUser();

        setHasOptionsMenu(true);

        loadMap();

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();

        createLocationRequest();

        buildLocationSettingsRequest();

        slidingLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        setAllFlagsToFalse();

        return rootView;
    }


    private void reloadWasher(String id) {
        Utils.getWasher(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Washer washer = dataSnapshot.getValue(Washer.class);
                washersList.put(washer.getId(), washer);
                showWasher(markersList.get(washer.getId()));
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMap() {
        Utils.getCityLocation(currentCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentCityCameraPosition = dataSnapshot.getValue(CameraPosition.class);
                setUpMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadWashers() {
        washersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    washersList.putAll(dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {
                            }
                    ));
                    setMarkers();
                    checkUserFavouriteWashers();
                }else {
                    Toast.makeText(context, R.string.no_washers_in_your_city, Toast.LENGTH_SHORT).show();
                    hideProgress();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (googleApiClient.isConnected())
            startLocationUpdates();
    }


    @Override
    public void onStop() {

        // Disconnecting the client invalidates it.
        stopLocationUpdates();

        // only stop if it's connected, otherwise we crash
        if (googleApiClient != null)
            googleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onDetach() {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        unbinder.unbind();
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.filter:
                startActivityForResult(
                        new Intent(getActivity(), FiltersActivity.class),
                        Constants.REQUEST_FILTER
                );
                return true;

            default:
                break;
        }

        return false;
    }

    @OnClick(R.id.fab_find_the_nearest_washer)
    public void navigateToTheNearestWasher() {
        if (washersList == null || washersList.isEmpty()) {
            Toast.makeText(context, R.string.no_washers_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (theNearestFreeWasher != null) {
            showWasher(markersList.get(theNearestFreeWasher.getId()));
            flagFindTheNearestWasher = false;
            Toast.makeText(context, R.string.nearest_was_found, Toast.LENGTH_SHORT).show();
        } else {
            flagFindTheNearestWasher = true;
            checkLocationSettings();
        }
    }

    @OnClick(R.id.btn_more)
    public void showWasherDetails() {
        Intent intent = new Intent(getActivity(), WasherActivity.class);
        intent.putExtra(Constants.WASHER_ID, showingWasher.getId());
        startActivityForResult(intent, Constants.REQUEST_RATING_CHANGED);
    }

    @OnClick(R.id.fab_location_settings)
    public void findCurrentLocation() {
        flagFindMyCurrentLocation = true;
        checkLocationSettings();
    }

    @OnClick(R.id.ll_services)
    public void showServiceDialog() {
        DialogFragment dialog = FeaturesDialog.newInstance(showingWasher.getFeatures());
        dialog.show(context.getSupportFragmentManager(), "FeaturesDialog");
    }

    @OnClick(R.id.ll_price)
    public void seePrices() {
        Intent intent = new Intent(context, PriceActivity.class);
        intent.putExtra(Constants.WASHER_ID, showingWasher.getId());
        startActivity(intent);
    }

    @OnClick(R.id.ll_schedule)
    public void showScheduleDialog() {
        if (!showingWasher.isRoundTheClock()) {
            AppCompatDialogFragment scheduleDialog = ScheduleDialog.newInstance(showingWasher.getSchedule());
            scheduleDialog.show(context.getSupportFragmentManager(), "Schedule");
        }
    }

    @OnClick(R.id.tv_phone)
    public void callToWasher() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + showingWasher.getPlace().getPhone()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(context.getApplicationContext(), R.string.failed_to_call, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.tv_location)
    public void showWasherOnGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:" + showingWasher.getPlace().getLatitude() + ","
                + showingWasher.getPlace().getLongitude() + "?q="
                + showingWasher.getPlace().getLatitude() + ","
                + showingWasher.getPlace().getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null)
            startActivity(mapIntent);
    }

    private void setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            return;
        }
        findCurrentLocation();
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private void checkUserFavouriteWashers() {
        Utils.getFavourites(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    HashMap<String, Boolean> temp = dataSnapshot.getValue(
                            new GenericTypeIndicator<HashMap<String, Boolean>>() {
                            }
                    );
                    favouritesWashers.clear();
                    favouritesWashers.addAll(temp.keySet());
                    filterWashers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMarkers() {
        map.clear();
        for (Washer washer : washersList.values()) {
            MarkerOptions marker = new MarkerOptions()
                    .title(washer.getId())
                    .position(washer.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_place_default));
            markersList.put(washer.getId(), map.addMarker(marker));
        }
        hideProgress();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                        context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE
                );
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        googleApiClient,
                        locationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                makeRequests();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //Location settings are not satisfied. Show the user a dialog to upgrade location settings
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(context, Constants.REQUEST_CHECK_LOCATION_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    //PendingIntent unable to execute request

                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                setAllFlagsToFalse();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.REQUEST_CHECK_LOCATION_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;

                    case Activity.RESULT_CANCELED:
                        setAllFlagsToFalse();
                        break;
                }
                break;
            case Constants.REQUEST_FILTER:
                switch (resultCode) {
                    case Constants.FILTER_CHANGED_CODE:
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        animateCameraToCurrentCityPosition();
                        filterWashers();
                        break;
                }

            case Constants.REQUEST_RATING_CHANGED:
                switch (resultCode) {
                    case Constants.RATING_CHANGED_CODE:
                        reloadWasher(showingWasher.getId());
                        break;
                }
        }
    }

    private void filterWashers() {
        for (Washer washer : washersList.values()) {
            markersList.get(washer.getId()).setVisible(
                    Utils.isWasherFits(washer, context, favouritesWashers)
            );
        }
    }

    /**
     * Location settings request was accepted, so we can send requests
     */
    private void makeRequests() {
        if (currentLocation != null) {
            if (slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN
                    && showingWasher != null) {
                calculateDistanceAndTime(showingWasher.getLatLng());
            }

            if (flagFindMyCurrentLocation && map != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16));
                fabLocationSettings.setColorFilter(colorAccent);
                flagFindMyCurrentLocation = false;
            }
            if (flagFindTheNearestWasher) {
                navigateToTheNearestWasher();
                flagFindTheNearestWasher = false;
            }
        }
    }

    /**
     * Location settings request was denied
     */
    private void setAllFlagsToFalse() {
        flagFindMyCurrentLocation = false;
        flagFindTheNearestWasher = false;
        fabLocationSettings.setColorFilter(colorDark);
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        // Create the location request
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        locationSettingsRequest = builder.build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setMyLocationUtilsEnabled(true);
        map.setOnMarkerClickListener(this);
        map.setOnMapClickListener(this);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        animateCameraToCurrentCityPosition();
        loadWashers();
    }

    private void animateCameraToCurrentCityPosition() {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(currentCityCameraPosition.getLatitude(),
                        currentCityCameraPosition.getLongitude()
                ),
                currentCityCameraPosition.getZoom()
        ));
    }

    public void setMyLocationUtilsEnabled(boolean value) {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;
        map.setMyLocationEnabled(value);
    }

    private void showWasher(Marker marker) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));

        if (washersList.get(marker.getTitle()).equals(showingWasher) &&
                slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)
            return;

        showingWasher = washersList.get(marker.getTitle());
        tvDuration.setText("");
        makeRequests();

        //Inflating bottom sheet view by washer details
        inflateWasherDetails();
        //Show bottom sheet as collapsed
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showWasher(marker);
        return true;
    }

    private void inflateWasherDetails() {
        mName.setText(showingWasher.getName());
        ratingBar.setRating(showingWasher.getRating());
        tvRating.setText(String.format(Locale.getDefault(), "%.1f", showingWasher.getRating()));
        tvCountOfRates.setText(String.format(Locale.getDefault(), "(%d)", showingWasher.getVotes()));
        tvLocation.setText(showingWasher.getPlace().getStreet());
        if(showingWasher.getPlace().getPhone().isEmpty())
            tvPhone.setText(R.string.no_info);
        else
            tvPhone.setText(showingWasher.getPlace().getPhone());

        if(showingWasher.getDefaultPrice() <= 0)
            tvDefaultPrice.setText(R.string.no_info);
        else
            tvDefaultPrice.setText(String.valueOf(showingWasher.getDefaultPrice()));

        if (showingWasher.isRoundTheClock()) {
            tvSchedule.setText(R.string.round_the_clock);
            tvIsWasherOpen.setText(R.string.open);
            tvIsWasherOpen.setTextColor(green);
        } else {
            if (!showingWasher.getSchedule().getScheduleForToday().isEmpty()) {
                tvSchedule.setText(showingWasher.getSchedule().getScheduleForToday());
                if (Utils.isWasherOpenAtTheTime(showingWasher)) {
                    tvIsWasherOpen.setText(R.string.open);
                    tvIsWasherOpen.setTextColor(green);
                } else {
                    tvIsWasherOpen.setText(R.string.closed);
                    tvIsWasherOpen.setTextColor(red);
                }
            } else {
                tvSchedule.setText(R.string.no_info);
            }
        }

        imWC.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isWc())));
        ivWifi.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isWifi())));
        ivCoffee.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isCoffee())));
        imGrocery.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isShop())));
        ivRestRoom.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isRestRoom())));
        imCardPayment.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isCardPayment())));
        imServiceStation.setColorFilter(resources
                .getColor(Utils.getServiceAvailableColor(showingWasher.getFeatures().isServiceStation())));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null)
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(context, R.string.disconnected, Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(context, R.string.network_lost, Toast.LENGTH_SHORT).show();
        }
    }

    // Trigger new location updates at interval
    private void startLocationUpdates() {
        // Request location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        makeRequests();
        findTheNearestWasher();
    }

    private void findTheNearestWasher() {
        String washerId = null;
        Double[] distances = new Double[washersList.size()];
        Double bestMatch = (double) -1;
        int i = 0;
        for (Washer washer : washersList.values()) {
            distances[i] = SphericalUtil.computeDistanceBetween(
                    currentLocation,
                    washer.getLatLng()
            );
            if (bestMatch.equals((double) -1) || bestMatch > distances[i]) {
                bestMatch = distances[i];
                washerId = washer.getId();
            }

            i++;
        }
        theNearestFreeWasher = washersList.get(washerId);
    }

    private void buildRoute(LatLng origin, LatLng destination) {
        if (origin == null || destination == null) return;
        DirectionsManager.with(this).buildDirection(
                origin,
                destination,
                TAG_BUILD_ROUTE
        );
    }

    private void buildRouteFromCurrentPosition(LatLng destination) {
        if (currentLocation == null || destination == null) return;
        DirectionsManager.with(this).buildDirection(
                currentLocation,
                destination,
                TAG_BUILD_ROUTE
        );
    }

    private void calculateDistanceAndTime(LatLng destination) {
        if (currentLocation == null || destination == null) return;
        DirectionsManager.with(this).buildDirection(
                currentLocation,
                destination,
                TAG_CALCULATE_DIS_DUR
        );
    }

    @Override
    public void onDirectionFindStart() {
        showProgress();
    }

    @Override
    public void onDirectionReady(Direction direction) {
        hideProgress();
        if (direction == null)
            return;

        switch (direction.getTag()) {
            case TAG_CALCULATE_DIS_DUR:
                tvDuration.setVisibility(View.VISIBLE);
                tvDuration.setText(direction.duration.getText());
                break;

            case TAG_BUILD_ROUTE:
                break;
        }
    }

}
