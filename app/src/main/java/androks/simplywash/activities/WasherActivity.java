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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Locale;

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
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingTL;
    @BindView(R.id.fab_favourite) FloatingActionButton fabFavourites;
    @BindView(R.id.tv_rates_count) TextView tvCountOfRates;
    @BindView(R.id.rating_bar) RatingBar ratingBar;
    @BindView(R.id.tv_rating) TextView tvRating;
    @BindView(R.id.vp_images) ViewPager vpImages;
    @BindView(R.id.images_slideshow_indicator) CirclePageIndicator imageSlideshowIndicator;
    @BindView(R.id.tv_location) TextView tvLocation;
    @BindView(R.id.tv_phone) TextView tvPhone;
    @BindView(R.id.tv_schedule) TextView tvSchedule;
    @BindView(R.id.tv_boxes) TextView tvBoxes;
    @BindView(R.id.tv_favourites) TextView tvCountOfFavourites;
    @BindView(R.id.tv_description) TextView tvDescription;
    @BindView(R.id.tv_is_washer_open) TextView tvIsWasherOpen;
    @BindView(R.id.iv_wifi) ImageView ivWifi;
    @BindView(R.id.im_coffee) ImageView ivCoffee;
    @BindView(R.id.iv_restRoom) ImageView ivRestRoom;
    @BindView(R.id.im_grocery) ImageView ivGrocery;
    @BindView(R.id.im_wc) ImageView ivWC;
    @BindView(R.id.im_service_station) ImageView ivServiceStation;
    @BindView(R.id.im_card_payment) ImageView ivCardPayment;

    private String washerId;
    private Washer washer;

    private boolean flagIsFavorite;

    private boolean ratingHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer);
        ButterKnife.bind(this);

        showProgressDialog();

        getWasherIdFromIntent();

        setupToolbar();

        downloadWasherData();
    }

    private void downloadWasherData() {
        downloadWasherBasicInfo();
        checkIfFavourite();
        downloadReviews();
    }

    private void getWasherIdFromIntent() {
        washerId = getIntent().getStringExtra(Constants.WASHER_ID);
        if (washerId == null)
            onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (ratingHasChanged)
            setResult(Constants.RATING_CHANGED_CODE);
        super.onBackPressed();
    }

    private void setUpPhotoViewPager() {
        vpImages.setAdapter(
                new PhotosPagerAdapter(
                        getSupportFragmentManager(),
                        washer,
                        R.layout.item_collapsing_toolbar_image
                )
        );
        vpImages.setPageTransformer(true, new DepthPageTransformer());
        imageSlideshowIndicator.setViewPager(vpImages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_RATING_CHANGED:
                if (resultCode == Constants.RATING_CHANGED_CODE) {
                    ratingHasChanged = true;
                    downloadWasherBasicInfo();
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
                        flagIsFavorite = dataSnapshot.hasChild(washerId);
                        initializeFavoriteFab();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void initializeFavoriteFab() {
        fabFavourites.setImageResource(flagIsFavorite ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_border_white_24dp
        );
        fabFavourites.setVisibility(View.VISIBLE);
    }

    private void downloadWasherBasicInfo() {
        Utils.getWasher(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                washer = dataSnapshot.getValue(Washer.class);
                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void downloadReviews() {
        Utils.getReviewsFor(washerId).limitToLast(NUM_OF_REVIEWS)
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
                                ((TextView) reviewView.findViewById(R.id.tv_name)).setText(temp.name);
                                ((TextView) reviewView.findViewById(R.id.dateSpinner)).setText(temp.date);
                                ((TextView) reviewView.findViewById(R.id.tv_text)).setText(temp.text);
                                ((RatingBar) reviewView.findViewById(R.id.rating_bar)).setRating(temp.rating);
                                pos++;
                                container.addView(reviewView);
                            }
                            findViewById(R.id.tv_no_items).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateUI() {
        setUpPhotoViewPager();
        collapsingTL.setTitle(washer.getName());

        tvLocation.setText(washer.getPlace().getAddress());
        tvCountOfFavourites.setText(String.valueOf(washer.getFavorites()));
        if (washer.getPlace().getPhone().isEmpty())
            tvPhone.setText(R.string.no_info);
        else
            tvPhone.setText(washer.getPlace().getPhone());

        if (washer.getBoxes() > 0)
            tvBoxes.setText(String.format(
                    Locale.getDefault(),
                    "%d %s",
                    washer.getBoxes(),
                    getResources().getString(R.string.boxes))
            );
        else
            tvBoxes.setText(R.string.no_info);

        if (washer.isRoundTheClock()) {
            tvSchedule.setText(R.string.round_the_clock);
            tvIsWasherOpen.setText(R.string.open);
            tvIsWasherOpen.setTextColor(green);
        } else {
            if (!washer.getSchedule().getScheduleForToday().isEmpty()) {
                tvSchedule.setText(washer.getSchedule().getScheduleForToday());
                if (Utils.isWasherOpenAtTheTime(washer)) {
                    tvIsWasherOpen.setText(R.string.open);
                    tvIsWasherOpen.setTextColor(green);
                } else {
                    tvIsWasherOpen.setText(R.string.closed);
                    tvIsWasherOpen.setTextColor(red);
                }
            } else {
                tvSchedule.setText(R.string.no_info);
            }
        }

        ivWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isWc())));
        ivWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isWifi())));
        ivCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isCoffee())));
        ivGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isShop())));
        ivRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isRestRoom())));
        ivCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isCardPayment())));
        ivServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isServiceStation())));

        updateRatingUI();

        if (washer.getDescription().isEmpty())
            tvDescription.setText(R.string.no_info);
        else
            tvDescription.setText(washer.getDescription());

        hideProgressDialog();
    }

    private void updateRatingUI() {
        ratingBar.setRating(washer.getRating());
        tvRating.setText(String.valueOf(washer.getRating()));
        tvCountOfRates.setText(String.valueOf(washer.getVotes()));
    }


    @OnClick(R.id.btn_add_review)
    public void addReview() {
        AppCompatDialogFragment addReviewDialog = AddReviewDialog.newInstance(washerId);
        addReviewDialog.show(getSupportFragmentManager(), "Add review");
    }

    @OnClick({R.id.ll_more_reviews, R.id.ll_rating})
    public void showMoreReviews() {
        Intent intent = new Intent(WasherActivity.this, ReviewsActivity.class);
        intent.putExtra(Constants.WASHER_ID, washerId);
        startActivityForResult(intent, Constants.REQUEST_RATING_CHANGED);
    }

    @OnClick(R.id.ll_price)
    public void seePrices() {
        Intent intent = new Intent(WasherActivity.this, PriceActivity.class);
        intent.putExtra(Constants.WASHER_ID, washerId);
        startActivity(intent);
    }

    @OnClick(R.id.ll_schedule)
    public void showScheduleDialog() {
        if (!washer.isRoundTheClock()) {
            AppCompatDialogFragment scheduleDialog = ScheduleDialog.newInstance(washer.getSchedule());
            scheduleDialog.show(getSupportFragmentManager(), "Schedule");
        }
    }

    @OnClick(R.id.tv_phone)
    public void callToWasher() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + washer.getPlace().getPhone()));
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.failed_to_call, Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.ll_location)
    public void showWasherOnGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:" + washer.getPlace().getLatitude() + ","
                + washer.getPlace().getLongitude() + "?q="
                + washer.getPlace().getLatitude() + ","
                + washer.getPlace().getLongitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null)
            startActivity(mapIntent);
    }

    @OnClick(R.id.fab_favourite)
    public void toggleFavourite() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(flagIsFavorite ?
                                R.string.remove :
                                R.string.add,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                flagIsFavorite = !flagIsFavorite;
                                fabFavourites.setImageResource(flagIsFavorite ?
                                        R.drawable.ic_favorite_white_24dp :
                                        R.drawable.ic_favorite_border_white_24dp
                                );
                                onFavouriteAdded();
                            }
                        })
                .setMessage(flagIsFavorite ?
                        R.string.request_remove_favorite :
                        R.string.request_add_favorite)
                .setTitle(R.string.confirmation);
        dialogBuilder.show();
    }

    private void onFavouriteAdded() {
        Utils.getWasher(washerId).limitToFirst(1).getRef().runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Washer washer = mutableData.getValue(Washer.class);
                if (washer == null) {
                    return Transaction.success(mutableData);
                }

                if (!flagIsFavorite) {
                    // Unstar the post and remove self from stars
                    washer.decreaseCountOfFavourites();
                    Utils.getFavourites(getCurrentUser().getUid()).child(washerId).removeValue();
                } else {
                    // Star the post and add self to stars
                    washer.increaseCountOfFavourites();
                    Utils.getFavourites(getCurrentUser().getUid()).child(washerId).setValue(true);
                }

                WasherActivity.this.washer = washer;
                // Set value and report transaction success
                mutableData.setValue(washer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Toast.makeText(
                        WasherActivity.this,
                        flagIsFavorite ? R.string.added_favorite : R.string.removed_favorite,
                        Toast.LENGTH_SHORT).show();
                tvCountOfFavourites.setText(String.valueOf(washer.getFavorites()));
            }
        });
    }

    @OnClick(R.id.ll_services)
    public void showServicesDialog() {
        DialogFragment dialog = FeaturesDialog.newInstance(washer.getFeatures());
        dialog.show(getSupportFragmentManager(), "FeaturesDialog");
    }

    @Override
    public void onReviewAdded(final Review review, final float oldRating) {
        ratingHasChanged = true;

        showProgressDialog();

        updateReview(review);

        onRatingChanged(review, oldRating);
    }

    private void onRatingChanged(final Review review, final float oldRating) {
        Utils.getWasher(washerId).runTransaction(new Transaction.Handler() {
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

                WasherActivity.this.washer = washer;
                // Set value and report transaction success
                mutableData.setValue(washer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Toast.makeText(
                        WasherActivity.this,
                        R.string.thanks_for_review,
                        Toast.LENGTH_SHORT).show();
                downloadReviews();
                updateRatingUI();
                hideProgressDialog();
            }
        });
    }

    private void updateReview(Review review) {
        if (review.name.isEmpty())
            review.name = getResources().getString(R.string.anonym);
        Utils.getReviewsFor(washerId).child(getCurrentUser().getUid()).setValue(review);
    }
}
