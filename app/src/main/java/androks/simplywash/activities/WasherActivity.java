package androks.simplywash.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

import androks.simplywash.R;
import androks.simplywash.adapters.PhotosPagerAdapter;
import androks.simplywash.dialogs.AddReviewDialog;
import androks.simplywash.dialogs.FeaturesDialog;
import androks.simplywash.dialogs.ScheduleDialog;
import androks.simplywash.models.Review;
import androks.simplywash.models.Washer;
import androks.simplywash.utils.Constants;
import androks.simplywash.utils.DepthPageTransformer;
import androks.simplywash.utils.Utils;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WasherActivity extends BaseActivity implements AddReviewDialog.AddReviewDialogListener {

    private static final int NUM_OF_REVIEWS = 3;

    @BindColor(R.color.green) int green;
    @BindColor(R.color.red) int red;
    @BindView(R.id.animated_toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.favourite_fab) FloatingActionButton mFavouritesFab;
    @BindView(R.id.rates_count) TextView mCountOfRates;
    @BindView(R.id.rating_bar) RatingBar mRatingBar;
    @BindView(R.id.rating_text) TextView mRatingText;
    @BindView(R.id.image_slideshow) ViewPager mPhotosViewPager;
    @BindView(R.id.images_indicator) CirclePageIndicator mImagesIndicator;
    @BindView(R.id.location) TextView mLocation;
    @BindView(R.id.phone) TextView mPhone;
    @BindView(R.id.schedule) TextView mSchedule;
    @BindView(R.id.boxes) TextView mBoxes;
    @BindView(R.id.favourites_count) TextView mCountOfFavourites;
    @BindView(R.id.description) TextView mDescription;
    @BindView(R.id.is_washer_open) TextView mIsWasherOpen;
    @BindView(R.id.wifi) ImageView mWifi;
    @BindView(R.id.coffee) ImageView mCoffee;
    @BindView(R.id.restRoom) ImageView mRestRoom;
    @BindView(R.id.grocery) ImageView mGrocery;
    @BindView(R.id.wc) ImageView mWC;
    @BindView(R.id.serviceStation) ImageView mServiceStation;
    @BindView(R.id.cardPayment) ImageView mCardPayment;

    private String mWasherId;
    private Washer mWasher;

    private boolean mFlagIsFavorite;

    private boolean mRatingHasChanged = false;

    private List<StorageReference> mPhotoReferences = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer);
        ButterKnife.bind(this);
        showProgressDialog();

        mWasherId = getIntent().getStringExtra(Constants.WASHER_ID);

        checkForNotNullIntent();

        setUpToolbar();

        checkIfFavourite();

        downloadPhotoReferences();
    }

    private void downloadPhotoReferences() {
        Utils.getPhotos(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> mUrls = dataSnapshot.getValue(
                        new GenericTypeIndicator<List<String>>() {}
                );
                for(String url: mUrls){
                    mPhotoReferences.add(Utils.getPhotoStorageRef(mWasherId).child(url));
                }
                setUpPhotoViewPager();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        downloadWasherInfo();
        downloadReviews();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(mRatingHasChanged)
            setResult(Constants.RATING_CHANGED_CODE);
        super.onBackPressed();
    }

    private void setUpPhotoViewPager() {
        mPhotosViewPager.setAdapter(
                new PhotosPagerAdapter(
                        getSupportFragmentManager(),
                        mPhotoReferences,
                        R.layout.item_coll_toolbar_image
                )
        );
        mPhotosViewPager.setPageTransformer(true, new DepthPageTransformer());
        mImagesIndicator.setViewPager(mPhotosViewPager);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constants.REQUEST_RATING_CHANGED:
                if(resultCode == Constants.RATING_CHANGED_CODE) {
                    mRatingHasChanged = true;
                    downloadWasherInfo();
                    downloadReviews();
                }
                break;
        }
    }

    private void checkIfFavourite() {
        Utils.getFavourites(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mFlagIsFavorite = dataSnapshot.hasChild(mWasherId);
                        initialFavoriteFab();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initialFavoriteFab(){
        mFavouritesFab.setImageResource(mFlagIsFavorite ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_border_white_24dp
        );
        mFavouritesFab.setVisibility(View.VISIBLE);
    }

    private void downloadWasherInfo() {
        Utils.getWasher(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mWasher = dataSnapshot.getValue(Washer.class);
                inflateView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void checkForNotNullIntent() {
        if (mWasherId == null)
            onBackPressed();
    }

    private void downloadReviews() {
        Utils.getExpandedReviews(mWasherId).limitToLast(NUM_OF_REVIEWS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        LinearLayout container = (LinearLayout) findViewById(R.id.review_container);
                        container.removeAllViews();
                        if (dataSnapshot.hasChildren()) {
                            int pos = 0;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Review temp = child.getValue(Review.class);

                                LayoutInflater inflater =
                                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View reviewView = inflater.inflate(R.layout.item_review, container, false);
                                reviewView.setId(pos);
                                ((TextView) reviewView.findViewById(R.id.name)).setText(temp.name);
                                ((TextView) reviewView.findViewById(R.id.dateSpinner)).setText(temp.date);
                                ((TextView) reviewView.findViewById(R.id.text)).setText(temp.text);
                                ((RatingBar) reviewView.findViewById(R.id.rate)).setRating(temp.rating);
                                pos++;
                                container.addView(reviewView);
                            }
                            findViewById(R.id.no_items).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void inflateView() {
        mCollapsingToolbarLayout.setTitle(mWasher.getName());

        mLocation.setText(mWasher.getPlace().getAddress());
        mPhone.setText(mWasher.getPhone());
        mBoxes.setText(String.valueOf(mWasher.getBoxes()));
        mCountOfFavourites.setText(String.valueOf(mWasher.getFavorites()));

        if(mWasher.isRoundTheClock())
            mSchedule.setText(R.string.round_the_clock);
        else
            mSchedule.setText(mWasher.getSchedule().getScheduleForToday());

        if (Utils.isWasherOpenAtTheTime(mWasher)) {
            mIsWasherOpen.setText(R.string.open);
            mIsWasherOpen.setTextColor(green);
        } else {
            mIsWasherOpen.setText(R.string.closed);
            mIsWasherOpen.setTextColor(red);
        }

        mWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isWc())));
        mWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isWifi())));
        mCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isCoffee())));
        mGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isShop())));
        mRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isRestRoom())));
        mCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isCardPayment())));
        mServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.getFeatures().isServiceStation())));

        setRatings();

        mDescription.setText(mWasher.getDescription());
        hideProgressDialog();
    }

    private void setRatings() {
        mRatingBar.setRating(mWasher.getRating());
        mRatingText.setText(String.valueOf(mWasher.getRating()));
        mCountOfRates.setText(String.valueOf(mWasher.getVotes()));
    }


    @OnClick(R.id.add_review_btn)
    public void addReview() {
        AppCompatDialogFragment addReviewDialog = AddReviewDialog.newInstance(mWasherId);
        addReviewDialog.show(getSupportFragmentManager(), "Add review");
    }

    @OnClick({R.id.more_reviews, R.id.rating_section})
    public void showMoreReviews() {
        Intent intent = new Intent(WasherActivity.this, ReviewsActivity.class);
        intent.putExtra(Constants.WASHER_ID, mWasherId);
        startActivityForResult(intent, Constants.REQUEST_RATING_CHANGED);
    }

    @OnClick(R.id.price_layout)
    public void seePrices() {
        Intent intent = new Intent(WasherActivity.this, PriceActivity.class);
        intent.putExtra(Constants.WASHER_ID, mWasherId);
        startActivity(intent);
    }

    @OnClick(R.id.schedule_layout)
    public void showScheduleDialog(){
        if(!mWasher.isRoundTheClock()){
            AppCompatDialogFragment scheduleDialog = ScheduleDialog.newInstance(mWasher.getSchedule());
            scheduleDialog.show(getSupportFragmentManager(), "Schedule");
        }
    }

    @OnClick(R.id.phone)
    public void callToWasher() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+mWasher.getPhone()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), R.string.failed_to_call,Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.location_layout)
    public void showWasherOnGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:" + mWasher.getPlace().getLatitude() + ","
                + mWasher.getPlace().getLongitude() + "?q="
                + mWasher.getPlace().getLatitude() + ","
                + mWasher.getPlace().getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null)
            startActivity(mapIntent);
    }

    @OnClick(R.id.favourite_fab)
    public void toggleFavourite() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(mFlagIsFavorite ?
                                R.string.remove :
                                R.string.add,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mFlagIsFavorite = !mFlagIsFavorite;
                                mFavouritesFab.setImageResource(mFlagIsFavorite ?
                                        R.drawable.ic_favorite_white_24dp :
                                        R.drawable.ic_favorite_border_white_24dp
                                );
                                onFavouriteAdded();
                            }
                        })
                .setMessage(mFlagIsFavorite ?
                        R.string.request_remove_favorite :
                        R.string.request_add_favorite)
                .setTitle(R.string.confirmation);
        dialogBuilder.show();
    }

    private void onFavouriteAdded() {
        Utils.getWasher(mWasherId).limitToFirst(1).getRef().runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Washer washer = mutableData.getValue(Washer.class);
                if (washer == null) {
                    return Transaction.success(mutableData);
                }

                if (!mFlagIsFavorite) {
                    // Unstar the post and remove self from stars
                    washer.decreaseCountOfFavourites();
                    Utils.getFavourites(getCurrentUser().getUid()).child(mWasherId).removeValue();
                } else {
                    // Star the post and add self to stars
                    washer.increaseCountOfFavourites();
                    Utils.getFavourites(getCurrentUser().getUid()).child(mWasherId).setValue(true);
                }

                mWasher = washer;
                // Set value and report transaction success
                mutableData.setValue(washer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Utils.getWasherInCity(getCurrentCity(), mWasher.getId()).setValue(mWasher);
                Toast.makeText(
                        WasherActivity.this,
                        mFlagIsFavorite ? R.string.added_favorite : R.string.removed_favorite,
                        Toast.LENGTH_SHORT).show();
                mCountOfFavourites.setText(String.valueOf(mWasher.getFavorites()));
            }
        });
    }

    @OnClick(R.id.services)
    public void showServicesDialog() {
        DialogFragment dialog = FeaturesDialog.newInstance(mWasher);
        dialog.show(getSupportFragmentManager(), "FeaturesDialog");
    }

    @Override
    public void onReviewAdded(final Review review, final float oldRating) {
        mRatingHasChanged = true;

        showProgressDialog();

        updateExpandedReviews(review);

        Utils.getReviewsFor(mWasherId).child(getCurrentUser().getUid()).setValue(review);

        onRatingChanged(review, oldRating);
    }

    private void onRatingChanged(final Review review, final float oldRating) {
        Utils.getWasher(mWasherId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Washer washer = mutableData.getValue(Washer.class);
                if (washer == null) {
                    return Transaction.success(mutableData);
                }

                if (oldRating <= 0.1f)
                    washer.setRating(((washer.getRating() * washer.getVotes()) + review.rating)
                            / washer.increaseCountOfVotes());
                else
                    washer.setRating(
                            ((washer.getRating() * washer.getVotes() - oldRating) + review.rating)
                                    / washer.getVotes()
                    );

                mWasher = washer;
                // Set value and report transaction success
                mutableData.setValue(washer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Utils.getWasherInCity(getCurrentCity(), mWasher.getId()).setValue(mWasher);
                Toast.makeText(
                        WasherActivity.this,
                        R.string.thanks_for_review,
                        Toast.LENGTH_SHORT).show();
                downloadReviews();
                setRatings();
                hideProgressDialog();
            }
        });
    }

    private void updateExpandedReviews(Review review) {
        if (!review.text.isEmpty()) {
            if (review.name.isEmpty())
                review.name = getResources().getString(R.string.anonym);
            Utils.getExpandedReviews(mWasherId).child(getCurrentUser().getUid()).setValue(review);
        } else
            Utils.getExpandedReviews(mWasherId).child(getCurrentUser().getUid()).removeValue();
    }
}
