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

    @BindView(R.id.iv_wifi) ImageView ivWifi;
    @BindView(R.id.im_coffee) ImageView ivCoffee;
    @BindView(R.id.iv_restRoom) ImageView ivRestRoom;
    @BindView(R.id.im_grocery) ImageView ivGrocery;
    @BindView(R.id.im_wc) ImageView ivWC;
    @BindView(R.id.im_service_station) ImageView ivServiceStation;
    @BindView(R.id.im_card_payment) ImageView ivCardPayment;

    @BindView(R.id.switch_wifi) Switch switchWifi;
    @BindView(R.id.switch_coffee) Switch switchCoffee;
    @BindView(R.id.switch_rest_room) Switch switchRestRoom;
    @BindView(R.id.switch_grocery) Switch switchGrocery;
    @BindView(R.id.switch_wc) Switch switchWC;
    @BindView(R.id.switch_service_station) Switch switchServiceStation;
    @BindView(R.id.switch_cardPayment) Switch switchCardPayment;

    @BindView(R.id.btn_apply) View btnApply;

    private Features features;

    private AddServicesDialogListener listener;

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
        switchWC.setClickable(true);
        switchWifi.setClickable(true);
        switchCoffee.setClickable(true);
        switchRestRoom.setClickable(true);
        switchGrocery.setClickable(true);
        switchServiceStation.setClickable(true);
        switchCardPayment.setClickable(true);
        btnApply.setVisibility(View.VISIBLE);
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
        switchWC.setChecked(features.isWc());
        switchWifi.setChecked(features.isWifi());
        switchCardPayment.setChecked(features.isCardPayment());
        switchCoffee.setChecked(features.isCoffee());
        switchGrocery.setChecked(features.isShop());
        switchRestRoom.setChecked(features.isRestRoom());
        switchServiceStation.setChecked(features.isServiceStation());
    }

    private void setPicturesColors() {
        ivWC.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isWc())));
        ivWifi.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isWifi())));
        ivCoffee.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isCoffee())));
        ivGrocery.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isShop())));
        ivRestRoom.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isRestRoom())));
        ivCardPayment.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isCardPayment())));
        ivServiceStation.setColorFilter(getResources()
                .getColor(Utils.getServiceAvailableColor(features.isServiceStation())));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.btn_apply)
    public void applyServices() {
        listener.onServicesAdded(new Features(
                    switchRestRoom.isChecked(),
                    switchWifi.isChecked(),
                    switchWC.isChecked(),
                    switchCoffee.isChecked(),
                    switchGrocery.isChecked(),
                    switchCardPayment.isChecked(),
                    switchServiceStation.isChecked()));
        dismiss();
    }

    @OnClick(R.id.btn_close)
    public void close() {
        this.dismiss();
    }

    public void setFeatures(Features features) {
        this.features = features;
    }

    public void setListener(AddServicesDialogListener mListener) {
        this.listener = mListener;
    }
}
