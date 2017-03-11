package androks.simplywash.Models;

import com.google.firebase.database.Exclude;

/**
 * Created by androks on 3/11/2017.
 */

public class Order {

    public interface OnOrderApplyListener{
        void onOrderApply(Order order);
    }

    @Exclude
    private static Order mInstance;
    @Exclude
    private OnOrderApplyListener mListener;

    public OrderStatus status;
    private void setListener(OnOrderApplyListener listener){
        mListener = listener;
    }

    public static Order getInstance() {
        if(mInstance == null) {
            mInstance = new Order();
        }
        return mInstance;
    }

    public void createOrder(){
        mListener.onOrderApply(Order.getInstance());
    }

    public Order(){}

}
