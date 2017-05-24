package androks.simplywash.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class AddWasherFragment extends Fragment implements FeaturesDialog.AddServicesDialogListener{

    private static final int PLACE_PICKER_REQUEST = 54;

    @BindView(R.id.ll_content) View llContent;
    @BindView(R.id.main_fields) View mainFields;
    @BindView(R.id.progress_bar) View progressBar;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_phone) TextView tvPhone;
    @BindView(R.id.tv_default_price) TextView tvPrice;
    @BindView(R.id.tv_place) TextView tvPlace;
    @BindView(R.id.tv_boxes) TextView tvBoxes;
    @BindView(R.id.ll_services) TextView llServices;
    @BindView(R.id.tv_description) TextView tvDescription;
    @BindView(R.id.spn_city) Spinner spnCity;
    @BindView(R.id.spn_type) Spinner spnType;
    @BindView(R.id.switch_schedule) Switch switchSchedule;

    private ProgressDialog photosProgressDialog;

    private Unbinder unbinder;

    private Washer washer = new Washer();
    //Download list of available cities to add washer in it
    private List<String> cityList;
    //List to store place Id which are already in our database
    private List<String> placeIDs = new ArrayList<>();
    //Variables to handle downloading photos
    private int totalPhotosNum, photoNum, photosUploaded;

    private GoogleApiClient googleApiClient;

    //A result callBack which check if result is successful and if it is, upload photo to storage
    private ResultCallback<PlacePhotoResult> displayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                photosProgressDialog.dismiss();
                onWasherAddedSuccessfully(false);
                return;
            }
            uploadPhotoToFirebaseStorage(placePhotoResult.getBitmap());
        }
    };

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

        washer.setDefaultValues();

        loadListOfWashers();

        loadAvailableCities();

        initializeWasherTypesSpinner();

        buildGoogleApiClient();

        return rootView;
    }

    @SuppressWarnings("VisibleForTests")
    private void uploadPhotoToFirebaseStorage(Bitmap bitmap) {

        WasherPhoto washerPhoto = new WasherPhoto(PhotoType.Firebase, "photo" + photoNum + ".jpg");
        washer.getPhotos().add(washerPhoto);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = Utils.getPhotoStorageRef(washer.getId())
                .child(washerPhoto.getUrl())
                .putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photosUploaded++;
                //Update ProgressDialog
                photosProgressDialog.setMessage(
                        getActivity().getResources().getString(R.string.uploading_photos)
                                + " " + photosUploaded + " / " + totalPhotosNum
                );
                //If total num of photo to upload are uploaded successful - quit
                if (totalPhotosNum == photosUploaded) {
                    onWasherAddedSuccessfully(true);
                }
            }
        });

        photoNum--;
    }

    private void onWasherAddedSuccessfully(boolean value) {
        if (value) {
            writeWasherToDB();
            photosProgressDialog.setMessage(getActivity().getResources().getString(R.string.uploading_washer));
        } else {
            Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private void loadListOfWashers() {
        Utils.getWasher().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    Map<String, Washer> washersList = dataSnapshot.getValue(
                            new GenericTypeIndicator<Map<String, Washer>>() {
                            }
                    );
                    for (Washer washer : washersList.values()) {
                        placeIDs.add(washer.getPlace().getId());
                    }
                }
                hideProgress();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showProgress() {
        if (llContent == null || progressBar == null)
            return;
        llContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        if (llContent == null || progressBar == null)
            return;
        llContent.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void initializeWasherTypesSpinner() {
        spnType.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                WasherType.values()));
    }

    private void loadAvailableCities() {
        Utils.getListOfCities().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    cityList = dataSnapshot.getValue(new GenericTypeIndicator<List<String>>() {
                    });
                    initializeCitiesSpinner();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeCitiesSpinner() {
        if (spnCity == null)
            return;
        spnCity.setAdapter(new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                cityList));
        spnCity.setSelection(0);
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
        if (!validateFields())
            return;
        initializeWasher();
        washer.setId(Utils.getWasher().push().getKey());
        showPhotoDialogProgress();
        uploadPhotos();
    }

    private void showPhotoDialogProgress() {
        photosProgressDialog = new ProgressDialog(getActivity());
        photosProgressDialog.setMessage(getActivity().getResources().getString(R.string.uploading));
        photosProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        photosProgressDialog.setCancelable(false);
        photosProgressDialog.show();
    }

    private void hidePhotoDialogProgress() {
        photosProgressDialog.dismiss();
    }

    private void uploadPhotos() {
        Places.GeoDataApi.getPlacePhotos(googleApiClient, washer.getPlace().getId())
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {
                    @Override
                    public void onResult(@NonNull PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            return;
                        }
                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        totalPhotosNum = photoMetadataBuffer.getCount();
                        photoNum = totalPhotosNum;
                        if (photoMetadataBuffer.getCount() > 0) {
                            for (PlacePhotoMetadata placePhotoMetadata : photoMetadataBuffer) {
                                placePhotoMetadata.getPhoto(googleApiClient)
                                        .setResultCallback(displayPhotoResultCallback);
                            }
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }

    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // only stop if it's connected, otherwise we crash
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }

    //Fill in the washer object
    private void initializeWasher() {
        washer.setCity(cityList.get(spnCity.getSelectedItemPosition()));
        washer.setName(tvName.getText().toString());
        washer.setPhone(tvPhone.getText().toString());
        try {
            washer.setDefaultPrice(Integer.valueOf(tvPrice.getText().toString()));
        } catch (NumberFormatException e) {
            washer.setDefaultPrice(0);
            e.printStackTrace();
        }
        washer.setType(WasherType.values()[spnType.getSelectedItemPosition()]);
        washer.setRoundTheClock(true);
        washer.setDescription(tvDescription.getText().toString());
    }

    private void writeWasherToDB() {
        Utils.getWasher().child(washer.getId()).setValue(washer, new DatabaseReference.CompletionListener() {
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
        if (tvName.getText().toString().isEmpty()) {
            tvName.setError(getActivity().getResources().getString(R.string.required_error));
            return false;
        }
        if (tvPhone.getText().toString().isEmpty()) {
            tvPhone.setError(getActivity().getResources().getString(R.string.required_error));
            return false;
        }

        return true;
    }

    private void clearError() {
        tvName.setError(null);
        tvPhone.setError(null);
        tvPrice.setError(null);
    }

    @OnClick(R.id.tv_place)
    public void pickPlace() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.ll_boxes)
    public void pickNumOfBoxes() {
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
                        washer.setBoxes(picker.getValue());
                        tvBoxes.setText(String.format(
                                Locale.getDefault(),
                                "%s %s",
                                washer.getBoxes(),
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
    public void pickServices() {
        AppCompatDialogFragment featuresDialog = FeaturesDialog.newInstance(washer.getFeatures(), this);
        featuresDialog.show(getActivity().getSupportFragmentManager(), FeaturesDialog.TAG_EDITABLE);
    }

    @OnCheckedChanged(R.id.switch_schedule)
    public void onScheduleCheckedChanged() {
        washer.setRoundTheClock(switchSchedule.isChecked());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                if (!checkPlace(place)) {
                    mainFields.setVisibility(View.GONE);
                    return;
                }
                mainFields.setVisibility(View.VISIBLE);
                inflateWasherInfoByGooglePlace(place);
            }
        }
    }

    //Check if selected place is an car wash, if not - try again
    private boolean checkPlace(Place place) {
        if (!place.getPlaceTypes().contains(Place.TYPE_CAR_WASH)) {
            Toast.makeText(getActivity(), R.string.place_isnt_washer, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (placeIDs.contains(place.getId())) {
            Toast.makeText(getActivity(), R.string.washer_already_registered, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Get data from google database and fill washer object with this data
    private void inflateWasherInfoByGooglePlace(Place place) {
        washer.setPlace(new WasherPlace(place));
        washer.setStreet(Utils.getStreetFromPlace(place, getActivity()));
        washer.setRating(place.getRating());
        washer.setVotes(1);
        tvPlace.setText(washer.getPlace().getAddress());
        tvName.setText(place.getName());
        tvPhone.setText(washer.getPlace().getPhone());
        if (cityList != null && !cityList.isEmpty()
                && cityList.indexOf(Utils.getCityFromPlace(place, getActivity())) >= 0) {
            spnCity.setSelection(cityList.indexOf(Utils.getCityFromPlace(place, getActivity())));
        }
    }

    @Override
    public void onServicesAdded(Features features) {
        washer.setFeatures(features);
        llServices.setText(Utils.featuresToString(features, getResources()));
    }
}
