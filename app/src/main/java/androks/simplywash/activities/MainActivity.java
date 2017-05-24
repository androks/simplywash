package androks.simplywash.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import androks.simplywash.R;
import androks.simplywash.fragments.AddWasherFragment;
import androks.simplywash.fragments.MapFragment;
import androks.simplywash.fragments.ShareFragment;
import androks.simplywash.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final int CHECK_PERMISSIONS_REQUEST = 125;
    // tags used to attach the fragments
    private static final String TAG_MAP = "TAG_MAP";
    private static final String TAG_SHARE = "TAG_SHARE";
    private static final String TAG_ADD_WASHER = "TAG_ADD_WASHER";

    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.nav_view) NavigationView mNVDrawer;

    private TextView mPhoneTextView;
    private TextView mCityTextView;

    public static String CURRENT_TAG = TAG_MAP;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    private Fragment mCurrentFragment;

    private ActionBarDrawerToggle mDrawerToggle;

    private Handler mHandler;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSharedPrefs = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);

        checkPermissions();

        checkIfUserLoggedIn();

        setUpToolbar();

        mHandler = new Handler();

        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_MAP;
            loadHomeFragment();
        }
    }

    private void checkPermissions() {
        List<String> permissionNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();
        if (!addPermission(permissionsList, Manifest.permission.RECEIVE_SMS))
            permissionNeeded.add("android.permission.INTERNET");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_NETWORK_STATE))
            permissionNeeded.add("android.permission.ACCESS_NETWORK_STATE");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionNeeded.add("android.permission.ACCESS_FINE_LOCATION");

        if (!permissionNeeded.isEmpty())
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissionNeeded.toArray(new String[permissionNeeded.size()]),
                    CHECK_PERMISSIONS_REQUEST);
    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission))
                return false;
        }
        return true;
    }

    private void loadNavHeader() {
        setUpHeaderView();
        getSharedPrefData();
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        mNVDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main llContent with ContentFragment Which is our Inbox View;
                    case R.id.nav_map:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_MAP;
                        break;
                    case R.id.add_washer:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_ADD_WASHER;
                        break;
                    case R.id.share:
                        navItemIndex = 2;
                        CURRENT_TAG = TAG_SHARE;
                        break;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawer,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        mDrawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void selectNavMenu() {
        mNVDrawer.getMenu().getItem(navItemIndex).setChecked(true);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawer.closeDrawers();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main llContent by replacing fragments
                mCurrentFragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.fl_content_main, mCurrentFragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
                setToolbarTitle();
            }
        };

        mHandler.post(mPendingRunnable);

        //Closing drawer on item click
        mDrawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();

    }

    private void setToolbarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;
        switch (navItemIndex) {
            case 0:
                actionBar.setTitle(R.string.title_activity_washers_map);
                break;
            case 2:
                actionBar.setTitle(R.string.title_activity_share);
                break;
            case 1:
                actionBar.setTitle(R.string.title_add_washer);
                break;
        }
    }

    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                return new MapFragment();
            case 1:
                return new AddWasherFragment();
            case 2:
                return new ShareFragment();
            default:
                return new MapFragment();
        }
    }

    private void getSharedPrefData() {
        String mCurrentPhone = mSharedPrefs.getString(Constants.PHONE_PREF, null);
        String mCurrentCity = mSharedPrefs.getString(Constants.CITY_PREF, null);
        if (mCurrentPhone != null)
            mPhoneTextView.setText(mCurrentPhone);
        if (mCurrentCity != null)
            mCityTextView.setText(mCurrentCity);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(R.string.title_activity_main);
    }

    private void setUpHeaderView() {
        View mNavHeader = mNVDrawer.getHeaderView(0);
        if (mNavHeader == null)
            return;
        mPhoneTextView = (TextView) mNavHeader.findViewById(R.id.tv_current_phone);
        mCityTextView = (TextView) mNavHeader.findViewById(R.id.tv_current_city);
        mNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    private void checkIfUserLoggedIn() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CURRENT_FRAGMENT, navItemIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            navItemIndex = savedInstanceState.getInt(Constants.CURRENT_FRAGMENT);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (navItemIndex == 0) {
            toggleSlidingUpPanel();
        } else
            super.onBackPressed();
    }

    private void toggleSlidingUpPanel() {
        SlidingUpPanelLayout slidingUpPanelLayout =
                (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        else if (slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mCurrentFragment != null)
            mCurrentFragment.onActivityResult(requestCode, resultCode, data);
    }

}
