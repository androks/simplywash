package androks.simplywash.Models;

/**
 * Created by androks on 2/5/2017.
 */

public class Price {
    private int contact;
    private int noneContact;
    private int wax;

    Price(){}

    public int getContact() {
        return contact;
    }

    public int getNoneContact() {
        return noneContact;
    }

    public int getWax() {
        return wax;
    }

    public Price(int contact, int noneContact, int wax) {
        this.contact = contact;
        this.noneContact = noneContact;
        this.wax = wax;
    }
}
