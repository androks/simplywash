package androks.simplywash.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by androks on 1/22/2017.
 */

public class Review {
    public String name;
    public String text;
    public String date;
    public float rating;

    Review(){}

    public Review(String name, String text, float rating) {
        this.name = name;
        this.text = text;
        this.rating = rating;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        date = format.format(calendar.getTime());
    }
}
