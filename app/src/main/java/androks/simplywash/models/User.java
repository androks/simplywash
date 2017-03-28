package androks.simplywash.models;

/**
 * Created by androks on 12/10/2016.
 */

public class User {
    private String phone;

    public User(String phone) {
        this.phone = phone;
    }

    public User() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
