package androks.simplywash.models;

import com.google.firebase.database.Exclude;

/**
 * Created by androks on 3/10/2017.
 */

public class Service {
    private String name;
    private int price;
    private boolean isSelected = false;

    Service(){}

    public Service(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public boolean isSelected() {
        return isSelected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Exclude
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Exclude
    public void toggle(){
        isSelected = !isSelected;
    }
}
