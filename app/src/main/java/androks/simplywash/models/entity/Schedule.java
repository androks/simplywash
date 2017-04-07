package androks.simplywash.models.entity;

import com.google.firebase.database.Exclude;

import java.util.Calendar;

public class Schedule {
    private String weekdays;
    private String sunday;
    private String saturday;

    public Schedule(){
        weekdays = "";
        sunday = "";
        saturday = "";
    }

    @Exclude
    public String getScheduleForToday(){
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        if(dayOfWeek == Calendar.SUNDAY)
            return sunday;
        else if(dayOfWeek == Calendar.SATURDAY)
            return saturday;
        else
            return weekdays;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays;
    }

    public String getSunday() {
        return sunday;
    }

    public void setSunday(String sunday) {
        this.sunday = sunday;
    }

    public String getSaturday() {
        return saturday;
    }

    public void setSaturday(String saturday) {
        this.saturday = saturday;
    }
}
