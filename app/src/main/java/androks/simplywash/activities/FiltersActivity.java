package androks.simplywash.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;

import androks.simplywash.R;
import androks.simplywash.utils.Constants;
import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FiltersActivity extends BaseActivity {

    @BindArray(R.array.priceCategoriesInt) int[] priceCategoriesInt;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.switch_favorites) Switch switchFavorites;
    @BindView(R.id.spn_price_category) Spinner spnPriceCategory;
    @BindView(R.id.rating_bar) RatingBar ratingBar;
    @BindView(R.id.switch_wifi) Switch switchWifi;
    @BindView(R.id.switch_coffee) Switch switchCoffee;
    @BindView(R.id.switch_rest_room) Switch switchRestRoom;
    @BindView(R.id.switch_grocery) Switch switchGrocery;
    @BindView(R.id.switch_wc) Switch switchWC;
    @BindView(R.id.switch_service_station) Switch switchServiceStation;
    @BindView(R.id.switch_cardPayment) Switch switchCardPayment;
    @BindView(R.id.switch_open_now) Switch switchOnlyOpen;

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

    //Upload filters from SharedPref and set field values
    private void setFiltersFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.FILTERS_PREFERENCES, MODE_PRIVATE);
        switchFavorites.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_FAVOURITES, false));
        switchOnlyOpen.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_ONLY_OPEN, false));
        switchRestRoom.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_REST_ROOM, false));
        switchWifi.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_WIFI, false));
        switchWC.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_TOILET, false));
        switchCoffee.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_COFFEE, false));
        switchGrocery.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_SHOP, false));
        switchCardPayment.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_CARD_PAYMENT, false));
        switchServiceStation.setChecked(
                sharedPreferences.getBoolean(Constants.FILTER_SERVICE_STATION, false));
        ratingBar.setRating(
                sharedPreferences.getFloat(Constants.FILTER_MINIMUM_RATING, 0.0f));
        spnPriceCategory.setSelection(
                sharedPreferences.getInt(Constants.FILTER_PRICE_CATEGORY, priceCategoriesInt.length-1));
    }

    //Get fields and write them to sharedPref
    private void saveFilterToSharedPref() {
        SharedPreferences sp = getSharedPreferences(Constants.FILTERS_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(Constants.FILTER_REST_ROOM, switchRestRoom.isChecked());
        edit.putBoolean(Constants.FILTER_ONLY_OPEN, switchOnlyOpen.isChecked());
        edit.putBoolean(Constants.FILTER_WIFI, switchWifi.isChecked());
        edit.putBoolean(Constants.FILTER_TOILET, switchWC.isChecked());
        edit.putBoolean(Constants.FILTER_COFFEE, switchCoffee.isChecked());
        edit.putBoolean(Constants.FILTER_SHOP, switchGrocery.isChecked());
        edit.putBoolean(Constants.FILTER_CARD_PAYMENT, switchCardPayment.isChecked());
        edit.putBoolean(Constants.FILTER_SERVICE_STATION, switchServiceStation.isChecked());
        edit.putBoolean(Constants.FILTER_ONLY_FAVOURITES, switchFavorites.isChecked());
        edit.putFloat(Constants.FILTER_MINIMUM_RATING, ratingBar.getRating());
        edit.putInt(Constants.FILTER_PRICE_CATEGORY, spnPriceCategory.getSelectedItemPosition());
        edit.apply();
    }

    //Reset all fields to default value
    private void resetFilters() {
        switchFavorites.setChecked(false);
        switchOnlyOpen.setChecked(false);
        switchRestRoom.setChecked(false);
        switchWifi.setChecked(false);
        switchWC.setChecked(false);
        switchCoffee.setChecked(false);
        switchGrocery.setChecked(false);
        switchCardPayment.setChecked(false);
        switchServiceStation.setChecked(false);
        ratingBar.setRating(0.0f);
        spnPriceCategory.setSelection(priceCategoriesInt.length-1);
    }

    @OnClick(R.id.btn_apply)
    public void applyFilters(){
        saveFilterToSharedPref();
        setResult(Constants.FILTER_CHANGED_CODE);
        finish();
    }
}
