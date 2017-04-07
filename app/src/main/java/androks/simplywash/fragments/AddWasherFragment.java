package androks.simplywash.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androks.simplywash.R;
import androks.simplywash.activities.MainActivity;
import androks.simplywash.dialogs.FeaturesDialog;
import androks.simplywash.enums.PhotoType;
import androks.simplywash.enums.WasherType;
import androks.simplywash.models.Washer;
import androks.simplywash.models.entity.Features;
import androks.simplywash.models.entity.WasherPhoto;
import androks.simplywash.models.entity.WasherPlace;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AddWasherFragment extends Fragment implements FeaturesDialog.AddServicesDialogListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int PLACE_PICKER_REQUEST = 54;

    @BindView(R.id.content) View mContent;
    @BindView(R.id.main_fields) View mMainFields;
    @BindView(R.id.progress) View mProgress;
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

    private ProgressDialog mPhotosProgressDialog;

    private Unbinder unbinder;

    private Washer mWasher = new Washer();
    private List<String> mCityList;
    private List<String> mPlaceIds = new ArrayList<>();
    private int mTotalPhotosNum = 0;
    private int mPhotoNum = 0;
    private int mPhotosUploaded = 0;
    private GoogleApiClient mGoogleApiClient;

    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                mPhotosProgressDialog.dismiss();
                onWasherAddedSuccessfully(false);
                return;
            }
            uploadPhotoToFirebaseStorage(placePhotoResult.getBitmap());
        }
    };

    @SuppressWarnings("VisibleForTests")
    private void uploadPhotoToFirebaseStorage(Bitmap bitmap) {

        WasherPhoto washerPhoto = new WasherPhoto(PhotoType.Firebase, "photo" + mPhotoNum + ".jpg");
        mWasher.getPhotos().add(washerPhoto);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = Utils.getPhotoStorageRef(mWasher.getId())
                .child(washerPhoto.getUrl())
                .putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mPhotosUploaded++;
                mPhotosProgressDialog.setMessage(
                        getActivity().getResources().getString(R.string.uploading_photos)
                                + " " + mPhotosUploaded + " / " + mTotalPhotosNum
                );
                if(mTotalPhotosNum == mPhotosUploaded){
                    onWasherAddedSuccessfully(true);
                }
            }
        });

        mPhotoNum--;
    }

    public AddWasherFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_wash, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        showProgress();

        mWasher.setDefaultValues();

        loadListOfWashers();

        loadCities();

        initializeWasherTypesSpinner();

        buildGoogleApiClient();

        return rootView;
    }

    private void onWasherAddedSuccessfully(boolean value){
        if(value) {
            writeWasherToDB();
            mPhotosProgressDialog.setMessage(getActivity().getResources().getString(R.string.uploading_washer));
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void loadListOfWashers() {
        Utils.getWasher().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    initializePlaceIds(dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {}
                    ));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializePlaceIds(Map<String, Washer> washersList){
        for(Washer washer: washersList.values()){
            mPlaceIds.add(washer.getPlace().getId());
        }
    }

    private void showProgress(){
        mContent.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    private void hideProgress(){
        mContent.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
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
        mWasher.setId(Utils.getWasher().push().getKey());
        showPhotoDialogProgress();
        uploadPhotos();
    }

    private void showPhotoDialogProgress() {
        mPhotosProgressDialog = new ProgressDialog(getActivity());
        mPhotosProgressDialog.setMessage(getActivity().getResources().getString(R.string.uploading));
        mPhotosProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mPhotosProgressDialog.setCancelable(false);
        mPhotosProgressDialog.show();
    }

    private void hidePhotoDialogProgress(){
        mPhotosProgressDialog.dismiss();
    }

    private void uploadPhotos() {
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, mWasher.getPlace().getId())
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                    @Override
                    public void onResult(@NonNull PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            return;
                        }
                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        mTotalPhotosNum = photoMetadataBuffer.getCount();
                        mPhotoNum = mTotalPhotosNum;
                        if (photoMetadataBuffer.getCount() > 0) {
                            for(PlacePhotoMetadata placePhotoMetadata : photoMetadataBuffer){
                                placePhotoMetadata.getPhoto(mGoogleApiClient)
                                        .setResultCallback(mDisplayPhotoResultCallback);
                            }
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // only stop if it's connected, otherwise we crash
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    private void initializeWasher(){
        mWasher.setCity(mCityList.get(mCity.getSelectedItemPosition()));
        mWasher.setName(mName.getText().toString());
        mWasher.setPhone(mPhone.getText().toString());
        try {
            mWasher.setDefaultPrice(Integer.valueOf(mPrice.getText().toString()));
        }catch (NumberFormatException e){
            mWasher.setDefaultPrice(0);
            e.printStackTrace();
        }
        mWasher.setType(WasherType.values()[mType.getSelectedItemPosition()]);
        mWasher.setRoundTheClock(true);
        mWasher.setDescription(mDescription.getText().toString());
    }

    private void writeWasherToDB() {
        Utils.getWasher().child(mWasher.getId()).setValue(mWasher, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hidePhotoDialogProgress();
                Toast.makeText(getActivity(), R.string.thanks_for_adding_washer, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
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
    }

    @OnClick(R.id.services_layout)
    public void pickServices(){
        AppCompatDialogFragment featuresDialog = FeaturesDialog.newInstance(mWasher.getFeatures(), this);
        featuresDialog.show(getActivity().getSupportFragmentManager(), FeaturesDialog.TAG_EDITABLE);
    }

    @OnCheckedChanged(R.id.schedule_switch)
    public void onScheduleCheckedChanged(){
        mWasher.setRoundTheClock(mScheduleSwitch.isChecked());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                if(!checkPlace(place)) {
                    mMainFields.setVisibility(View.GONE);
                    return;
                }
                mMainFields.setVisibility(View.VISIBLE);
                inflateWasherInfoByGooglePlace(place);
            }
        }
    }

    private boolean checkPlace(Place place) {
        if(!place.getPlaceTypes().contains(Place.TYPE_CAR_WASH)) {
            Toast.makeText(getActivity(), R.string.place_isnt_washer, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mPlaceIds.contains(place.getId())){
            Toast.makeText(getActivity(), R.string.washer_already_registered, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void inflateWasherInfoByGooglePlace(Place place) {
        mWasher.setPlace(new WasherPlace(place));
        mWasher.setStreet(Utils.getStreetFromPlace(place, getActivity()));
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
