package androks.simplywash.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androks.simplywash.Constants;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.phone) TextView mPhone;
    @BindView(R.id.city) Spinner mCitySpinner;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    private String mCurrentPhone;
    private String mCurrentCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setUpToolbar();

        getDataFromPref();

        loadListOfCities();
    }

    private void getDataFromPref() {
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        mCurrentPhone = sp.getString(Constants.PHONE_PREF, null);
        mCurrentCity = sp.getString(Constants.CITY_PREF, null);
        setPhone();
    }

    private void setPhone() {
        mPhone.setText(mCurrentPhone);
    }

    private void loadListOfCities() {
        Utils.getListOfCities().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> cities = dataSnapshot.getValue(
                        new GenericTypeIndicator<List<String>>() {}
                );
                initializeCitiesSpinner(cities);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeCitiesSpinner(final List<String> items) {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        mCitySpinner.setAdapter(adapter);

        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setCurrentCityTo(items.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(mCurrentCity != null && items.contains(mCurrentCity))
            mCitySpinner.setSelection(adapter.getPosition(mCurrentCity));
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setTitle(R.string.title_activity_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }
    }

    @OnClick(R.id.log_out_btn)
    public void logOutRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.log_out)
                .setMessage("Do you really wanna log out?")
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constants.PHONE_PREF, null);
        edit.putString(Constants.CITY_PREF, null);
        edit.apply();
        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
        finish();
    }

    public void setCurrentCityTo(String city) {
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constants.PHONE_PREF, city);
        edit.apply();
    }
}
