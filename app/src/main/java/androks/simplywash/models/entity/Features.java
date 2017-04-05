package androks.simplywash.models.entity;

import androks.simplywash.R;

/**
 * Created by androks on 4/5/2017.
 */

public class Features {
    private boolean restRoom;
    private boolean wifi;
    private boolean wc;
    private boolean coffee;
    private boolean shop;
    private boolean cardPayment;
    private boolean serviceStation;


    public Features() {
    }

    public Features(boolean restRoom, boolean wifi, boolean wc, boolean coffee, boolean shop,
                    boolean cardPayment, boolean serviceStation) {
        this.restRoom = restRoom;
        this.wifi = wifi;
        this.wc = wc;
        this.coffee = coffee;
        this.shop = shop;
        this.cardPayment = cardPayment;
        this.serviceStation = serviceStation;
    }

    public boolean isRestRoom() {
        return restRoom;
    }

    public void setRestRoom(boolean restRoom) {
        this.restRoom = restRoom;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isWc() {
        return wc;
    }

    public void setWc(boolean wc) {
        this.wc = wc;
    }

    public boolean isCoffee() {
        return coffee;
    }

    public void setCoffee(boolean coffee) {
        this.coffee = coffee;
    }

    public boolean isShop() {
        return shop;
    }

    public void setShop(boolean shop) {
        this.shop = shop;
    }

    public boolean isCardPayment() {
        return cardPayment;
    }

    public void setCardPayment(boolean cardPayment) {
        this.cardPayment = cardPayment;
    }

    public boolean isServiceStation() {
        return serviceStation;
    }

    public void setServiceStation(boolean serviceStation) {
        this.serviceStation = serviceStation;
    }

    @Override
    public String toString() {
        String temp = "";
        if(wc)
            temp += R.string.wc + ", ";
        if(restRoom)
            temp += R.string.rest_room + ", ";
        if(wifi)
            temp += R.string.wifi + ", ";
        if(coffee)
            temp += R.string.coffee + ", ";
        if(shop)
            temp += R.string.shop + ", ";
        if(cardPayment)
            temp += R.string.card_payment + ", ";
        if(serviceStation)
            temp += R.string.service_station;

        return temp;
    }
}
