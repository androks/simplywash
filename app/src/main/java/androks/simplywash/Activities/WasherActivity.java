package androks.simplywash.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androks.simplywash.Dialogs.AddReviewDialog;
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
    @BindView(R.id.rest_room)
    ImageView mRestRoom;
    @BindView(R.id.grocery)
    ImageView mGrocery;
    @BindView(R.id.wc)
    ImageView mWC;
    @BindView(R.id.tire)
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

        checkIfWasherIfFavourite();

        downloadWasherInfo();

        downloadReviews();
    }

    private void checkIfWasherIfFavourite() {
        Utils.getFavourites(getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FLAG_IS_FAVOURITE = dataSnapshot.hasChild(mWasherId);
                        initializeFavouriteFab();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void initializeFavouriteFab(){
        mFavouritesFab.setImageResource(FLAG_IS_FAVOURITE ?
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
                        if (dataSnapshot.hasChildren()) {
                            LinearLayout container = (LinearLayout) findViewById(R.id.review_container);
                            container.removeAllViews();
                            int pos = 0;

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Review temp = child.getValue(Review.class);

                                LayoutInflater inflater =
                                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View reviewView = inflater.inflate(R.layout.item_review, container, false);
                                reviewView.setId(pos);
                                ((TextView) reviewView.findViewById(R.id.name)).setText(temp.getName());
                                ((TextView) reviewView.findViewById(R.id.date)).setText(temp.getDate());
                                ((TextView) reviewView.findViewById(R.id.text)).setText(temp.getText());
                                ((RatingBar) reviewView.findViewById(R.id.rate)).setRating(temp.getRating());
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
        collapsingToolbarLayout.setTitle(mWasher.getName());

        mLocation.setText(mWasher.getLocation());
        mPhone.setText(mWasher.getPhone());
        mOpeningHours.setText(Utils.workHoursToString(mWasher));
        mBoxesStatus.setText(mWasher.getAvailableBoxes() + " of " + mWasher.getBoxes());
        mCountOfFavourites.setText(String.valueOf(mWasher.getCountOfFavourites()));

        if (Utils.isWasherOpenAtTheTime(mWasher)) {
            mIsWasherOpen.setText("Open");
            mIsWasherOpen.setTextColor(green);
        } else {
            mIsWasherOpen.setText("Closed");
            mIsWasherOpen.setTextColor(red);
        }

        mWC.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isToilet()));
        mWifi.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isWifi()));
        mCoffee.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isCoffee()));
        mGrocery.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isShop()));
        mRestRoom.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isRestRoom()));
        mCardPayment.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isCardPayment()));
        mServiceStation.setColorFilter(Utils.getServiceAvailabledColor(mWasher.isServiceStation()));

        mDescription.setText(mWasher.getDescription());
        hideProgressDialog();
    }


    @OnClick(R.id.add_review_btn)
    public void addReview() {
        AppCompatDialogFragment addReviewDialog = AddReviewDialog.newInstance(mWasherId);
        addReviewDialog.show(getSupportFragmentManager(), "Add review");
    }

    @OnClick(R.id.more_reviews)
    public void showMoreReviews() {

    }

    @OnClick(R.id.phone_layout)
    public void callToWasher() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mWasher.getPhone()));
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
        Uri gmmIntentUri = Uri.parse("geo:" + mWasher.getLangtitude() + "," + mWasher.getLongtitude() + "?q=" + mWasher.getLangtitude() + "," + mWasher.getLongtitude());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null)
            startActivity(mapIntent);
    }

    @OnClick(R.id.favourite_fab)
    public void toggleFavourite() {
        FLAG_IS_FAVOURITE = !FLAG_IS_FAVOURITE;
        mFavouritesFab.setImageResource(FLAG_IS_FAVOURITE ?
                R.drawable.ic_favorite_white_24dp :
                R.drawable.ic_favorite_border_white_24dp
        );
        mCountOfFavourites.setText(String.valueOf(FLAG_IS_FAVOURITE?
                mWasher.increaseCountOfFavourites():
                mWasher.decreaseCountOfFavourites()
        ));
    }

    @Override
    public void onReviewAdded(String userPhone, Review review, float oldRating) {
        showProgressDialog();
        if (!review.getText().isEmpty()) {
            if (review.getName().isEmpty())
                review.setName("Anonym");
            Utils.getExpandedReviews(mWasherId).child(userPhone).setValue(review);
        }
        Utils.getReviewsFor(mWasherId).child(userPhone).setValue(review, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                hideProgressDialog();
                Toast.makeText(WasherActivity.this, "Added", Toast.LENGTH_SHORT).show();
                downloadReviews();
            }
        });
        mWasher.updateRate(oldRating, review.getRating());
    }
}
