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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androks.simplywash.Adapters.CheckedPriceListRecyclerAdapter;
import androks.simplywash.Constants;
import androks.simplywash.Enums.Day;
import androks.simplywash.Models.Service;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class OrderActivity extends AppCompatActivity implements
        CheckedPriceListRecyclerAdapter.ServiceSelect {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.dateSpinner)
    Spinner dateSpinner;
    @BindView(R.id.carTypesSpinner)
    Spinner carTypesSpinner;
    @BindView(R.id.price)
    TextView price;
    @BindView(R.id.servicesRecyclerView)
    RecyclerView servicesRecycleView;
    @BindView(R.id.timeText)
    TextView timeText;

    private String washerId;
    private Day selectedDay;
    private Washer washer;
    private String selectedTime;
    private String selectedCarType;
    private int totalPrice = 0;

    private List<String> availableTimes = new ArrayList<>();
    private Map<String, Map<String, ArrayList<String>>> busyTimes;

    private Map<String, Map<String, Service>> priceList;
    private List<Service> showingServices = new ArrayList<>();
    private List<Service> selectedServices = new ArrayList<>();

    private CheckedPriceListRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);
        setUpToolbar();
        washerId = getIntent().getExtras().getString(Constants.WASHER_ID);
        downloadBusyTimes();
        downloadPriceList();
    }

    private void downloadWasher() {
        Utils.getWasher(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                washer = dataSnapshot.getValue(Washer.class);
                inflateSpinner();
                getSupportActionBar().setTitle(washer.getName());
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
                timeText.setText("Pick time");
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
                android.R.layout.simple_spinner_dropdown_item,
                carTypes)
        );
        carTypesSpinner.setSelection(0);
        carTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item;
                try{
                    item = (String) carTypesSpinner.getSelectedItem();
                }catch (Exception ignored){
                    item = (String) carTypesSpinner.getItemAtPosition(0);
                };
                showingServices.clear();
                showingServices.addAll(priceList.get(item).values());
                adapter.notifyDataSetChanged();
                price.setText("0 UAH");
                selectedCarType = item;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpRecyclerView() {
        showingServices.addAll(priceList.get(carTypesSpinner.getSelectedItem()).values());
        servicesRecycleView.setHasFixedSize(true);
        servicesRecycleView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckedPriceListRecyclerAdapter(showingServices, this);
        servicesRecycleView.setAdapter(adapter);
    }

    @OnClick(R.id.time)
    public void pickTime() {
        String[] values = new String[availableTimes.size()];
        values = availableTimes.toArray(values);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick time")
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedTime = availableTimes.get(which);
                        timeText.setText(selectedTime);
                    }
                });
        builder.create().show();
    }

    @OnClick(R.id.date)
    public void datePick() {
        dateSpinner.performClick();
    }

    @Override
    public void onServiceSelected(Service service) {
        if (service.isSelected()) {
            totalPrice += service.getPrice();
            selectedServices.add(service);
        } else {
            totalPrice -= service.getPrice();
            selectedServices.remove(service);
        }
        price.setText(String.valueOf(totalPrice + " UAH"));
    }

    @OnClick(R.id.applyBtn)
    public void applyOrder() {
        if (!validateData())
            return;
        String listOfServices = "";
        for(Service temp: selectedServices)
            listOfServices+= temp.getName() + ", ";
        String dataCheck = "Washer: " + washer.getName() + "\nAt address: " + washer.getLocation()
                + "\nTime: " + selectedTime + "\nDate: " + selectedDay.name()
                + "\nYour car: " + selectedCarType
                + "\nList of services: " + listOfServices
                + "\nTotal price: " + totalPrice;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick time")
                .setTitle("Check data")
                .setMessage(dataCheck)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createOrder();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void createOrder() {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, selectedDay.ordinal());
        //Todo:Write to Db and direct to confirm
    }

    private boolean validateData() {
        if (selectedServices.size() < 1) {
            Toast.makeText(this, "Please, pick services", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedTime == null || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please, pick time", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
