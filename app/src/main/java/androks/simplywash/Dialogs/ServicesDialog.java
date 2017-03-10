package androks.simplywash.Dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import androks.simplywash.Models.Washer;
import androks.simplywash.R;
import androks.simplywash.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by androks on 3/10/2017.
 */

public class ServicesDialog extends AppCompatDialogFragment {

    @BindView(R.id.wifi) ImageView mWifi;
    @BindView(R.id.coffee) ImageView mCoffee;
    @BindView(R.id.restRoom) ImageView mRestRoom;
    @BindView(R.id.grocery) ImageView mGrocery;
    @BindView(R.id.wc) ImageView mWC;
    @BindView(R.id.serviceStation) ImageView mServiceStation;
    @BindView(R.id.cardPayment) ImageView mCardPayment;

    @BindView(R.id.wifi_switch) Switch mWifiSwitch;
    @BindView(R.id.coffee_switch) Switch mCoffeeSwitch;
    @BindView(R.id.restRoom_switch) Switch mRestRoomSwitch;
    @BindView(R.id.grocery_switch) Switch mGrocerySwitch;
    @BindView(R.id.wc_switch) Switch mWCSwitch;
    @BindView(R.id.serviceStation_switch) Switch mServiceStationSwitch;
    @BindView(R.id.cardPayment_switch) Switch mCardPaymentSwitch;

    private Washer washer;

    public ServicesDialog() {
        // Empty constructor required for DialogFragment
    }

    public static ServicesDialog newInstance(Washer washer){
        ServicesDialog dialog = new ServicesDialog();
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
        setData();
        return view;
    }



    private void setData() {
        mWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.toilet)));
        mWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.wifi)));
        mCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.coffee)));
        mGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.shop)));
        mRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.restRoom)));
        mCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.cardPayment)));
        mServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(washer.serviceStation)));

        mWCSwitch.setChecked(washer.toilet);
        mWifiSwitch.setChecked(washer.wifi);
        mCardPaymentSwitch.setChecked(washer.cardPayment);
        mCoffeeSwitch.setChecked(washer.coffee);
        mGrocerySwitch.setChecked(washer.shop);
        mRestRoomSwitch.setChecked(washer.restRoom);
        mServiceStationSwitch.setChecked(washer.serviceStation);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.close)
    public void close(){
        this.dismiss();
    }

    public void setWasher(Washer washer) {
        this.washer = washer;
    }
}
