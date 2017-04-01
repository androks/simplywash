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

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.progressBar) View mProgressBar;
    @BindView(R.id.recyclerLV) RecyclerView mRecyclerView;
    @BindView(R.id.carTypesSpinner) Spinner mCarTypesSpinner;

    private String mWasherId;
    private Map<String, Map<String, Service>> mPriceList;
    private List<Service> mShowingPrices = new ArrayList<>();
    List<String> mCarTypes = new ArrayList<>();

    private PriceListRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price);
        ButterKnife.bind(this);
        showProgress();

        mWasherId = getIntent().getExtras().getString(Constants.WASHER_ID);

        setUpToolbar();
        downloadPriceList();
    }

    private void downloadPriceList() {
        Utils.getPricesFor(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mPriceList = dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Map<String, Service>>>() {}
                    );
                    initializeStartValues();
                    setUpSpinner();
                    setUpRecyclerView();
                    hideProgress();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeStartValues() {
        mCarTypes.addAll(mPriceList.keySet());
        mShowingPrices.addAll(mPriceList.get(mCarTypes.get(0)).values());
    }

    private void setUpSpinner() {
        mCarTypesSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.spinner_dropdown_toolbar_item,
                mCarTypes)
        );
        mCarTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mShowingPrices.clear();
                String carType = (String) mCarTypesSpinner.getSelectedItem();
                if(carType != null) {
                    mShowingPrices.addAll(mPriceList.get(carType).values());
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new PriceListRecyclerAdapter(mShowingPrices);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
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
        mCarTypesSpinner.setEnabled(false);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mCarTypesSpinner.setEnabled(true);
        mProgressBar.setVisibility(View.GONE);
    }
}
