package androks.simplywash.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import androks.simplywash.Models.AddReviewDialog;
import androks.simplywash.Models.Review;
import androks.simplywash.Models.Washer;
import androks.simplywash.R;

public class WasherDetailsActivity extends BaseActivity implements View.OnClickListener, AddReviewDialog.AddReviewDialogListener {

    private CollapsingToolbarLayout mCollapsingToolbar;
    private String mWasherId;
    private Washer mWasher;
    private View mNoReviewView;
    private ListView listView;
    private int mutedColor = R.attr.colorPrimary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer_details);
        showProgressDialog();

        mWasherId = getIntent().getStringExtra("id");

        final Toolbar toolbar = (Toolbar) findViewById(R.id.anim_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Upload washer info
        FirebaseDatabase.getInstance().getReference()
                .child("washers")
                .child(mWasherId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mWasher = dataSnapshot.getValue(Washer.class);
                inflateView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
    }

    private void inflateView() {
        mCollapsingToolbar.setTitle(mWasher.getName());
        ((TextView) findViewById(R.id.stars)).setText(String.valueOf(mWasher.getStars() + " people like it"));
        ((TextView) findViewById(R.id.washer_location)).setText(mWasher.getLocation());
        ((TextView) findViewById(R.id.washer_phone)).setText(mWasher.getPhone());
        ((TextView) findViewById(R.id.washer_opening_hours)).setText(mWasher.getHours());
        ((TextView) findViewById(R.id.free_boxes)).setText(mWasher.getFreeBoxes() + " of " + mWasher.getBoxes() + " boxes are free");
        ((ImageView) findViewById(R.id.wifi)).setColorFilter(mWasher.getWifi() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.coffee)).setColorFilter(mWasher.getCafe() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.lunch_room)).setColorFilter(mWasher.getLunchRoom() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.rest_room)).setColorFilter(mWasher.getRestRoom() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.wc)).setColorFilter(mWasher.getWc() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        ((ImageView) findViewById(R.id.tire)).setColorFilter(mWasher.getTire() ?
                ContextCompat.getColor(this, R.color.colorServiceAvailable): ContextCompat.getColor(this, R.color.colorServiceNotAvailable));
        hideProgressDialog();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_review_btn:
                AppCompatDialogFragment addReviewDialog = new AddReviewDialog();
                addReviewDialog.show(getSupportFragmentManager(), "Add review");
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
