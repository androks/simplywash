package androks.simplywash.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import androks.simplywash.utils.Constants;
import androks.simplywash.dialogs.AddReviewDialog;
import androks.simplywash.models.Review;
import androks.simplywash.models.Washer;
import androks.simplywash.R;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReviewsActivity extends BaseActivity implements
        AddReviewDialog.AddReviewDialogListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_rates_count) TextView tvRatesCount;
    @BindView(R.id.rating_bar) RatingBar ratingBar;
    @BindView(R.id.tv_rating) TextView tvRating;
    @BindView(R.id.rv_prices_list) RecyclerView rvPriceList;
    @BindView(R.id.ll_content) View llContent;
    @BindView(R.id.progress_bar) View progressBar;
    @BindView(R.id.tv_empty_list) View tvEmptyList;

    private String washerId;
    private Washer washer;

    FirebaseRecyclerAdapter rvAdapter;

    private boolean dataHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);
        showProgress();

        getDataFromIntent();

        setUpToolbar();

        setUpReviewsRecyclerLV();

        downloadWasherInfo();
    }

    private void getDataFromIntent() {
        washerId = getIntent().getExtras().getString(Constants.WASHER_ID, null);
        if (washerId == null) {
            onBackPressed();
            finish();
        }
    }

    private void downloadWasherInfo() {
        Utils.getWasher(washerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    washer = dataSnapshot.getValue(Washer.class);
                    updateRating();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateRating() {
        ratingBar.setRating(washer.getRating());
        tvRating.setText(String.valueOf(washer.getRating()));
        tvRatesCount.setText(String.valueOf(washer.getVotes()));
        hideProgress();
    }

    private void setUpReviewsRecyclerLV() {
        rvPriceList.setHasFixedSize(true);
        rvPriceList.setLayoutManager(new LinearLayoutManager(this));
        rvAdapter = new FirebaseRecyclerAdapter<Review, ViewHolder>(
                Review.class,
                R.layout.item_review,
                ViewHolder.class,
                Utils.getReviewsFor(washerId)
        ) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Review model, int position) {
                viewHolder.name.setText(model.name);
                viewHolder.date.setText(model.date);
                viewHolder.text.setText(model.text);
                viewHolder.rate.setRating(model.rating);
            }
        };
        rvPriceList.setAdapter(rvAdapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_reviews);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (dataHasChanged)
            setResult(Constants.RATING_CHANGED_CODE);
        finish();
    }

    private void showProgress() {
        llContent.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyList.setVisibility(View.GONE);
    }

    private void hideProgress() {
        llContent.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        if (rvAdapter.getItemCount() <= 0)
            tvEmptyList.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_add_review)
    public void addReview() {
        AppCompatDialogFragment addReviewDialog = AddReviewDialog.newInstance(washerId);
        addReviewDialog.show(getSupportFragmentManager(), "Add review");
    }

    @Override
    public void onReviewAdded(final Review review, final float oldRating) {
        showProgress();
        dataHasChanged = true;
        updateReview(review);
        onRatingChanged(review, oldRating);
    }

    private void updateReview(Review review) {
        if (review.name.isEmpty())
            review.name = getResources().getString(R.string.anonym);
        Utils.getReviewsFor(washerId).child(getCurrentUser().getUid()).setValue(review);
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

                ReviewsActivity.this.washer = washer;
                // Set value and report transaction success
                mutableData.setValue(washer);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Toast.makeText(
                        ReviewsActivity.this,
                        R.string.thanks_for_review,
                        Toast.LENGTH_SHORT).show();
                updateRating();
                hideProgressDialog();
            }
        });
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @BindView(R.id.tv_name)
        TextView name;
        @BindView(R.id.tv_text)
        TextView text;
        @BindView(R.id.rating_bar)
        RatingBar rate;
        @BindView(R.id.dateSpinner)
        TextView date;
    }
}
