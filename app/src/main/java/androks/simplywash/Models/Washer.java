package androks.simplywash.Models;

/**
 * Created by androks on 11/17/2016.
 */

public class Washer {
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String location;
    private String description;
    private String placeId;
    private String state;
    private double langtitude;
    private double longtitude;
    private float rating;
    private int boxes;
    private int availableBoxes;
    private int votesCount;
    private int workHoursFrom;
    private int workHoursTo;
    private boolean restRoom;
    private boolean wifi;
    private boolean toilet;
    private boolean coeffee;
    private boolean shop;
    private boolean cardPayment;
    private boolean serviceStation;

    public Washer() {}

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getState() {
        return state;
    }

    public double getLangtitude() {
        return langtitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public float getRating() {
        return rating;
    }

    public int getBoxes() {
        return boxes;
    }

    public int getAvailableBoxes() {
        return availableBoxes;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public int getWorkHoursFrom() {
        return workHoursFrom;
    }

    public int getWorkHoursTo() {
        return workHoursTo;
    }

    public boolean isRestRoom() {
        return restRoom;
    }

    public boolean isWifi() {
        return wifi;
    }

    public boolean isToilet() {
        return toilet;
    }

    public boolean isCoeffee() {
        return coeffee;
    }

    public boolean isShop() {
        return shop;
    }

    public boolean isCardPayment() {
        return cardPayment;
    }

    public boolean isServiceStation() {
        return serviceStation;
    }
}
