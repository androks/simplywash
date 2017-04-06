package androks.simplywash.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

import androks.simplywash.R;
import androks.simplywash.dialogs.FeaturesDialog;
import androks.simplywash.enums.WasherType;
import androks.simplywash.models.Washer;
import androks.simplywash.models.entity.Features;
import androks.simplywash.models.entity.WasherPlace;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by androks on 4/5/2017.
 */

public class AddWasherFragment extends Fragment implements FeaturesDialog.AddServicesDialogListener {

    private static final int PLACE_PICKER_REQUEST = 54;

    @BindView(R.id.name) TextView mName;
    @BindView(R.id.phone) TextView mPhone;
    @BindView(R.id.default_price) TextView mPrice;
    @BindView(R.id.place) TextView mPlaceTV;
    @BindView(R.id.boxes) TextView mBoxesTV;
    @BindView(R.id.services) TextView mServices;
    @BindView(R.id.description) TextView mDescription;
    @BindView(R.id.city) Spinner mCity;
    @BindView(R.id.type) Spinner mType;
    @BindView(R.id.schedule_switch) Switch mScheduleSwitch;
    @BindView(R.id.schedule_layout) View mScheduleLayout;
    @BindView(R.id.schedule) TextView mSchedule;

    private Unbinder unbinder;

    private List<String> mCityList;

    private Washer mWasher = new Washer();

    public AddWasherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_wash, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);
        mWasher.setDefaultValues();
        loadCities();
        initializeWasherTypesSpinner();
        pickPlace();
        return rootView;
    }

    private void initializeWasherTypesSpinner() {
        mType.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                WasherType.values()));
    }

    private void loadCities() {
        Utils.getListOfCities().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    mCityList = dataSnapshot.getValue(new GenericTypeIndicator<List<String>>() {});
                    initializeCitiesSpinner();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeCitiesSpinner() {
        mCity.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                mCityList));
        mCity.setSelection(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_washer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.apply:
                applyWasher();
                return true;
        }
        return false;
    }

    private void applyWasher() {
        if(!validateFields())
            return;
        initializeWasher();
        writeWasherToDB();
    }

    private void initializeWasher() throws NumberFormatException{
        mWasher.setCity(mCityList.get(mCity.getSelectedItemPosition()));
        mWasher.setName(mName.getText().toString());
        mWasher.setPhone(mPhone.getText().toString());
        mWasher.setDefaultPrice(Integer.valueOf(mPrice.getText().toString()));
        mWasher.setType(WasherType.values()[mType.getSelectedItemPosition()]);
        mWasher.setRoundTheClock(true);
        mWasher.setDescription(mDescription.getText().toString());
    }

    private void writeWasherToDB() {
        String id = Utils.getWasher().push().getKey();
        mWasher.setId(id);
        Utils.getWasher().child(id).setValue(mWasher);
    }

    private boolean validateFields() {
        clearError();
        if(mName.getText().toString().isEmpty()){
            mName.setError(getActivity().getResources().getString(R.string.required_error));
            return false;
        }
        if(mPhone.getText().toString().isEmpty()){
            mPhone.setError(getActivity().getResources().getString(R.string.required_error));
            return false;
        }
        if(mPrice.getText().toString().isEmpty()){
            mPrice.setError(getActivity().getResources().getString(R.string.required_error));
            return false;
        }
        return true;
    }

    private void clearError() {
        mName.setError(null);
        mPhone.setError(null);
        mPrice.setError(null);
    }

    @OnClick(R.id.place)
    public void pickPlace(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.boxes_layout)
    public void pickNumOfBoxes(){
        final NumberPicker picker = new NumberPicker(getActivity());
        picker.setMinValue(1);
        picker.setMaxValue(20);

        final FrameLayout layout = new FrameLayout(getActivity());
        layout.addView(picker, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER));

        new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mWasher.setBoxes(picker.getValue());
                        mBoxesTV.setText(String.format(
                                Locale.getDefault(),
                                "%s %s",
                                mWasher.getBoxes(),
                                getActivity().getResources().getString(R.string.boxes)));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        PlacePhotoMetadata metadata ;
    }

    @OnClick(R.id.services_layout)
    public void pickServices(){
        AppCompatDialogFragment featuresDialog = FeaturesDialog.newInstance(mWasher.getFeatures(), this);
        featuresDialog.show(getActivity().getSupportFragmentManager(), FeaturesDialog.TAG_EDITABLE);
    }

    @OnClick(R.id.schedule_layout)
    public void pickSchedule(){

    }

    @OnCheckedChanged(R.id.schedule_switch)
    public void onScheduleCheckedChanged(){
        mScheduleLayout.setVisibility(mScheduleSwitch.isChecked()? View.GONE: View.VISIBLE);
        mWasher.setRoundTheClock(mScheduleSwitch.isChecked());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                inflateWasherInfoByGooglePlace(data);
            }
        }
    }

    private void inflateWasherInfoByGooglePlace(Intent data) {
        Place place = PlacePicker.getPlace(data, getActivity());
        mWasher.setPlace(new WasherPlace(place));
        mPlaceTV.setText(mWasher.getPlace().getAddress());
        mName.setText(place.getName());
        mPhone.setText(mWasher.getPlace().getPhone());
        if(mCityList != null && !mCityList.isEmpty()
                && mCityList.indexOf(Utils.getCityFromPlace(place, getActivity())) >= 0){
            mCity.setSelection(mCityList.indexOf(Utils.getCityFromPlace(place, getActivity())));
        }

    }

    @Override
    public void onServicesAdded(Features features) {
        mWasher.setFeatures(features);
        mServices.setText(Utils.featuresToString(features, getResources()));
    }
}
