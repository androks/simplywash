package androks.simplywash.Models;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androks.simplywash.Constants;
import androks.simplywash.Enums.OrderStatus;

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

    private OrderStatus status;
    private Calendar date;
    private Calendar orderDate;
    private String carType;
    private List<Service> services;
    private int price;
    private String userId;
    private String washerId;

    @Exclude
    public OrderStatus getStatusAsEnum(){
        return status;
    }

    public void setStatus(String status) {
        this.status = OrderStatus.valueOf(status);
    }

    public String getStatus() {
        return status.name();
    }



    @Exclude
    public void setDate(Calendar date){
        this.date = date;
    }

    @Exclude
    public Calendar getDateAsCalendar(){
        return date;
    }

    public void setDate(String date){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.ORDER_DATE_FORMAT, Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(date));
        }catch (ParseException e){
            calendar = Calendar.getInstance();
        }
        this.date = calendar;
    }

    public String getDate(){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.ORDER_DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }




    @Exclude
    public void setOrderDate(Calendar date){
        this.orderDate = date;
    }

    @Exclude
    public Calendar getOrderDateAsCalendar(){
        return orderDate;
    }

    public void setOrderDate(String date){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.ORDER_DATE_FORMAT, Locale.getDefault());
        try {
            calendar.setTime(sdf.parse(date));
        }catch (ParseException e){
            calendar = Calendar.getInstance();
        }
        this.orderDate = calendar;
    }

    public String getOrderDate(){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.ORDER_DATE_FORMAT, Locale.getDefault());
        return sdf.format(orderDate);
    }

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




    public String getCarType() {
        return carType;
    }

    public List<Service> getServices() {
        return services;
    }

    public int getPrice() {
        return price;
    }

    public String getUserId() {
        return userId;
    }

    public String getWasherId() {
        return washerId;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setWasherId(String washerId) {
        this.washerId = washerId;
    }
}
