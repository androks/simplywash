package androks.simplywash.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androks.simplywash.Constants;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderDetailsActivity extends AppCompatActivity {

    @BindView(R.id.name) TextView mName;
    @BindView(R.id.status) TextView mStatus;
    @BindView(R.id.phone) TextView mClientPhone;
    @BindView(R.id.price) TextView mPrice;
    @BindView(R.id.date) TextView mDate;
    @BindView(R.id.carType) TextView mCartype;
    @BindView(R.id.services) RecyclerView mServices;
    @BindView(R.id.applyBtn) Button mApplyBtn;

    private boolean isInConfirmMode = false;
    private String mOrderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        ButterKnife.bind(this);

        mOrderId = getIntent().getExtras().getString(Constants.ORDER_ID, null);
        checkIfConfirmMode();
        if(!isInConfirmMode) hideConfirmTools();

        downloadOrderData();
    }

    private void downloadOrderData() {
        Utils.getOrder(mOrderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkIfConfirmMode() {
        isInConfirmMode = getIntent().getExtras().getBoolean(Constants.CONFIRM_ORDER_MODE, false);
    }

    private void hideConfirmTools() {
        mApplyBtn.setVisibility(View.GONE);
        mStatus.setVisibility(View.GONE);
    }

}
