package androks.simplywash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import androks.simplywash.Constants;
import androks.simplywash.R;
import androks.simplywash.fragments.MapFragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    public static final String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";

    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.nav_view) NavigationView mNVDrawer;
    private View mNavHeader;
    private TextView mCurrentPhone;

    private ActionBarDrawerToggle mDrawerToggle;
    private SharedPreferences mSharedPrefs;

    private int currentFragment = 0;

    private Fragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPrefs = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setUpToolbar();

        setUpHeaderView();

        checkUserPhoneNum();

        mapFragment = new MapFragment();

        mDrawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(mDrawerToggle);
        setupDrawerContent(mNVDrawer);

        setCurrentFragment();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setUpHeaderView() {
        mNavHeader = mNVDrawer.getHeaderView(0);
        if (mNavHeader == null)
            return;
        mCurrentPhone = (TextView) mNavHeader.findViewById(R.id.current_phone);
        mNavHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    private void checkUserPhoneNum(){
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent toLogin = new Intent(this, LoginActivity.class);
            startActivity(toLogin);
            finish();
        }else{
            String phone = mSharedPrefs.getString(Constants.PHONE_PREF, null);
            mCurrentPhone.setText(phone);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_FRAGMENT, currentFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState != null){
            currentFragment = savedInstanceState.getInt(CURRENT_FRAGMENT);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(
                this,
                mDrawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        boolean changeFragment = false;
        switch(menuItem.getItemId()) {
            case R.id.nav_map:
                changeFragment = (currentFragment == 0);
                currentFragment = 0;
                break;

            case R.id.share:
                //TODO:determine share func
                break;
        }

        if(!changeFragment)
            setCurrentFragment();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }else if(currentFragment == 0) {
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
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(getCurrentFragment() != null)
            getCurrentFragment().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setCurrentFragment(){
        Fragment fragment;

        switch (currentFragment) {
            case 0:
                fragment = mapFragment;
                break;
            default:
                fragment = mapFragment;
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();
    }

    private Fragment getCurrentFragment(){
        switch (currentFragment) {
            case 0:
                return mapFragment;
            default:
                return null;
        }
    }
}
