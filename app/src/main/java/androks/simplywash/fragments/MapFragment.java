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
    @BindColor(R.color.green)
    int green;
    @BindColor(R.color.red)
    int red;
    @BindColor(R.color.colorAccent)
    int colorAccent;
    @BindColor(android.R.color.black)
    int colorDark;
    @BindView(R.id.progress_horizontal)
    ProgressBar mProgressBar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;
    @BindView(R.id.rating_text)
    TextView mRatingText;
    @BindView(R.id.count_of_rates)
    TextView mCountOfRates;
    @BindView(R.id.location)
    TextView mLocation;
    @BindView(R.id.phone)
    TextView mPhone;
    @BindView(R.id.schedule)
    TextView mSchedule;
    @BindView(R.id.default_price)
    TextView mDefaultPrice;
    @BindView(R.id.is_washer_open)
    TextView mIsWasherOpen;
    @BindView(R.id.duration)
    TextView mDuration;
    @BindView(R.id.wifi)
    ImageView mWifi;
    @BindView(R.id.coffee)
    ImageView mCoffee;
    @BindView(R.id.restRoom)
    ImageView mRestRoom;
    @BindView(R.id.grocery)
    ImageView mGrocery;
    @BindView(R.id.wc)
    ImageView mWC;
    @BindView(R.id.serviceStation)
    ImageView mServiceStation;
    @BindView(R.id.cardPayment)
    ImageView mCardPayment;
    @BindView(R.id.fab_location_settings)
    FloatingActionButton mMyLocationFab;

    private Unbinder mUnbinder;
    /** End bindings  **/

    /**
     * Google Maps field and values
     **/
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * End Google Maps section
     **/


    //Reference for downloading all washers
    private Query mWashersReference;

    private FragmentActivity mContext;
    private static Resources mResources;
    private static FirebaseUser mUser;

    private Washer mShowingWasher;
    private Washer mTheNearestFreeWasher;
    public LatLng mCurrentLocation;

    //Relation between markers on map and list of washers
    private HashMap<String, Washer> mWashersList = new HashMap<>();
    private HashMap<String, Marker> mMarkersList = new HashMap<>();
    private List<String> mFavouritesWashers = new ArrayList<>();

    private boolean FLAG_FIND_MY_CURRENT_LOCATION;
    private boolean FLAG_FIND_NEAREST_WASHER;

    private String mCurrentCity;
    private androks.simplywash.models.CameraPosition mCurrentCityCameraPosition;

    public MapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        // FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG);
        mCurrentCity = ((BaseActivity) getActivity()).getCurrentCity();
        mWashersReference = Utils.getWasher().orderByChild("city").equalTo(mCurrentCity);
        showProgress();

        mContext = getActivity();
        mResources = mContext.getResources();
        mUser = ((BaseActivity) mContext).getCurrentUser();

        setHasOptionsMenu(true);

        loadMap();

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();

        createLocationRequest();

        buildLocationSettingsRequest();

        mSlidingLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
                mWashersList.put(washer.getId(), washer);
                showWasher(mMarkersList.get(washer.getId()));
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMap() {
        Utils.getCityLocation(mCurrentCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentCityCameraPosition = dataSnapshot.getValue(CameraPosition.class);
                setUpMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadWashers() {
        mWashersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mWashersList.putAll(dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {
                            }
                    ));
                    setMarkers();
                    checkUserFavouriteWashers();
                }else {
                    Toast.makeText(mContext, R.string.no_washers_in_your_city, Toast.LENGTH_SHORT).show();
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
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected())
            startLocationUpdates();
    }


    @Override
    public void onStop() {

        // Disconnecting the client invalidates it.
        stopLocationUpdates();

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onDetach() {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mUnbinder.unbind();
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
        if (mWashersList == null || mWashersList.isEmpty()) {
            Toast.makeText(mContext, R.string.no_washers_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mTheNearestFreeWasher != null) {
            showWasher(mMarkersList.get(mTheNearestFreeWasher.getId()));
            FLAG_FIND_NEAREST_WASHER = false;
            Toast.makeText(mContext, R.string.nearest_was_found, Toast.LENGTH_SHORT).show();
        } else {
            FLAG_FIND_NEAREST_WASHER = true;
            checkLocationSettings();
        }
    }

    @OnClick(R.id.moreBtn)
    public void showWasherDetails() {
        Intent intent = new Intent(getActivity(), WasherActivity.class);
        intent.putExtra(Constants.WASHER_ID, mShowingWasher.getId());
        startActivityForResult(intent, Constants.REQUEST_RATING_CHANGED);
    }

    @OnClick(R.id.fab_location_settings)
    public void findCurrentLocation() {
        FLAG_FIND_MY_CURRENT_LOCATION = true;
        checkLocationSettings();
    }

    @OnClick(R.id.services)
    public void showServiceDialog() {
        DialogFragment dialog = FeaturesDialog.newInstance(mShowingWasher.getFeatures());
        dialog.show(mContext.getSupportFragmentManager(), "FeaturesDialog");
    }

    @OnClick(R.id.price_layout)
    public void seePrices() {
        Intent intent = new Intent(mContext, PriceActivity.class);
        intent.putExtra(Constants.WASHER_ID, mShowingWasher.getId());
        startActivity(intent);
    }

    @OnClick(R.id.schedule_layout)
    public void showScheduleDialog() {
        if (!mShowingWasher.isRoundTheClock()) {
            AppCompatDialogFragment scheduleDialog = ScheduleDialog.newInstance(mShowingWasher.getSchedule());
            scheduleDialog.show(mContext.getSupportFragmentManager(), "Schedule");
        }
    }

    @OnClick(R.id.phone)
    public void callToWasher() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mShowingWasher.getPlace().getPhone()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(mContext.getApplicationContext(), R.string.failed_to_call, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.location)
    public void showWasherOnGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:" + mShowingWasher.getPlace().getLatitude() + ","
                + mShowingWasher.getPlace().getLongitude() + "?q="
                + mShowingWasher.getPlace().getLatitude() + ","
                + mShowingWasher.getPlace().getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(mContext.getPackageManager()) != null)
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
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void checkUserFavouriteWashers() {
        Utils.getFavourites(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    HashMap<String, Boolean> temp = dataSnapshot.getValue(
                            new GenericTypeIndicator<HashMap<String, Boolean>>() {
                            }
                    );
                    mFavouritesWashers.clear();
                    mFavouritesWashers.addAll(temp.keySet());
                    filterWashers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMarkers() {
        mMap.clear();
        for (Washer washer : mWashersList.values()) {
            MarkerOptions marker = new MarkerOptions()
                    .title(washer.getId())
                    .position(washer.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_place_default));
            mMarkersList.put(washer.getId(), mMap.addMarker(marker));
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
                        mGoogleApiClient,
                        mLocationSettingsRequest
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
                    status.startResolutionForResult(mContext, Constants.REQUEST_CHECK_LOCATION_SETTINGS);
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
                        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        animateCameraToCurrentCityPosition();
                        filterWashers();
                        break;
                }

            case Constants.REQUEST_RATING_CHANGED:
                switch (resultCode) {
                    case Constants.RATING_CHANGED_CODE:
                        reloadWasher(mShowingWasher.getId());
                        break;
                }
        }
    }

    private void filterWashers() {
        for (Washer washer : mWashersList.values()) {
            mMarkersList.get(washer.getId()).setVisible(
                    Utils.isWasherFits(washer, mContext, mFavouritesWashers)
            );
        }
    }

    /**
     * Location settings request was accepted, so we can send requests
     */
    private void makeRequests() {
        if (mCurrentLocation != null) {
            if (mSlidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN
                    && mShowingWasher != null) {
                calculateDistanceAndTime(mShowingWasher.getLatLng());
            }

            if (FLAG_FIND_MY_CURRENT_LOCATION && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16));
                mMyLocationFab.setColorFilter(colorAccent);
                FLAG_FIND_MY_CURRENT_LOCATION = false;
            }
            if (FLAG_FIND_NEAREST_WASHER) {
                navigateToTheNearestWasher();
                FLAG_FIND_NEAREST_WASHER = false;
            }
        }
    }

    /**
     * Location settings request was denied
     */
    private void setAllFlagsToFalse() {
        FLAG_FIND_MY_CURRENT_LOCATION = false;
        FLAG_FIND_NEAREST_WASHER = false;
        mMyLocationFab.setColorFilter(colorDark);
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setMyLocationUtilsEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        animateCameraToCurrentCityPosition();
        loadWashers();
    }

    private void animateCameraToCurrentCityPosition() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mCurrentCityCameraPosition.getLatitude(),
                        mCurrentCityCameraPosition.getLongitude()
                ),
                mCurrentCityCameraPosition.getZoom()
        ));
    }

    public void setMyLocationUtilsEnabled(boolean value) {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(value);
    }

    private void showWasher(Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));

        if (mWashersList.get(marker.getTitle()).equals(mShowingWasher) &&
                mSlidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)
            return;

        mShowingWasher = mWashersList.get(marker.getTitle());
        mDuration.setText("");
        makeRequests();

        //Inflating bottom sheet view by washer details
        inflateWasherDetails();
        //Show bottom sheet as collapsed
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showWasher(marker);
        return true;
    }

    private void inflateWasherDetails() {
        mName.setText(mShowingWasher.getName());
        mRatingBar.setRating(mShowingWasher.getRating());
        mRatingText.setText(String.format(Locale.getDefault(), "%.1f", mShowingWasher.getRating()));
        mCountOfRates.setText(String.format(Locale.getDefault(), "(%d)", mShowingWasher.getVotes()));
        mLocation.setText(mShowingWasher.getPlace().getStreet());
        if(mShowingWasher.getPlace().getPhone().isEmpty())
            mPhone.setText(R.string.no_info);
        else
            mPhone.setText(mShowingWasher.getPlace().getPhone());

        if(mShowingWasher.getDefaultPrice() <= 0)
            mDefaultPrice.setText(R.string.no_info);
        else
            mDefaultPrice.setText(String.valueOf(mShowingWasher.getDefaultPrice()));

        if (mShowingWasher.isRoundTheClock()) {
            mSchedule.setText(R.string.round_the_clock);
            mIsWasherOpen.setText(R.string.open);
            mIsWasherOpen.setTextColor(green);
        } else {
            if (!mShowingWasher.getSchedule().getScheduleForToday().isEmpty()) {
                mSchedule.setText(mShowingWasher.getSchedule().getScheduleForToday());
                if (Utils.isWasherOpenAtTheTime(mShowingWasher)) {
                    mIsWasherOpen.setText(R.string.open);
                    mIsWasherOpen.setTextColor(green);
                } else {
                    mIsWasherOpen.setText(R.string.closed);
                    mIsWasherOpen.setTextColor(red);
                }
            } else {
                mSchedule.setText(R.string.no_info);
            }
        }

        mWC.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isWc())));
        mWifi.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isWifi())));
        mCoffee.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isCoffee())));
        mGrocery.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isShop())));
        mRestRoom.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isRestRoom())));
        mCardPayment.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isCardPayment())));
        mServiceStation.setColorFilter(mResources
                .getColor(Utils.getServiceAvailableColor(mShowingWasher.getFeatures().isServiceStation())));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null)
            mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(mContext, R.string.disconnected, Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(mContext, R.string.network_lost, Toast.LENGTH_SHORT).show();
        }
    }

    // Trigger new location updates at interval
    private void startLocationUpdates() {
        // Request location updates
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        makeRequests();
        findTheNearestWasher();
    }

    private void findTheNearestWasher() {
        String washerId = null;
        Double[] distances = new Double[mWashersList.size()];
        Double bestMatch = (double) -1;
        int i = 0;
        for (Washer washer : mWashersList.values()) {
            distances[i] = SphericalUtil.computeDistanceBetween(
                    mCurrentLocation,
                    washer.getLatLng()
            );
            if (bestMatch.equals((double) -1) || bestMatch > distances[i]) {
                bestMatch = distances[i];
                washerId = washer.getId();
            }

            i++;
        }
        mTheNearestFreeWasher = mWashersList.get(washerId);
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
        if (mCurrentLocation == null || destination == null) return;
        DirectionsManager.with(this).buildDirection(
                mCurrentLocation,
                destination,
                TAG_BUILD_ROUTE
        );
    }

    private void calculateDistanceAndTime(LatLng destination) {
        if (mCurrentLocation == null || destination == null) return;
        DirectionsManager.with(this).buildDirection(
                mCurrentLocation,
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
                mDuration.setVisibility(View.VISIBLE);
                mDuration.setText(direction.duration.getText());
                break;

            case TAG_BUILD_ROUTE:
                break;
        }
    }

}
