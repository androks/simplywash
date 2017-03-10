package androks.simplywash.Activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.graphics.Palette;
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

import androks.simplywash.Constants;
import androks.simplywash.Dialogs.AddReviewDialog;
import androks.simplywash.Dialogs.ServicesDialog;
import androks.simplywash.Models.Review;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WasherActivity extends BaseActivity implements AddReviewDialog.AddReviewDialogListener {

    private static final int NUM_OF_REVIEWS = 3;

    //Binding colors
    @BindColor(R.color.green)
    int green;
    @BindColor(R.color.red)
    int red;

    // Binding general views
    @BindView(R.id.animated_toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.favourite_fab)
    FloatingActionButton mFavouritesFab;
    @BindView(R.id.rates_count)
    TextView mCountOfRates;
    @BindView(R.id.rating_bar)
    RatingBar mRatingBar;
    @BindView(R.id.rating_text)
    TextView mRatingText;

    /**
     * Start binding washerInfo views
     */
    @BindView(R.id.location)
    TextView mLocation;
    @BindView(R.id.phone)
    TextView mPhone;
    @BindView(R.id.opening_hours)
    TextView mOpeningHours;
    @BindView(R.id.boxes_status)
    TextView mBoxesStatus;
    @BindView(R.id.favourites_count)
    TextView mCountOfFavourites;
    @BindView(R.id.description)
    TextView mDescription;
    @BindView(R.id.is_washer_open)
    TextView mIsWasherOpen;

    @BindView(R.id.wifi)
    ImageView mWifi;
    @BindView(R.id.coffee)
    ImageView mCoffee;
    @BindView(R.id.restRoom)
    ImageView mRestRoom;
    @BindView(R.id.grocery)
    ImageView mGrocery;
    @BindView(R.id.wc)
    ImageView mWC;
    @BindView(R.id.serviceStation)
    ImageView mServiceStation;
    @BindView(R.id.cardPayment)
    ImageView mCardPayment;
    /**
     * End binding washerInfo views
     */

    private String mWasherId;
    private Washer mWasher;
    private int mutedColor = R.attr.colorPrimary;

    private boolean FLAG_IS_FAVOURITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer);
        ButterKnife.bind(this);
        showProgressDialog();

        mWasherId = getIntent().getStringExtra("id");
        checkForNotNullIntent();

        setUpToolbar();

        initializeFavouriteFab();

        downloadWasherInfo();

        downloadReviews();
    }

    private void initializeFavouriteFab() {
        Utils.getFavourites(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FLAG_IS_FAVOURITE = dataSnapshot.hasChild(mWasherId);
                        mFavouritesFab.setImageResource(FLAG_IS_FAVOURITE ?
                                R.drawable.ic_favorite_white_24dp :
                                R.drawable.ic_favorite_border_white_24dp
                        );
                        mFavouritesFab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView header = (ImageView) findViewById(R.id.header);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.vianor);

        header.setImageBitmap(bitmap);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {
                mutedColor = palette.getMutedColor(R.color.primary_500);
                collapsingToolbarLayout.setContentScrimColor(mutedColor);
                collapsingToolbarLayout.setStatusBarScrimColor(R.color.black_trans80);
            }
        });
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
                                ((TextView) reviewView.findViewById(R.id.date)).setText(temp.date);
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
        collapsingToolbarLayout.setTitle(mWasher.name);

        mLocation.setText(mWasher.location);
        mPhone.setText(mWasher.phone);
        mOpeningHours.setText(Utils.workHoursToString(mWasher));
        mBoxesStatus.setText(mWasher.availableBoxes + " of " + mWasher.boxes);
        mCountOfFavourites.setText(String.valueOf(mWasher.countOfFavourites));

        if (Utils.isWasherOpenAtTheTime(mWasher)) {
            mIsWasherOpen.setText("Open");
            mIsWasherOpen.setTextColor(green);
        } else {
            mIsWasherOpen.setText("Closed");
            mIsWasherOpen.setTextColor(red);
        }

        mWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.toilet)));
        mWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.wifi)));
        mCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.coffee)));
        mGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.shop)));
        mRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.restRoom)));
        mCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.cardPayment)));
        mServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(mWasher.serviceStation)));

        setRatings();

        mDescription.setText(mWasher.description);
        hideProgressDialog();
    }

    private void setRatings() {
        mRatingBar.setRating(mWasher.rating);
        mRatingText.setText(String.valueOf(mWasher.rating));
        mCountOfRates.setText(String.valueOf(mWasher.votesCount));
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
        startActivity(intent);
    }

    @OnClick(R.id.phone_layout)
    public void callToWasher() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mWasher.phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    @OnClick(R.id.location_layout)
    public void showWasherOnGoogleMap() {
        Uri gmmIntentUri = Uri.parse("geo:" + mWasher.latitude + "," + mWasher.longitude + "?q=" + mWasher.latitude + "," + mWasher.longitude);
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
                .setPositiveButton(FLAG_IS_FAVOURITE?
                                R.string.remove:
                                R.string.add,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FLAG_IS_FAVOURITE = !FLAG_IS_FAVOURITE;
                        mFavouritesFab.setImageResource(FLAG_IS_FAVOURITE ?
                                R.drawable.ic_favorite_white_24dp :
                                R.drawable.ic_favorite_border_white_24dp
                        );
                        onFavouriteAdded();
                    }
                })
                .setMessage(FLAG_IS_FAVOURITE?
                        "Do you really wanna remove this washer to favourites?" :
                        "Do you really wanna add this washer to favourites?")
                .setTitle("Favourites");
        dialogBuilder.show();
    }

    private void onFavouriteAdded() {
        Utils.getWasher(mWasherId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Washer washer = mutableData.getValue(Washer.class);
                if (washer == null) {
                    return Transaction.success(mutableData);
                }

                if (!FLAG_IS_FAVOURITE) {
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
                Toast.makeText(
                        WasherActivity.this,
                        FLAG_IS_FAVOURITE ?"Add to favourite":"Removed from favourite",
                        Toast.LENGTH_SHORT).show();
                mCountOfFavourites.setText(String.valueOf(mWasher.countOfFavourites));
            }
        });
    }


    @OnClick(R.id.services)
    public void showServicesDialog() {
        DialogFragment dialog = ServicesDialog.newInstance(mWasher);
        dialog.show(getSupportFragmentManager(), "ServicesDialog");
    }

    @Override
    public void onReviewAdded(final Review review, final float oldRating) {
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

                if(oldRating <= 0.1f) {
                    washer.rating = ((washer.rating * washer.votesCount) + review.rating) / ++washer.votesCount;
                }
                else
                    washer.rating = ((washer.rating * washer.votesCount - oldRating) + review.rating) / washer.votesCount;

                mWasher = washer;
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
                        "Thanks for review",
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
                review.name = "Anonym";
            Utils.getExpandedReviews(mWasherId).child(getCurrentUser().getUid()).setValue(review);
        } else
            Utils.getExpandedReviews(mWasherId).child(getCurrentUser().getUid()).removeValue();
    }
}
