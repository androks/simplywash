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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androks.simplywash.Dialogs.AddReviewDialog;
import androks.simplywash.Entity.TabEntity;
import androks.simplywash.Models.Price;
import androks.simplywash.Models.PricesFragmentPagerAdapter;
import androks.simplywash.Models.Review;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;

public class WasherDetailsActivity extends BaseActivity implements View.OnClickListener, AddReviewDialog.AddReviewDialogListener {

    private static final int NUM_OF_REVIEWS = 3;
    private final String[] mTitles = {"Car", "SUV", "Minivan"};
    private final int[] mIconUnselectIds = {
            R.drawable.ic_sedan, R.drawable.ic_suv,
            R.drawable.ic_minivan};
    private final int[] mIconSelectIds = {
            R.drawable.ic_sedan_selected, R.drawable.ic_suv_selected,
            R.drawable.ic_minivan_selected};
    private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
    private CommonTabLayout mTabLayout;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private String mWasherId;
    private Washer mWasher;
    private HashMap<String, Price> mPrices = new HashMap<>();
    private int mutedColor = R.attr.colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer);
        showProgressDialog();

        mWasherId = getIntent().getStringExtra("id");

        final Toolbar toolbar = (Toolbar) findViewById(R.id.animated_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Upload washer info
        FirebaseDatabase.getInstance().getReference().child("washers").child(mWasherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mWasher = dataSnapshot.getValue(Washer.class);
                inflateView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Upload washer's prices
        FirebaseDatabase.getInstance().getReference().child("prices").child(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Price temp = child.getValue(Price.class);
                        mPrices.put(child.getKey(), temp);
                    }
                    inflatePrices();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        downloadReviews();

        //Setting up toolbar image and color
        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        ImageView header = (ImageView) findViewById(R.id.header);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.vianor);
        header.setImageBitmap(bitmap);
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @SuppressWarnings("ResourceType")
            @Override
            public void onGenerated(Palette palette) {
                mutedColor = palette.getMutedColor(R.color.primary_500);
                mCollapsingToolbar.setContentScrimColor(mutedColor);
                mCollapsingToolbar.setStatusBarScrimColor(R.color.black_trans80);
            }
        });

        //Setting up on listeners
        findViewById(R.id.add_review_btn).setOnClickListener(this);
        findViewById(R.id.phone_layout).setOnClickListener(this);
        findViewById(R.id.location_layout).setOnClickListener(this);
        findViewById(R.id.more_reviews).setOnClickListener(this);
    }

    private void downloadReviews() {
        FirebaseDatabase.getInstance().getReference().child("reviews").child(mWasherId).limitToLast(NUM_OF_REVIEWS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    LinearLayout container = (LinearLayout) findViewById(R.id.review_container);
                    int pos = 0;

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Review temp = child.getValue(Review.class);

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View reviewView = inflater.inflate(R.layout.item_review, null);
                        reviewView.setId(pos);
                        ((TextView) reviewView.findViewById(R.id.email)).setText(temp.getEmail());
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
        mCollapsingToolbar.setTitle(mWasher.getName());
        ((TextView) findViewById(R.id.stars)).setText(String.valueOf(mWasher.getStars() + " people like it"));
        ((TextView) findViewById(R.id.location)).setText(mWasher.getLocation());
        ((TextView) findViewById(R.id.phone)).setText(mWasher.getPhone());
        ((TextView) findViewById(R.id.opening_hours)).setText(mWasher.getHours());
        ((TextView) findViewById(R.id.free_boxes)).setText(mWasher.getFreeBoxes() + " of " + mWasher.getBoxes() + " boxes are free");
        ((ImageView) findViewById(R.id.wifi)).setColorFilter(mWasher.getWifi() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.coffee)).setColorFilter(mWasher.getCafe() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.lunch_room)).setColorFilter(mWasher.getLunchRoom() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.rest_room)).setColorFilter(mWasher.getRestRoom() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.wc)).setColorFilter(mWasher.getWc() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.tire)).setColorFilter(mWasher.getTire() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable) : ContextCompat.getColor(this, R.color.colorServiceNotAvailable));

        ((TextView) findViewById(R.id.description)).setText(mWasher.getDescription());
        hideProgressDialog();
    }

    private void inflatePrices() {
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnselectIds[i]));
        }

        mTabLayout = (CommonTabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setTabData(mTabEntities);
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new PricesFragmentPagerAdapter(getSupportFragmentManager(), mPrices));

        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                viewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_review_btn:
                AppCompatDialogFragment addReviewDialog = new AddReviewDialog();
                addReviewDialog.show(getSupportFragmentManager(), "Add review");
                break;
            case R.id.more_reviews:

                break;
            case R.id.phone_layout:
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
                break;

            case R.id.location_layout:
                Uri gmmIntentUri = Uri.parse("geo:" + mWasher.getLangtitude() + "," + mWasher.getLongtitude() + "?q="  + mWasher.getLangtitude() + "," + mWasher.getLongtitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null)
                    startActivity(mapIntent);
                break;
        }
    }


    @Override
    public void onReviewAdded(Review review) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        review.setDate(sdf.format(new Date()));
        review.setWasherId(mWasherId);
        review.setUid(getCurrentUser().getUid());
        review.setEmail(getCurrentUser().getEmail());
        String id = FirebaseDatabase.getInstance().getReference().child("reviews").child(review.getWasherId()).push().getKey();
        FirebaseDatabase.getInstance().getReference().child("reviews").child(review.getWasherId()).child(id).setValue(review);
    }
}
