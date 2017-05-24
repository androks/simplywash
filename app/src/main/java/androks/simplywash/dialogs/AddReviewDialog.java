package androks.simplywash.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androks.simplywash.R;
import androks.simplywash.activities.BaseActivity;
import androks.simplywash.models.Review;
import androks.simplywash.utils.Constants;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by androks on 1/22/2017.
 */

public class AddReviewDialog extends AppCompatDialogFragment{

    public interface AddReviewDialogListener {
        void onReviewAdded(Review review, float oldRating);
    }

    @BindView(R.id.tv_text) EditText mReviewText;
    @BindView(R.id.rating_bar) RatingBar mRatingBar;
    @BindView(R.id.tv_name) EditText mName;
    @BindView(R.id.btn_add) Button mBtnAdd;
    @BindView(R.id.btn_cancel) Button mBtnCancel;

    //Set oldRating to default value
    private float oldRating = 0.0f;

    private DatabaseReference mReviewReference;
    private Review mReview;

    public AddReviewDialog() {
        // Empty constructor required for DialogFragment
    }

    public static AddReviewDialog newInstance(String washerId){
        AddReviewDialog dialog = new AddReviewDialog();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.WASHER_ID, washerId);
        dialog.setArguments(arguments);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mReviewReference = Utils.getUserReview(
                bundle.getString(Constants.WASHER_ID),
                ((BaseActivity)getActivity()).getCurrentUser().getUid()
        );
        downloadReview();
    }

    private void downloadReview() {
        mReviewReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    mReview = dataSnapshot.getValue(Review.class);

                updateUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        if(mReview != null){
            oldRating = mReview.rating;
            if(mReview.rating >= oldRating)
                mBtnAdd.setText(R.string.edit);
            mName.setText(mReview.name);
            mReviewText.setText(mReview.text);
            mRatingBar.setRating(mReview.rating);
        }

        mName.setEnabled(true);
        mReviewText.setEnabled(true);
        mBtnAdd.setEnabled(true);
        mBtnCancel.setEnabled(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_review, container, false);
        ButterKnife.bind(this, view);
        Dialog dialog = getDialog();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.btn_cancel)
    public void cancel(){
        this.dismiss();
    }

    @OnClick(R.id.btn_add)
    public void addReview(){
        String name = mName.getText().toString();
        String text = mReviewText.getText().toString();
        Float rating = mRatingBar.getRating();
        if(text.isEmpty())
            name = "";
        // Return input text to activity
        ((AddReviewDialogListener) getActivity()).onReviewAdded(
                new Review(name, text, rating), oldRating
        );
        this.dismiss();
    }

}
