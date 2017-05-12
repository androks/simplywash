package androks.simplywash.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androks.simplywash.R;
import androks.simplywash.models.entity.Features;
import androks.simplywash.utils.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by androks on 3/10/2017.
 */

public class FeaturesDialog extends AppCompatDialogFragment {

    public interface AddServicesDialogListener {
        void onServicesAdded(Features features);
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

    private Features features;

    private AddServicesDialogListener mListener;

    public FeaturesDialog() {
        // Empty constructor required for DialogFragment
    }

    public static FeaturesDialog newInstance(Features features) {
        FeaturesDialog dialog = new FeaturesDialog();
        dialog.setFeatures(features);
        return dialog;
    }

    public static FeaturesDialog newInstance(Features features, AddServicesDialogListener listener) {
        FeaturesDialog dialog = new FeaturesDialog();
        dialog.setListener(listener);
        dialog.setFeatures(features);
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
        mWCSwitch.setClickable(true);
        mWifiSwitch.setClickable(true);
        mCoffeeSwitch.setClickable(true);
        mRestRoomSwitch.setClickable(true);
        mGrocerySwitch.setClickable(true);
        mServiceStationSwitch.setClickable(true);
        mCardPaymentSwitch.setClickable(true);
        mApplyBtn.setVisibility(View.VISIBLE);
    }

    //This dialog can be used as just preview of available services
    //or It can be used when we want to set up available services
    private void checkMode() {
        //If tag equal to TAG_EDITABLE we apply editable mode
        if (getTag().equals(FeaturesDialog.TAG_EDITABLE)) {
            applyEditMode();
            setFields();
        }
        //if not, just set data to switches and show
        else
            setData();
    }


    private void setData() {
        setPicturesColors();
        setFields();
    }

    private void setFields() {
        mWCSwitch.setChecked(features.isWc());
        mWifiSwitch.setChecked(features.isWifi());
        mCardPaymentSwitch.setChecked(features.isCardPayment());
        mCoffeeSwitch.setChecked(features.isCoffee());
        mGrocerySwitch.setChecked(features.isShop());
        mRestRoomSwitch.setChecked(features.isRestRoom());
        mServiceStationSwitch.setChecked(features.isServiceStation());
    }

    private void setPicturesColors() {
        mWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isWc())));
        mWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isWifi())));
        mCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isCoffee())));
        mGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isShop())));
        mRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isRestRoom())));
        mCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isCardPayment())));
        mServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isServiceStation())));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.applyBtn)
    public void applyServices() {
        mListener.onServicesAdded(new Features(
                    mRestRoomSwitch.isChecked(),
                    mWifiSwitch.isChecked(),
                    mWCSwitch.isChecked(),
                    mCoffeeSwitch.isChecked(),
                    mGrocerySwitch.isChecked(),
                    mCardPaymentSwitch.isChecked(),
                    mServiceStationSwitch.isChecked()));
        dismiss();
    }

    @OnClick(R.id.close)
    public void close() {
        this.dismiss();
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public void setListener(AddServicesDialogListener mListener) {
        this.mListener = mListener;
    }
}
