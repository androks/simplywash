package androks.simplywash.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;

import androks.simplywash.Constants;
import androks.simplywash.R;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FiltersActivity extends AppCompatActivity {

    @BindArray(R.array.priceCategoriesInt) int[] priceCategoriesInt;

    @BindView(R.id.toolbar) Toolbar toolbar;

    @BindView(R.id.onlyFavouritesSwitch) Switch mOnlyFavourites;
    @BindView(R.id.priceCategorySpinner) Spinner mPriceCategory;
    @BindView(R.id.rate) RatingBar mRatingBar;

    @BindView(R.id.wifi_switch) Switch mWifiSwitch;
    @BindView(R.id.coffee_switch) Switch mCoffeeSwitch;
    @BindView(R.id.restRoom_switch) Switch mRestRoomSwitch;
    @BindView(R.id.grocery_switch) Switch mGrocerySwitch;
    @BindView(R.id.wc_switch) Switch mWCSwitch;
    @BindView(R.id.serviceStation_switch) Switch mServiceStationSwitch;
    @BindView(R.id.cardPayment_switch) Switch mCardPaymentSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        ButterKnife.bind(this);

        setUpToolbar();

        setFiltersFromPreferences();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(R.string.title_activity_filters);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.reset:
                resetFilters();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFiltersFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.FILTERS_PREFERENCES, MODE_PRIVATE);
        mOnlyFavourites.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_FAVOURITES, false));
        mRestRoomSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_REST_ROOM, false));
        mWifiSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_WIFI, false));
        mWCSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_TOILET, false));
        mCoffeeSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_COFFEE, false));
        mGrocerySwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_SHOP, false));
        mCardPaymentSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_CARD_PAYMENT, false));
        mServiceStationSwitch.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_SERVICE_STATION, false));
        mRatingBar.setRating(
                sharedPreferences.getFloat(Constants.FILTER_MINIMUM_RATING, 0.0f));
        mPriceCategory.setSelection(
                sharedPreferences.getInt(Constants.FILTER_PRICE_CATEGORY, priceCategoriesInt.length-1));
    }

    private void saveFilterToSharedPref() {
        SharedPreferences sp = getSharedPreferences(Constants.FILTERS_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(Constants.FILTER_REST_ROOM, mRestRoomSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_WIFI, mWifiSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_TOILET, mWCSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_COFFEE, mCoffeeSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_SHOP, mGrocerySwitch.isChecked());
        edit.putBoolean(Constants.FILTER_CARD_PAYMENT, mCardPaymentSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_SERVICE_STATION, mServiceStationSwitch.isChecked());
        edit.putBoolean(Constants.FILTER_ONLY_FAVOURITES, mOnlyFavourites.isChecked());
        edit.putFloat(Constants.FILTER_MINIMUM_RATING, mRatingBar.getRating());
        edit.putInt(Constants.FILTER_PRICE_CATEGORY, mPriceCategory.getSelectedItemPosition());
        edit.apply();
    }

    private void resetFilters() {
        mOnlyFavourites.setChecked(false);
        mRestRoomSwitch.setChecked(false);
        mWifiSwitch.setChecked(false);
        mWCSwitch.setChecked(false);
        mCoffeeSwitch.setChecked(false);
        mGrocerySwitch.setChecked(false);
        mCardPaymentSwitch.setChecked(false);
        mServiceStationSwitch.setChecked(false);
        mRatingBar.setRating(0.0f);
        mPriceCategory.setSelection(0);
    }

    @OnClick(R.id.applyBtn)
    public void applyFilters(){
        saveFilterToSharedPref();
        setResult(Constants.FILTER_CHANGED);
        finish();
    }
}
