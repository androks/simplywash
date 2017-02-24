package androks.simplywash.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;

import androks.simplywash.Models.Review;
import androks.simplywash.R;


/**
 * Created by androks on 1/22/2017.
 */

public class AddReviewDialog extends AppCompatDialogFragment implements View.OnClickListener {

    public interface AddReviewDialogListener {
        void onReviewAdded(Review review);
    }

    private EditText mTextEdit;
    private RatingBar mRatingBar;
    private TextInputLayout mTextLayout;

    public AddReviewDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_review, container, false);
        Dialog dialog = getDialog();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        mTextEdit = (EditText) view.findViewById(R.id.review_text);
        mRatingBar = (RatingBar) view.findViewById(R.id.review_rate);
        mTextLayout = (TextInputLayout) view.findViewById(R.id.review_layout);

        view.findViewById(R.id.add).setOnClickListener(this);
        view.findViewById(R.id.cancel).setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancel:
                this.dismiss();
                break;

            case R.id.add:
                if(!validateForm()){
                    break;
                }
                // Return input text to activity
                ((AddReviewDialogListener) getActivity()).onReviewAdded(new Review(mTextEdit.getText().toString(), mRatingBar.getRating()));
                this.dismiss();
                break;
        }

    }

    private boolean validateForm() {
        if(mRatingBar.getRating() < 0 || mRatingBar.getRating() > 5) {
            return false;
        }
        else if(mTextEdit.getText().toString().trim().length() < 5){
            mTextLayout.setError("Must be more than 5 characters");
            return false;
        }
        return true;
    }
}
