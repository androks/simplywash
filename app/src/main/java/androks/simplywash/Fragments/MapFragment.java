package androks.simplywash.Fragments;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends BaseFragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        OrderDialog.OrderToWashListener, DirectionsManager.Listener {

    /** Define constant values  **/
    private static final int SIGN_IN = 10;
    private static final String CURRENT_WASHER_ID = "CURRENT_WASHER_ID";

    public static final int REQUEST_CHECK_SETTINGS = 11;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /** End constant values  **/


    /** Binding view with ButterKnife  **/
    @BindView(R.id.progress_horizontal) ProgressBar mProgressBar;
    @BindView(R.id.fab_state_marker) FloatingActionButton mChangeStateFab;
    @BindView(R.id.fab_get_direction) FloatingActionButton mOrderToNearestWash;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout mLayout;
    private Unbinder unbinder;

    @BindColor(android.R.color.holo_red_dark) int red;
    @BindColor(android.R.color.holo_green_light) int green;
    @BindColor(android.R.color.darker_gray) int gray;
    /** End bindings  **/


    /** Google Maps field and values **/
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private Location mCurrentLocation;
    /** End Google Maps section**/


    /**  Database section **/
    //Reference for downloading all washers
    private DatabaseReference mWashersReference = getWasher();

    //Reference for downloading Ids of free washers
    private DatabaseReference mFreeWashersReference = getStatesOfWashers();
    ValueEventListener mUploadWashers;
    ChildEventListener mChangeWasherStatusListener;
    /** End database section **/


    private FragmentActivity mContext;

    //Polyline list using as buffer to build directions
    private List<Polyline> mPolylinePaths = new ArrayList<>();

    //Relation between markers on map and list of washers
    private HashMap<String, Washer> mWashersList = new HashMap<>();
    private HashMap<String, Marker> mMarkersList = new HashMap<>();
    private HashMap<String, String> mWashersStatus = new HashMap<>();
    private LatLng mCurrentWasherLocation;
    private Bundle bundle = new Bundle();


    private boolean displayAllStates = false;
    private boolean routeBuildFirstTime = true;
    private boolean routeToBestMatchWashIsBuilt = false;
    private boolean routeToSelectedWashIsBuild = false;
    private boolean dialogIsShowing;


    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        mContext = getActivity();

        determineListenersForDatabase();
        setListenersForDatabase();
        setUpMap();

        determineListenersForDatabase();

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
    public void showWasherDetails(){
        Intent intent = new Intent(getActivity(), WasherDetailsActivity.class);
        intent.putExtra("id", bundle.getString(CURRENT_WASHER_ID));
        startActivity(intent);
    }

    @OnClick(R.id.fab_state_marker)
    public void changeWashersStateFlag(){
        displayAllStates = !displayAllStates;
        for (Washer washer : mWashersList.values())
            if(displayAllStates && (
                    washer.getState().equals(Utils.BUSY) ||
                    washer.getState().equals(Utils.OFFLINE))
                    )
                mMarkersList.get(washer.getId()).setVisible(displayAllStates);
            else if(!displayAllStates && (
                        washer.getState().equals(Utils.BUSY) ||
                        washer.getState().equals(Utils.OFFLINE))
                    )
                mMarkersList.get(washer.getId()).setVisible(displayAllStates);

        mChangeStateFab.setImageResource(displayAllStates ?
                R.mipmap.ic_markers_all : R.mipmap.ic_marker_free);
    }

    @OnClick(R.id.bottom_sheet_order_fab)
    public void orderToWash(){
        if (dialogIsShowing) {
            Toast.makeText(mContext, "Loading... \n Wait for previous task", Toast.LENGTH_SHORT).show();
            return;
        }
        routeToSelectedWashIsBuild = true;
        if (getCurrentUser() == null)
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), SIGN_IN);
        else {
            dialogIsShowing = true;
            mProgressBar.setVisibility(View.VISIBLE);
            checkLocationSettings();
        }
    }

    @OnClick(R.id.fab_get_direction)
    public void orderToTheNearestWash(){
        if (dialogIsShowing) {
            Toast.makeText(mContext, "Loading... \n Wait for previous task", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mWashersList.isEmpty() || mMarkersList.isEmpty()) {
            Toast.makeText(mContext, "No washers available", Toast.LENGTH_SHORT).show();
            return;
        }
        routeToBestMatchWashIsBuilt = true;
        if (getCurrentUser() == null)
            startActivityForResult(new Intent(getActivity(), LoginActivity.class), SIGN_IN);
        else {
            dialogIsShowing = true;
            mProgressBar.setVisibility(View.VISIBLE);
            checkLocationSettings();
        }
    }

    private void setUpMap(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();

        createLocationRequest();

        buildLocationSettingsRequest();

        checkLocationSettings();
    }

    private void setListenersForDatabase(){
        mWashersReference.addValueEventListener(mUploadWashers);
        mFreeWashersReference.addChildEventListener(mChangeWasherStatusListener);
    }

    private void deleteListenersForDatabase(){
        mWashersReference.removeEventListener(mUploadWashers);
        mFreeWashersReference.removeEventListener(mChangeWasherStatusListener);
    }

    private void determineListenersForDatabase(){
        mUploadWashers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    mWashersList.putAll(dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {
                            }
                    ));

                    HashMap<String, String> states = new HashMap<>();
                    for(Washer washer: mWashersList.values())
                        states.put(washer.getId(), washer.getState());
                    mWashersStatus.putAll(states);

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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mWashersStatus.put(dataSnapshot.getKey(), dataSnapshot.getValue(String.class));
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
        for(Washer washer: mWashersList.values()){
            MarkerOptions marker = new MarkerOptions()
                    .title(washer.getId())
                    .position(new LatLng(washer.getLangtitude(), washer.getLongtitude()))
                    .visible(washer.getState().equals(Utils.AVAILABLE) || displayAllStates);
            Utils.setMarkerIcon(marker, washer.getState());
            mMarkersList.put(washer.getId(), mMap.addMarker(marker));
        }
    }

    private void updateMarker(String id) {
        Utils.setMarkerIcon(mMarkersList.get(id), mWashersList.get(id).getState());
        mMarkersList.get(id).setVisible(
                mWashersList.get(id).getState().equals(Utils.AVAILABLE) || displayAllStates
        );
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
                //TODO:Build route
//                if (routeToSelectedWashIsBuild) {
//                    AppCompatDialogFragment addCarDialog = new OrderDialog();
//                    addCarDialog.setArguments(bundle);
//                    addCarDialog.setTargetFragment(MapFragment.this, 12);
//                    addCarDialog.show(getFragmentManager(), "Order");
//                    dialogIsShowing = false;
//                    mProgressBar.setVisibility(View.GONE);
//                    routeToSelectedWashIsBuild = false;
//                }
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //Location settings are not satisfied. Show the user a dialog to upgrade location settings
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(mContext, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    //PendingIntent unable to execute request

                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                //"Location settings are inadequate, and cannot be fixed here. Dialog not created.
                dialogIsShowing = false;
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
        }
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
        //Move camera to clicked marker and set washer's id to currentWash string
        bundle.putString(CURRENT_WASHER_ID, marker.getTitle());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        //Inflating bottom sheet view by washer details
        inflateWasherDetails(mWashersList.get(marker.getTitle()));
        //Show bottom sheet as collapsed
       //TODO:
        //Show order to current wash button with animation
        mContext.findViewById(R.id.bottom_sheet_order_fab).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.simple_grow));
        return true;
    }

    private void inflateWasherDetails(Washer washer) {
        View bottomSheet = mContext.findViewById(R.id.drag_view);
        ((TextView) bottomSheet.findViewById(R.id.name)).setText(washer.getName());
        ((TextView) bottomSheet.findViewById(R.id.location)).setText(washer.getLocation());
        ((TextView) bottomSheet.findViewById(R.id.phone)).setText(washer.getPhone());
        ((TextView) bottomSheet.findViewById(R.id.opening_hours)).setText(washer.getHours());
        ((TextView) bottomSheet.findViewById(R.id.boxes_status)).setText(washer.getFreeBoxes() + " of " + washer.getBoxes() + " are free");
        (bottomSheet.findViewById(R.id.lunch_room)).setVisibility(washer.getLunchRoom() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.rest_room)).setVisibility(washer.getRestRoom() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.wifi)).setVisibility(washer.getWifi() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.coffee)).setVisibility(washer.getCafe() ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onMapClick(LatLng latLng) {

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
    protected void startLocationUpdates() {
        // Request location updates
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        mCurrentLocation = location;
        if (routeToBestMatchWashIsBuilt) {
            orderToNearestWash();
            routeToBestMatchWashIsBuilt = false;
        } else if (routeToSelectedWashIsBuild) {
            orderToSelectedWash();
            routeToSelectedWashIsBuild = false;
        }
        buildRouteFromCurrentToMarkerLocation();
    }

    protected void buildRouteFromCurrentToMarkerLocation() {
        if (mCurrentLocation == null || mCurrentWasherLocation == null) return;
        DirectionsManager.with(this).buildDirection(
                new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                mCurrentWasherLocation
        );
    }

    private Marker find_closest_marker() {
//        if (mCurrentLocation == null) return null;
//        double pi = Math.PI;
//        int R = 6371; //equatorial radius
//        double[] distances = new double[mWashersFreeList.size()];
//        int closest = -1;
//        for (int i = 0; i < mWashersFreeList.size(); i++) {
//            double lat2 = mMarkersList.get(mWashersFreeList.get(i)).getPosition().latitude;
//            double lon2 = mMarkersList.get(mWashersFreeList.get(i)).getPosition().longitude;
//
//            double chLat = lat2 - mCurrentLocation.getLatitude();
//            double chLon = lon2 - mCurrentLocation.getLongitude();
//
//            double dLat = chLat * (pi / 180);
//            double dLon = chLon * (pi / 180);
//
//            double rLat1 = mCurrentLocation.getLatitude() * (pi / 180);
//            double rLat2 = lat2 * (pi / 180);
//
//            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
//                    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);
//            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//            double d = R * c;
//
//            distances[i] = d;
//            if (closest == -1 || d < distances[closest]) {
//                closest = i;
//            }
//        }
//        return mMarkersList.get(mWashersFreeList.get(closest));
        return null;
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
        mProgressBar.setVisibility(View.VISIBLE);
        mCurrentWasherLocation = mMarkersList.get(bundle.getString(CURRENT_WASHER_ID)).getPosition();
        startLocationUpdates();
        routeBuildFirstTime = true;
        buildRouteFromCurrentToMarkerLocation();
    }

    public void orderToNearestWash() {
        Marker marker = find_closest_marker();
        if (marker != null) {
            bundle.putString(CURRENT_WASHER_ID, marker.getTitle());
            AppCompatDialogFragment addCarDialog = new OrderDialog();
            addCarDialog.setArguments(bundle);
            addCarDialog.setTargetFragment(MapFragment.this, 12);
            addCarDialog.show(getFragmentManager(), "Order");
            mProgressBar.setVisibility(View.GONE);
        }
        dialogIsShowing = false;
    }

    private void orderToSelectedWash() {
        AppCompatDialogFragment addCarDialog = new OrderDialog();
        addCarDialog.setArguments(bundle);
        addCarDialog.setTargetFragment(MapFragment.this, 12);
        addCarDialog.show(getFragmentManager(), "Order");
        mProgressBar.setVisibility(View.GONE);
        dialogIsShowing = false;
    }

    @Override
    public void onDirectionFindStart() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDirectionReady(Direction direction) {
        if (mPolylinePaths != null)
            for (Polyline polyline : mPolylinePaths)
                polyline.remove();

        mPolylinePaths = new ArrayList<>();

        if (routeBuildFirstTime)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(direction.startLocation, 16));

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(12);

        for (int i = 0; i < direction.points.size(); i++)
            polylineOptions.add(direction.points.get(i));

        mPolylinePaths.add(mMap.addPolyline(polylineOptions));

        routeBuildFirstTime = false;
        mCurrentWasherLocation = direction.endLocation;

        mProgressBar.setVisibility(View.GONE);
    }
}
