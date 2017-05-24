package androks.simplywash.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androks.simplywash.adapters.PriceListRecyclerAdapter;
import androks.simplywash.utils.Constants;
import androks.simplywash.models.Service;
import androks.simplywash.R;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PriceActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progress_bar) View progressBar;
    @BindView(R.id.rv_prices_list) RecyclerView rvPricesList;
    @BindView(R.id.spn_car_types) Spinner spnCarTypes;
    @BindView(R.id.tv_no_items) TextView tvNoItems;

    private String washerId;
    private Map<String, Map<String, Service>> priceList;
    private List<Service> showingPrices = new ArrayList<>();
    List<String> carTypes = new ArrayList<>();

    private PriceListRecyclerAdapter rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);
        ButterKnife.bind(this);
        showProgress();

        washerId = getIntent().getExtras().getString(Constants.WASHER_ID);

        setUpToolbar();
        downloadPriceList();
    }

    private void downloadPriceList() {
        Utils.getPricesFor(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    priceList = dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Map<String, Service>>>() {}
                    );
                    initializeStartValues();
                    setUpSpinner();
                    setUpRecyclerView();
                    hideProgress();
                }else{
                    showNoPricesMessage();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showNoPricesMessage() {
        spnCarTypes.setVisibility(View.GONE);
        hideProgress();
        tvNoItems.setVisibility(View.VISIBLE);
    }

    private void initializeStartValues() {
        carTypes.addAll(priceList.keySet());
        showingPrices.addAll(priceList.get(carTypes.get(0)).values());
    }

    private void setUpSpinner() {
        spnCarTypes.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.spinner_dropdown_toolbar_item,
                carTypes)
        );
        spnCarTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showingPrices.clear();
                String carType = (String) spnCarTypes.getSelectedItem();
                if(carType != null) {
                    showingPrices.addAll(priceList.get(carType).values());
                    rvAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpRecyclerView() {
        rvPricesList.setHasFixedSize(true);
        rvPricesList.setLayoutManager(new LinearLayoutManager(this));
        rvAdapter = new PriceListRecyclerAdapter(showingPrices);
        rvPricesList.setAdapter(rvAdapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_price);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showProgress() {
        spnCarTypes.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        spnCarTypes.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
}
