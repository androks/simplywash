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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androks.simplywash.Activities.LoginActivity;
import androks.simplywash.Activities.WasherDetailsActivity;
import androks.simplywash.Dialogs.OrderDialog;
import androks.simplywash.DirectionsApi.Data.Direction;
import androks.simplywash.DirectionsApi.DirectionsManager;
import androks.simplywash.Models.Order;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class WashersFragment extends BaseFragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<LocationSettingsResult>,
        OrderDialog.OrderToWashListener, DirectionsManager.Listener {


    private static final int SIGN_IN = 9001;

    private static final String CURRENT_WASHER_ID = "currentWasherId";
    private FragmentActivity mContext;
    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private GoogleMap mMap;

    //Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;

    //Stores parameters for requests to the FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    //Polyline list using as buffer to build directions
    private List<Polyline> polylinePaths = new ArrayList<>();

    //Reference for downloading all washers
    private DatabaseReference mWashersReference;
    //Reference for downloading Ids of free washers
    private DatabaseReference mFreeWashersReference;
    ValueEventListener mListenerForDownloadWashers, mListenerForDownloadFreeWashersList;

    //Relation between markers on map and list of washers
    private HashMap<String, Washer> mWashersList = new HashMap<>();
    private HashMap<String, Marker> mMarkersList = new HashMap<>();
    private ArrayList<String> mWashersNonfreeList = new ArrayList<>();
    private ArrayList<String> mWashersFreeList = new ArrayList<>();
    private LatLng mCurrentWasherLocation;
    private Bundle bundle = new Bundle();


    /**
     * Views
     */

    private View mProgressBar;

    //View to handle change showing marker types
    private FloatingActionButton mShowOnlyFreeWashersFab;

    private FloatingActionButton mOrderToNearestWash;

    /**
     * Bottom Sheet views
     */
    BottomSheetBehavior behavior;

    /**
     * Flags
     */
    private boolean bottomSheetIsExpanded;
    private boolean routeBuildFirstTime;
    private boolean busyWashersIsIncluded;
    private boolean routeToBestMatchWashIsBuilt;
    private boolean routeToSelectedWashIsBuild;
    private boolean dialogIsShowing;

    public WashersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_washers, container, false);

        mProgressBar = rootView.findViewById(R.id.progress_horizontal);
        mProgressBar.setVisibility(View.VISIBLE);

        //setting flags
        routeBuildFirstTime = true;
        busyWashersIsIncluded = true;
        routeToBestMatchWashIsBuilt = false;
        routeToSelectedWashIsBuild = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        // Kick off the process of building the GoogleApiClient, LocationRequest, and
        // LocationSettingsRequest objects.
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

        checkLocationSettings();

        /**
         * Database section
         */
        mWashersReference = FirebaseDatabase.getInstance().getReference().child("washers");
        mFreeWashersReference = FirebaseDatabase.getInstance().getReference().child("free-washers");

        /**
         * Database listener implementation
         */
        mListenerForDownloadWashers = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Washer temp = child.getValue(Washer.class);
                    mWashersList.put(temp.getId(), temp);
                }
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "Error while download\n Check your internet connection", Toast.LENGTH_SHORT).show();
            }
        };

        mListenerForDownloadFreeWashersList = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Washer washer = mWashersList.get(child.getKey());
                        if (washer != null) {
                            washer.setStatus((Boolean) child.getValue());

                            if (washer.getStatus()) {
                                mWashersNonfreeList.remove(washer.getId());
                                mWashersFreeList.add(washer.getId());
                            } else {
                                mWashersNonfreeList.add(washer.getId());
                                mWashersFreeList.remove(washer.getId());
                            }
                            setWasherToMap(mWashersList.get(child.getKey()));
                        }
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(mContext, "Error while download\n Check your internet connection", Toast.LENGTH_SHORT).show();
            }
        };

        /**
         * FABs
         */

        mShowOnlyFreeWashersFab = (FloatingActionButton) rootView.findViewById(R.id.fab_status_marker);
        mShowOnlyFreeWashersFab.setOnClickListener(this);

        mOrderToNearestWash = (FloatingActionButton) rootView.findViewById(R.id.fab_get_direction);
        mOrderToNearestWash.setOnClickListener(this);

        /**
         * Bottom Sheet
         */

        // To handle FAB animation upon entrance and exit
        final Animation growAnimation = AnimationUtils.loadAnimation(mContext, R.anim.simple_grow);
        final Animation shrinkAnimation = AnimationUtils.loadAnimation(mContext, R.anim.simple_shrink);

        //Adding behavior for bottom sheet
        behavior = BottomSheetBehavior.from(rootView.findViewById(R.id.coordinatorLayout).findViewById(R.id.bottom_sheet));
        //Set bottom sheet hidden by default
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //Set listeners for bottom sheet states
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mShowOnlyFreeWashersFab.startAnimation(shrinkAnimation);
                    mOrderToNearestWash.startAnimation(shrinkAnimation);
                    mShowOnlyFreeWashersFab.setVisibility(View.GONE);
                    mOrderToNearestWash.setVisibility(View.GONE);
                    bottomSheetIsExpanded = true;
                } else if (bottomSheetIsExpanded) {
                    mShowOnlyFreeWashersFab.startAnimation(growAnimation);
                    mOrderToNearestWash.startAnimation(growAnimation);
                    mShowOnlyFreeWashersFab.setVisibility(View.VISIBLE);
                    mOrderToNearestWash.setVisibility(View.VISIBLE);
                    bottomSheetIsExpanded = false;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        //Adding listeners for bottom sheet views
        rootView.findViewById(R.id.bottom_sheet_title).setOnClickListener(this);
        rootView.findViewById(R.id.bottom_sheet_order_fab).setOnClickListener(this);
        rootView.findViewById(R.id.moreBtn).setOnClickListener(this);

        DirectionsManager.with(this).buildDirection(
                new LatLng(50.4472772,30.4461914),
                new LatLng(50.441046,30.4338318)
        );
        return rootView;
    }


    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
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
                if (routeToSelectedWashIsBuild) {
                    AppCompatDialogFragment addCarDialog = new OrderDialog();
                    addCarDialog.setArguments(bundle);
                    addCarDialog.setTargetFragment(WashersFragment.this, 12);
                    addCarDialog.show(getFragmentManager(), "Order");
                    dialogIsShowing = false;
                    mProgressBar.setVisibility(View.GONE);
                    routeToSelectedWashIsBuild = false;
                }
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
                checkLocationSettings();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        //Adding single value listener to download list of washers
        mWashersReference.addValueEventListener(mListenerForDownloadWashers);
        //Adding single value listener to download list of only free washers
        mFreeWashersReference.addValueEventListener(mListenerForDownloadFreeWashersList);

    }

    @Override
    public void onPause() {
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        super.onPause();
    }

    @Override
    public void onStop() {

        // Disconnecting the client invalidates it.
        stopLocationUpdates();

        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        //Adding single value listener to download list of washers
        mWashersReference.removeEventListener(mListenerForDownloadWashers);
        //Adding single value listener to download list of only free washers
        mFreeWashersReference.removeEventListener(mListenerForDownloadFreeWashersList);
        super.onStop();
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.4448235, 30.5497172), 10));
    }

    private void setWasherToMap(Washer washer) {
        //Adding map is does not exist
        if (!mMarkersList.containsKey(washer.getId())) {
            mMarkersList.put(washer.getId(),
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(washer.getLangtitude(), washer.getLongtitude()))
                            .title(washer.getName())
                            .icon(BitmapDescriptorFactory.defaultMarker(washer.getStatus() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED))
                            .title(washer.getId())
                    )
            );
        } else {
            /**
             * Change marker parameters after changing marker status
             */
            //Set marker image
            mMarkersList.get(washer.getId()).setIcon(BitmapDescriptorFactory.defaultMarker(washer.getStatus() ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED));
            //Set washer visible if has free status
            if (washer.getStatus())
                mMarkersList.get(washer.getId()).setVisible(true);
            //Dont show marker if washer has false status and busy washers don't showing
            if (!busyWashersIsIncluded && !washer.getStatus())
                mMarkersList.get(washer.getId()).setVisible(false);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Move camera to clicked marker and set washer's id to currentWash string
        bundle.putString(CURRENT_WASHER_ID, marker.getTitle());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 14));
        //Inflating bottom sheet view by washer details
        inflateWasherDetails(mWashersList.get(marker.getTitle()));
        //Show bottom sheet as collapsed
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //Show order to current wash button with animation
        mContext.findViewById(R.id.bottom_sheet_order_fab).startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.simple_grow));
        return true;
    }

    private void inflateWasherDetails(Washer washer) {
        View bottomSheet = mContext.findViewById(R.id.bottom_sheet);
        ((TextView) bottomSheet.findViewById(R.id.washer_name)).setText(washer.getName());
        ((TextView) bottomSheet.findViewById(R.id.location)).setText(washer.getLocation());
        ((TextView) bottomSheet.findViewById(R.id.phone)).setText(washer.getPhone());
        ((TextView) bottomSheet.findViewById(R.id.opening_hours)).setText(washer.getHours());
        ((TextView) bottomSheet.findViewById(R.id.washer_free_boxes)).setText(washer.getFreeBoxes() + " of " + washer.getBoxes() + " are free");
        (bottomSheet.findViewById(R.id.lunch_room)).setVisibility(washer.getLunchRoom() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.rest_room)).setVisibility(washer.getRestRoom() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.wifi)).setVisibility(washer.getWifi() ? View.VISIBLE : View.GONE);
        (bottomSheet.findViewById(R.id.coffee)).setVisibility(washer.getCafe() ? View.VISIBLE : View.GONE);

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.moreBtn:
                Intent intent = new Intent(getActivity(), WasherDetailsActivity.class);
                intent.putExtra("id", bundle.getString(CURRENT_WASHER_ID));
                startActivity(intent);
                break;
            case R.id.fab_status_marker:
                busyWashersIsIncluded = !busyWashersIsIncluded;
                for (String washerId : mWashersNonfreeList)
                    mMarkersList.get(washerId).setVisible(busyWashersIsIncluded);
                mShowOnlyFreeWashersFab.setImageResource(busyWashersIsIncluded ? R.mipmap.ic_marker_free : R.mipmap.ic_markers_all);
                break;

            case R.id.bottom_sheet_title:
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;

            case R.id.bottom_sheet_order_fab:
                if (dialogIsShowing) {
                    Toast.makeText(mContext, "Loading... \n Wait for previous task", Toast.LENGTH_SHORT).show();
                    break;
                }
                routeToSelectedWashIsBuild = true;
                if (getCurrentUser() == null)
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), SIGN_IN);
                else {
                    dialogIsShowing = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    checkLocationSettings();
                }
                break;

            case R.id.fab_get_direction:
                if (dialogIsShowing) {
                    Toast.makeText(mContext, "Loading... \n Wait for previous task", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (mWashersList.isEmpty() || mMarkersList.isEmpty()) {
                    Toast.makeText(mContext, "No washers available", Toast.LENGTH_SHORT).show();
                    break;
                }
                routeToBestMatchWashIsBuilt = true;
                if (getCurrentUser() == null)
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), SIGN_IN);
                else {
                    dialogIsShowing = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    checkLocationSettings();
                }
                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Get last known recent location.
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // Note that this can be NULL if last location isn't already known.
//        if (mCurrentLocation == null) {
//            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 14));
//        }

        // Begin polling for new location updates.
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
        if (mCurrentLocation == null) return null;
        double pi = Math.PI;
        int R = 6371; //equatorial radius
        double[] distances = new double[mWashersFreeList.size()];
        int closest = -1;
        for (int i = 0; i < mWashersFreeList.size(); i++) {
            double lat2 = mMarkersList.get(mWashersFreeList.get(i)).getPosition().latitude;
            double lon2 = mMarkersList.get(mWashersFreeList.get(i)).getPosition().longitude;

            double chLat = lat2 - mCurrentLocation.getLatitude();
            double chLon = lon2 - mCurrentLocation.getLongitude();

            double dLat = chLat * (pi / 180);
            double dLon = chLon * (pi / 180);

            double rLat1 = mCurrentLocation.getLatitude() * (pi / 180);
            double rLat2 = lat2 * (pi / 180);

            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(rLat1) * Math.cos(rLat2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = R * c;

            distances[i] = d;
            if (closest == -1 || d < distances[closest]) {
                closest = i;
            }
        }
        return mMarkersList.get(mWashersFreeList.get(closest));
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
            addCarDialog.setTargetFragment(WashersFragment.this, 12);
            addCarDialog.show(getFragmentManager(), "Order");
            mProgressBar.setVisibility(View.GONE);
        }
        dialogIsShowing = false;
    }

    private void orderToSelectedWash() {
        AppCompatDialogFragment addCarDialog = new OrderDialog();
        addCarDialog.setArguments(bundle);
        addCarDialog.setTargetFragment(WashersFragment.this, 12);
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
        if (polylinePaths != null)
            for (Polyline polyline : polylinePaths)
                polyline.remove();

        polylinePaths = new ArrayList<>();

        if (routeBuildFirstTime)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(direction.startLocation, 16));

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(12);

        for (int i = 0; i < direction.points.size(); i++)
            polylineOptions.add(direction.points.get(i));

        polylinePaths.add(mMap.addPolyline(polylineOptions));

        routeBuildFirstTime = false;
        mCurrentWasherLocation = direction.endLocation;

        mProgressBar.setVisibility(View.GONE);
    }
}
