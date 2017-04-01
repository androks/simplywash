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
        AddReviewDialog.AddReviewDialogListener{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.rates_count) TextView mCountOfRates;
    @BindView(R.id.rating_bar) RatingBar mRatingBar;
    @BindView(R.id.rating_text) TextView mRatingText;
    @BindView(R.id.recyclerLV) RecyclerView mRecyclerLV;
    @BindView(R.id.content) View mContent;
    @BindView(R.id.progressBar) View mProgressBar;
    @BindView(R.id.empty_list) View mListEmptyList;

    private String mWasherId;
    private Washer mWasher;

    FirebaseRecyclerAdapter mRecyclerAdapter;

    private boolean mDataHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        ButterKnife.bind(this);
        showProgress();

        getDataFromIntent();

        setUpToolbar();

        setUpRecyclerLV();

        downloadCommonData();
    }

    private void getDataFromIntent() {
        mWasherId = getIntent().getExtras().getString(Constants.WASHER_ID, null);
        if(mWasherId == null){
            onBackPressed();
            finish();
        }
    }

    private void downloadCommonData() {
        Utils.getWasher(mWasherId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()) {
                    mWasher = dataSnapshot.getValue(Washer.class);
                    setRatings();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setRatings() {
        mRatingBar.setRating(mWasher.getRating());
        mRatingText.setText(String.valueOf(mWasher.getRating()));
        mCountOfRates.setText(String.valueOf(mWasher.getVotes()));
        hideProgress();
    }

    private void setUpRecyclerLV() {
        mRecyclerLV.setHasFixedSize(true);
        mRecyclerLV.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerAdapter = new FirebaseRecyclerAdapter<Review, ViewHolder>(
                Review.class,
                R.layout.item_review,
                ViewHolder.class,
                Utils.getExpandedReviews(mWasherId)
        ) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Review model, int position) {
                viewHolder.name.setText(model.name);
                viewHolder.date.setText(model.date);
                viewHolder.text.setText(model.text);
                viewHolder.rate.setRating(model.rating);
            }
        };
        mRecyclerLV.setAdapter(mRecyclerAdapter);
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_activity_reviews);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mDataHasChanged)
            setResult(Constants.RATING_CHANGED_CODE);
        finish();
    }

    private void showProgress(){
        mContent.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mListEmptyList.setVisibility(View.GONE);
    }

    private void hideProgress(){
        mContent.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        if(mRecyclerAdapter.getItemCount() <= 0)
            mListEmptyList.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.add_review_btn)
    public void addReview(){
        AppCompatDialogFragment addReviewDialog = AddReviewDialog.newInstance(mWasherId);
        addReviewDialog.show(getSupportFragmentManager(), "Add review");
    }

    @Override
    public void onReviewAdded(final Review review, final float oldRating) {
        showProgress();
        mDataHasChanged = true;
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

                if(oldRating <= 0.1f)
                    washer.setRating(((washer.getRating() * washer.getVotes()) + review.rating)
                            / washer.increaseCountOfFavourites());
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
                        ReviewsActivity.this,
                        R.string.thanks_for_review,
                        Toast.LENGTH_SHORT).show();
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        @BindView(R.id.name) TextView name;
        @BindView(R.id.text) TextView text;
        @BindView(R.id.rate) RatingBar rate;
        @BindView(R.id.dateSpinner) TextView date;
    }
}
