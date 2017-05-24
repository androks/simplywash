package androks.simplywash.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import androks.simplywash.utils.Constants;
import androks.simplywash.R;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChooseCityActivity extends BaseActivity {

    @BindView(R.id.rv_cities) RecyclerView rvCities;
    @BindView(R.id.progress_bar) View progressBar;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_city);
        ButterKnife.bind(this);
        checkCityIsAlreadyChoosen();

        setUpToolbar();
        setupCityRecyclerView();
    }

    //If city is already set, go to MainActivity
    private void checkCityIsAlreadyChoosen(){
        if (getCurrentCity() != null) {
            startActivity(new Intent(ChooseCityActivity.this, MainActivity.class));
            finish();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_choose_city_activity);
        }
    }

    private void setupCityRecyclerView() {
        rvCities.setHasFixedSize(true);
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(new FirebaseRecyclerAdapter<String, ViewHolder>(
                String.class,
                R.layout.simple_list_item,
                ViewHolder.class,
                Utils.getListOfCities()
        ) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, final String model, int position) {
                hideProgress();
                viewHolder.name.setText(model);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectCity(model);
                        startActivity(new Intent(ChooseCityActivity.this, MainActivity.class));
                        finish();
                    }
                });
            }
        });
    }

    //Select current user city and write the data to the SharedPreferences
    private void selectCity(String city) {
        SharedPreferences sp = getSharedPreferences(Constants.AUTH_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(Constants.CITY_PREF, city);
        edit.apply();
    }

    private void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_text) TextView name;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}
