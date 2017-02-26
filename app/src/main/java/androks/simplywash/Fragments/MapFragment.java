package androks.simplywash.Fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androks.simplywash.Activities.LoginActivity;
import androks.simplywash.Activities.WasherDetailsActivity;
import androks.simplywash.Dialogs.OrderDialog;
import androks.simplywash.DirectionsApi.Data.Direction;
import androks.simplywash.DirectionsApi.DirectionsManager;
import androks.simplywash.Models.Order;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MapFragment extends BaseFragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        OrderDialog.OrderToWashListener,
        DirectionsManager.Listener{

    /**
     * Define constant values
     **/
    private static final String TAG_CALCULATE_DIS_DUR = "TAG_CALCULATE_DIS_DUR";
    private static final String TAG_BUILD_ROUTE = "TAG_BUILD_ROUTE";
    private static final int SIGN_IN = 10;
    private static final String CURRENT_WASHER_ID = "CURRENT_WASHER_ID";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /** End constant values  **/


    /**
     * Binding view with ButterKnife
     **/
    @BindView(R.id.progress_horizontal)
    ProgressBar mProgressBar;
    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout mSlidingLayout;

    @BindView(R.id.name) TextView mName;
    @BindView(R.id.location) TextView mLocation;
    @BindView(R.id.phone) TextView mPhone;
    @BindView(R.id.opening_hours) TextView mOpeningHours;
    @BindView(R.id.boxes_status) TextView mBoxesStatus;
    @BindView(R.id.duration) TextView mDuration;
    @BindView(R.id.duration_in_progress) View mDurationInProgress;
    @BindView(R.id.distance) TextView mDistance;
    @BindView(R.id.distance_in_progress) View mDistanceInProgress;
    @BindView(R.id.wifi) ImageView mWifi;
    @BindView(R.id.coffee) ImageView mCoffee;
    @BindView(R.id.lunch_room) ImageView mLunchRoom;
    @BindView(R.id.rest_room) ImageView mRestRoom;
    @BindView(R.id.wc) ImageView mWC;
    @BindView(R.id.tire) ImageView mTire;
    private Unbinder unbinder;
    /** End bindings  **/


    /**
     * Google Maps field and values
     **/
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    /** End Google Maps section**/


    /**
     * Database section
     **/
    //Reference for downloading all washers
    private DatabaseReference mWashersReference = Utils.getWasher();

    //Reference for downloading Ids of free washers
    private DatabaseReference mFreeWashersReference = Utils.getStatesOfWashers();
    ValueEventListener mUploadWashers;
    ChildEventListener mChangeWasherStatusListener;
    /**
     * End database section
     **/

    private FragmentActivity mContext;

    //Polyline list using as buffer to build directions
    private List<Polyline> mPolylinePaths = new ArrayList<>();
    private Washer mShowingWasher;
    private Washer mCurrentWasher;
    private Washer mTheNearestFreeWasher;
    private LatLng mCurrentLocation;

    //Relation between markers on map and list of washers
    private HashMap<String, Washer> mWashersList = new HashMap<>();
    private HashMap<String, Marker> mMarkersList = new HashMap<>();

    private boolean FLAG_DISPLAY_ALL_STATES = false;
    private boolean FLAG_DIALOG_IN_PROCESS = false;
    private boolean FLAG_DIS_DUR_CALCULATE = false;

    public MapFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        showProgress();
        mContext = getActivity();

        determineListenersForDatabase();
        setListenersForDatabase();
        setUpMap();

        determineListenersForDatabase();

        mSlidingLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
        return rootView;
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

        setListenersForDatabase();
    }


    @Override
    public void onStop() {

        // Disconnecting the client invalidates it.
        stopLocationUpdates();

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();

        deleteListenersForDatabase();
        super.onStop();
    }

    @Override
    public void onDetach() {
        unbinder.unbind();
        super.onDetach();
    }

    @OnClick(R.id.moreBtn)
    public void showWasherDetails() {
        Intent intent = new Intent(getActivity(), WasherDetailsActivity.class);
        intent.putExtra("id", mShowingWasher.getId());
        startActivity(intent);
    }

    @OnClick(R.id.fab_state_marker)
    public void changeWashersStateFlag(FloatingActionButton fab) {
        FLAG_DISPLAY_ALL_STATES = !FLAG_DISPLAY_ALL_STATES;
        for (Washer washer : mWashersList.values())
            if (FLAG_DISPLAY_ALL_STATES && (
                    washer.getState().equals(Utils.BUSY) ||
                            washer.getState().equals(Utils.OFFLINE))
                    )
                mMarkersList.get(washer.getId()).setVisible(FLAG_DISPLAY_ALL_STATES);
            else if (!FLAG_DISPLAY_ALL_STATES && (
                    washer.getState().equals(Utils.BUSY) ||
                            washer.getState().equals(Utils.OFFLINE))
                    )
                mMarkersList.get(washer.getId()).setVisible(FLAG_DISPLAY_ALL_STATES);

        fab.setImageResource(FLAG_DISPLAY_ALL_STATES ?
                R.mipmap.ic_markers_all : R.mipmap.ic_marker_free);
    }

    @OnClick({R.id.order_to_showing_wash, R.id.fab_get_direction})
    public void orderToWash(FloatingActionButton fab) {
        if (FLAG_DIALOG_IN_PROCESS) {
            Toast.makeText(mContext, "Loading... \n Wait for previous task", Toast.LENGTH_SHORT).show();
            return;
        }
        if (fab.getId() == R.id.fab_get_direction && (mWashersList.isEmpty() || mMarkersList.isEmpty())) {
            Toast.makeText(mContext, "No washers available", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getCurrentUser() == null) {
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), SIGN_IN);
            return;
        }
        showProgress();
        FLAG_DIALOG_IN_PROCESS = true;
    }

    private void setUpMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();

        createLocationRequest();

        buildLocationSettingsRequest();

        checkLocationSettings();
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void setListenersForDatabase() {
        mWashersReference.addValueEventListener(mUploadWashers);
        mFreeWashersReference.addChildEventListener(mChangeWasherStatusListener);
    }

    private void deleteListenersForDatabase() {
        mWashersReference.removeEventListener(mUploadWashers);
        mFreeWashersReference.removeEventListener(mChangeWasherStatusListener);
    }

    private void determineListenersForDatabase() {
        mUploadWashers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mWashersList.putAll(dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {
                            }
                    ));
                    setMarkers();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(
                        mContext,
                        "Error while download\n Check your internet connection",
                        Toast.LENGTH_SHORT
                ).show();
            }
        };

        mChangeWasherStatusListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                updateMarker(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, databaseError.toString(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setMarkers() {
        for (Washer washer : mWashersList.values()) {
            MarkerOptions marker = new MarkerOptions()
                    .title(washer.getId())
                    .position(new LatLng(washer.getLangtitude(), washer.getLongtitude()))
                    .visible(washer.getState().equals(Utils.AVAILABLE) || FLAG_DISPLAY_ALL_STATES);
            Utils.setMarkerIcon(marker, washer.getState());
            mMarkersList.put(washer.getId(), mMap.addMarker(marker));
        }
        hideProgress();
    }

    private void updateMarker(String id) {
        Utils.setMarkerIcon(mMarkersList.get(id), mWashersList.get(id).getState());
        mMarkersList.get(id).setVisible(
                mWashersList.get(id).getState().equals(Utils.AVAILABLE) || FLAG_DISPLAY_ALL_STATES
        );
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
                    status.startResolutionForResult(mContext, Utils.REQUEST_CHECK_LOCATION_SETTINGS);
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
            case SIGN_IN:
                //TODO:Build route
                //checkLocationSettings();
                break;
            case Utils.REQUEST_CHECK_LOCATION_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        makeRequests();
                        break;

                    case Activity.RESULT_CANCELED:
                        setAllFlagsToFalse();
                        break;
                }
                break;
        }
    }

    /**
     * Location settings request was accepted, so we can handle requests
     */
    private void makeRequests(){
        if(mCurrentLocation != null){
            if(FLAG_DIS_DUR_CALCULATE)
                calculateDistanceAndTime(mShowingWasher.getLanLng());
        }
    }

    /**
     * Location settings request was denied
     */
    private void setAllFlagsToFalse(){
        FLAG_DIS_DUR_CALCULATE = false;
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
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
        LatLng kiev = new LatLng(50.4448235, 30.5497172);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(kiev, 10));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        FLAG_DIS_DUR_CALCULATE = true;

        mShowingWasher = mWashersList.get(marker.getTitle());

        checkLocationSettings();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        //Inflating bottom sheet view by washer details
        inflateWasherDetails();
        //Show bottom sheet as collapsed
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        return true;
    }

    private void inflateWasherDetails() {
        mDistanceInProgress.setVisibility(View.VISIBLE);
        mDurationInProgress.setVisibility(View.VISIBLE);
        mDuration.setVisibility(View.GONE);
        mDistance.setVisibility(View.GONE);
        mName.setText(mShowingWasher.getName());
        mLocation.setText(mShowingWasher.getLocation());
        mPhone.setText(mShowingWasher.getPhone());
        mOpeningHours.setText(mShowingWasher.getHours());
        mBoxesStatus.setText(mShowingWasher.getFreeBoxes() + " of " + mShowingWasher.getBoxes());

        mWifi.setColorFilter(mShowingWasher.getWifi() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
        mCoffee.setColorFilter(mShowingWasher.getCafe() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
        mLunchRoom.setColorFilter(mShowingWasher.getLunchRoom() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
        mRestRoom.setColorFilter(mShowingWasher.getRestRoom() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
        mWC.setColorFilter(mShowingWasher.getWc() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
        mTire.setColorFilter(mShowingWasher.getTire() ?
                ContextCompat.getColor(mContext, R.color.colorServiceAvailable) :
                ContextCompat.getColor(mContext, R.color.colorServiceNotAvailable));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(mContext, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(mContext, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
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
        for(Washer washer: mWashersList.values()) {
            if (washer.getState().equals(Utils.AVAILABLE)) {
                distances[i] = SphericalUtil.computeDistanceBetween(
                        mCurrentLocation,
                        washer.getLanLng()
                );
                if(bestMatch.equals((double) -1) || bestMatch > distances[i]){
                    bestMatch = distances[i];
                    washerId = washer.getId();
                }
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

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
//        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
//        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onOrder(Order order) {
//        mProgressBar.setVisibility(View.VISIBLE);
//        mCurrentWasherLocation = mMarkersList.get(bundle.getString(CURRENT_WASHER_ID)).getPosition();
//        startLocationUpdates();
//        routeBuildFirstTime = true;
//        buildRouteFromCurrentToMarkerLocation();
    }

    public void orderToNearestWash() {
//        Marker marker = find_closest_marker();
//        if (marker != null) {
//            bundle.putString(CURRENT_WASHER_ID, marker.getTitle());
//            AppCompatDialogFragment addCarDialog = new OrderDialog();
//            addCarDialog.setArguments(bundle);
//            addCarDialog.setTargetFragment(MapFragment.this, 12);
//            addCarDialog.show(getFragmentManager(), "Order");
//            mProgressBar.setVisibility(View.GONE);
//        }
//        FLAG_DIALOG_IN_PROCESS = false;
    }

    private void orderToCurrentWash() {
        AppCompatDialogFragment addCarDialog = new OrderDialog();
        //addCarDialog.setArguments(bundle);
        addCarDialog.setTargetFragment(MapFragment.this, 12);
        addCarDialog.show(getFragmentManager(), "Order");
        mProgressBar.setVisibility(View.GONE);
        FLAG_DIALOG_IN_PROCESS = false;
    }

    @Override
    public void onDirectionFindStart() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDirectionReady(Direction direction) {
        if(direction == null)
            return;

        switch (direction.getTag()) {
            case TAG_CALCULATE_DIS_DUR:
                if (FLAG_DIS_DUR_CALCULATE) {

                    mDistance.setText(direction.distance.getText());
                    mDuration.setText(direction.duration.getText());

                    mDurationInProgress.setVisibility(View.GONE);
                    mDistanceInProgress.setVisibility(View.GONE);

                    mDistance.setVisibility(View.VISIBLE);
                    mDuration.setVisibility(View.VISIBLE);
                    FLAG_DIS_DUR_CALCULATE = false;
                }
                break;

            case TAG_BUILD_ROUTE:

                //Todo: write correct behavior
                if (mPolylinePaths != null)
                    for (Polyline polyline : mPolylinePaths)
                        polyline.remove();

                mPolylinePaths = new ArrayList<>();

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(direction.startLocation, 16));

                PolylineOptions polylineOptions = new PolylineOptions().
                        geodesic(true).
                        color(Color.BLUE).
                        width(12);

                for (int i = 0; i < direction.points.size(); i++)
                    polylineOptions.add(direction.points.get(i));

                mPolylinePaths.add(mMap.addPolyline(polylineOptions));

                hideProgress();
                break;
        }
    }
}
