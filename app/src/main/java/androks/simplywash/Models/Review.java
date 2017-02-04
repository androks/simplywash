package androks.simplywash.Models;

/**
 * Created by androks on 1/22/2017.
 */

public class Review {
    private String id;
    private String uid;
    private String washerId;
    private String text;
    private String date;
    private String email;
    private float rating;

    Review(){}

    public Review(String id, String uid, String washerId, String text, String date, String email, float rating) {
        this.id = id;
        this.uid = uid;
        this.washerId = washerId;
        this.text = text;
        this.date = date;
        this.email = email;
        this.rating = rating;
    }

    public Review(String text, float rating) {
        this.text = text;
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getWasherId() {
        return washerId;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setWasherId(String washerId) {
        this.washerId = washerId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
