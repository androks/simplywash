package androks.simplywash.Activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androks.simplywash.Constants;
import androks.simplywash.Enums.Day;
import androks.simplywash.Models.Service;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.date)
    Spinner dateSpinner;
    @BindView(R.id.carTypesSpinner)
    Spinner carTypesSpinner;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.servicesRecycleView)
    RecyclerView servicesRecycleView;
    @BindView(R.id.timeBtn)
    Button timeBtn;

    private String washerId;
    private Day selectedDay;
    private Washer washer;
    private String selectedTime;

    private List<String> availableTimes = new ArrayList<>();
    private Map<String, Map<String, ArrayList<String>>> busyTimes;

    private Map<String, Map<String, Service>> priceList;
    private List<Service> showingPrices = new ArrayList<>();

    private PriceActivity.PriceListRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);
        washerId = getIntent().getExtras().getString(Constants.WASHER_ID);
        downloadBusyTimes();
        downloadPriceList();
    }

    private void downloadWasher() {
        Utils.getWasher(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                washer = dataSnapshot.getValue(Washer.class);
                setUpToolbar();
                inflateSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void inflateSpinner() {
        dateSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Day.values()
        ));
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDay = Day.values()[position];
                fillAvailableTimes();
                setUpAvailableTimes();
                timeBtn.performClick();
                timeBtn.setText(availableTimes.get(0));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void fillAvailableTimes() {
        availableTimes.clear();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        minute = Utils.castMinuteToFormat(minute);
        if (dateSpinner.getSelectedItem() != Day.Today) {
            hour = 0;
            minute = 0;
        }


        while (hour < 24) {
            while (minute <= 60) {
                if (minute == 60) {
                    minute = 0;
                    break;
                }
                availableTimes.add(String.format(
                        Locale.getDefault(),
                        String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                        hour, Utils.castMinuteToFormat(minute)
                ));
                minute += 15;
            }
            hour++;
        }
    }


    private void downloadBusyTimes() {
        Utils.getScheduleFor(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    busyTimes = dataSnapshot.getValue(
                            new GenericTypeIndicator
                                    <Map<String, Map<String, ArrayList<String>>>>() {
                            }
                    );
                    downloadWasher();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpAvailableTimes() {
        if (busyTimes.containsKey(selectedDay.name()))
            for (Map.Entry<String, ArrayList<String>> temp :
                    busyTimes.get(selectedDay.name()).entrySet())
                if (temp.getValue().size() >= washer.getBoxes())
                    availableTimes.remove(temp.getKey());
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(washer.getName());
        }
    }

    private void downloadPriceList() {
        Utils.getPricesFor(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    priceList = dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Map<String, Service>>>() {
                            }
                    );
                    setUpCarTypesSpinner();
                    setUpRecyclerView();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpCarTypesSpinner() {
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
        servicesRecycleView.setHasFixedSize(true);
        servicesRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PriceActivity.PriceListRecyclerAdapter(showingPrices);
        servicesRecycleView.setAdapter(adapter);
    }

    @OnClick({R.id.timeBtn, R.id.time})
    public void pickTime() {
        String[] values = new String[availableTimes.size()];
        values = availableTimes.toArray(values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick time")
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTime = availableTimes.get(which);
                        timeBtn.setText(selectedTime);
                    }
                });
        builder.create().show();
    }
}
