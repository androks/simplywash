package androks.simplywash.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androks.simplywash.models.Washer;
import androks.simplywash.R;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by androks on 3/10/2017.
 */

public class FeaturesDialog extends AppCompatDialogFragment {

    public interface AddServicesDialogListener {
        void onServicesAdded(boolean wifi, boolean coffee, boolean restRoom, boolean grocery,
                             boolean wc, boolean serviceStation, boolean cardPayment);
    }

    public static final String TAG_EDITABLE = "TAG_EDITABLE";

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

    @BindView(R.id.wifi_switch)
    Switch mWifiSwitch;
    @BindView(R.id.coffee_switch)
    Switch mCoffeeSwitch;
    @BindView(R.id.restRoom_switch)
    Switch mRestRoomSwitch;
    @BindView(R.id.grocery_switch)
    Switch mGrocerySwitch;
    @BindView(R.id.wc_switch)
    Switch mWCSwitch;
    @BindView(R.id.serviceStation_switch)
    Switch mServiceStationSwitch;
    @BindView(R.id.cardPayment_switch)
    Switch mCardPaymentSwitch;

    @BindView(R.id.applyBtn)
    View mApplyBtn;

    private Washer washer;

    public FeaturesDialog() {
        // Empty constructor required for DialogFragment
    }

    public static FeaturesDialog newInstance(Washer washer) {
        FeaturesDialog dialog = new FeaturesDialog();
        dialog.setWasher(washer);
        return dialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_services, container, false);
        ButterKnife.bind(this, view);
        Dialog dialog = getDialog();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        checkMode();
        return view;
    }

    private void applyEditMode() {
        mWC.setEnabled(true);
        mWifi.setEnabled(true);
        mCoffee.setEnabled(true);
        mRestRoom.setEnabled(true);
        mGrocery.setEnabled(true);
        mServiceStation.setEnabled(true);
        mCardPayment.setEnabled(true);
        mApplyBtn.setVisibility(View.VISIBLE);
    }

    private void checkMode() {
        if (getTag().equals(FeaturesDialog.TAG_EDITABLE))
            applyEditMode();
        else
            setData();
    }


    private void setData() {
        mWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isWc())));
        mWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isWifi())));
        mCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isCoffee())));
        mGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isShop())));
        mRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isRestRoom())));
        mCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isCardPayment())));
        mServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.getFeatures().isServiceStation())));

        mWCSwitch.setChecked(washer.getFeatures().isWc());
        mWifiSwitch.setChecked(washer.getFeatures().isWifi());
        mCardPaymentSwitch.setChecked(washer.getFeatures().isCardPayment());
        mCoffeeSwitch.setChecked(washer.getFeatures().isCoffee());
        mGrocerySwitch.setChecked(washer.getFeatures().isShop());
        mRestRoomSwitch.setChecked(washer.getFeatures().isRestRoom());
        mServiceStationSwitch.setChecked(washer.getFeatures().isServiceStation());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.applyBtn)
    public void applyServices() {
        try {
            ((AddServicesDialogListener) getActivity()).onServicesAdded(
                    mWifi.isSelected(),
                    mCoffee.isSelected(),
                    mRestRoom.isSelected(),
                    mGrocery.isSelected(),
                    mWC.isSelected(),
                    mServiceStation.isSelected(),
                    mCardPayment.isSelected()
            );
        } catch (ClassCastException ignored) {
        }
        dismiss();
    }

    @OnClick(R.id.close)
    public void close() {
        this.dismiss();
    }

    public void setWasher(Washer washer) {
        this.washer = washer;
    }
}
