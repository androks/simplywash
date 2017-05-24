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

import com.digits.sdk.android.Digits;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import androks.simplywash.utils.Constants;
import androks.simplywash.R;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.tv_phone) TextView tvPhone;
    @BindView(R.id.spn_city) Spinner spnCity;
    @BindView(R.id.toolbar) Toolbar toolbar;

    private List<String> citiesList;

    private String currentPhone;
    private String currentCity;
    private String lastKnownCity;

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
        currentPhone = sp.getString(Constants.PHONE_PREF, null);
        currentCity = sp.getString(Constants.CITY_PREF, null);
        lastKnownCity = currentCity;
        setPhone();
    }

    private void setPhone() {
        tvPhone.setText(currentPhone);
    }

    private void loadListOfCities() {
        Utils.getListOfCities().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                citiesList = dataSnapshot.getValue(
                        new GenericTypeIndicator<List<String>>() {}
                );
                initializeCitiesSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeCitiesSpinner() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, citiesList);
        spnCity.setAdapter(adapter);

        spnCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!currentCity.equals(citiesList.get(position)))
                    setCurrentCityTo(citiesList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(currentCity != null && citiesList.contains(currentCity))
            spnCity.setSelection(adapter.getPosition(currentCity));
    }

    @Override
    public void onBackPressed() {
        if(checkIfSettingsDataTheSame()) {
            super.onBackPressed();
            return;
        }
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }

    private boolean checkIfSettingsDataTheSame() {
        if(!currentCity.equals(lastKnownCity))
            return false;
        return true;
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            getSupportActionBar().setTitle(R.string.title_activity_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @OnClick(R.id.log_out_btn)
    public void logOutRequest(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.log_out)
                .setMessage(R.string.request_logout)
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
        Digits.logout();
        Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    public void setCurrentCityTo(String city) {
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constants.CITY_PREF, city);
        edit.apply();
        currentCity = city;
    }
}
