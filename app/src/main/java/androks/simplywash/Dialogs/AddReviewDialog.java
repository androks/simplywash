package androks.simplywash.Dialogs;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androks.simplywash.Constants;
import androks.simplywash.Models.Review;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by androks on 1/22/2017.
 */

public class AddReviewDialog extends AppCompatDialogFragment{

    private float oldRating = 0.0f;

    public interface AddReviewDialogListener {
        void onReviewAdded(Review review, float oldRating);
    }

    @BindView(R.id.text) EditText mReviewText;
    @BindView(R.id.rate) RatingBar mRatingBar;
    @BindView(R.id.name) EditText mName;
    @BindView(R.id.add) Button mBtnAdd;
    @BindView(R.id.cancel) Button mBtnCancel;

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
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        );
        uploadReview();
    }

    private void uploadReview() {
        mReviewReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren())
                    mReview = dataSnapshot.getValue(Review.class);

                inflateAndEnableViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inflateAndEnableViews() {
        if(mReview != null){
            mBtnAdd.setText(R.string.edit);
            oldRating = mReview.rating;
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

    @OnClick(R.id.cancel)
    public void cancel(){
        this.dismiss();
    }

    @OnClick(R.id.add)
    public void addReview(){
        String name = mName.getText().toString();
        String text = mReviewText.getText().toString();
        Float rating = mRatingBar.getRating();
        if(text.isEmpty())
            name = "";
        // Return input text to activity
        ((AddReviewDialogListener) getActivity()).onReviewAdded(new Review(name, text, rating), oldRating);
        this.dismiss();
    }

}
