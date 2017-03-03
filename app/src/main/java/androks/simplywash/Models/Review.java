package androks.simplywash.Models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by androks on 1/22/2017.
 */

public class Review {
    private String name;
    private String text;
    private String date;
    private float rating;

    Review(){}

    public Review(String name, String text, float rating) {
        this.name = name;
        this.text = text;
        this.rating = rating;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        date = format.format(calendar.getTime());
    }

    public String getName() {
        return name;
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

    public void setName(String name) {
        this.name = name;
    }
}
