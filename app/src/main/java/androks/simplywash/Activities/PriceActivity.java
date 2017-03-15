package androks.simplywash.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import androks.simplywash.Constants;
import androks.simplywash.Models.Service;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PriceActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progressBar) View progressBar;
    @BindView(R.id.recyclerLV) RecyclerView recyclerView;
    @BindView(R.id.carTypesSpinner) Spinner carTypesSpinner;

    private String washerId;
    private Map<String, Map<String, Service>> priceList;
    private List<Service> showingPrices = new ArrayList<>();

    private PriceListRecyclerAdapter adapter;

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
                    setUpSpinner();
                    setUpRecyclerView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpSpinner() {
        List<String> carTypes = new ArrayList<>();
        carTypes.addAll(priceList.keySet());
        carTypesSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.spinner_dropdown_toolbar_item,
                carTypes)
        );
        carTypesSpinner.setSelection(0);
        carTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showingPrices.clear();
                showingPrices.addAll(priceList.get(carTypesSpinner.getSelectedItem()).values());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpRecyclerView() {
        showingPrices.addAll(priceList.get(carTypesSpinner.getSelectedItem()).values());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PriceListRecyclerAdapter(showingPrices);
        recyclerView.setAdapter(adapter);
        hideProgress();
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
            getSupportActionBar().setTitle("Service list");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void showProgress() {
        carTypesSpinner.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        carTypesSpinner.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    public static class PriceListRecyclerAdapter extends RecyclerView.Adapter<PriceListRecyclerAdapter.ViewHolder> {

        private List<Service> mValues;

        public PriceListRecyclerAdapter(List<Service> currencies) {
            mValues = currencies;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public PriceListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                      int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_price, parent, false);
            return new PriceListRecyclerAdapter.ViewHolder(v);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        @Override
        public void onBindViewHolder(PriceListRecyclerAdapter.ViewHolder holder, int position) {
            Service price = mValues.get(position);
            holder.name.setText(price.name);
            holder.price.setText(price.price);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }

            @BindView(R.id.name)
            TextView name;
            @BindView(R.id.price)
            TextView price;
        }
    }
}
