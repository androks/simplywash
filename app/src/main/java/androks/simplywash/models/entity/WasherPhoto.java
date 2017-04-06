package androks.simplywash.models.entity;

import com.google.firebase.database.Exclude;

import androks.simplywash.enums.PhotoType;

/**
 * Created by androks on 4/6/2017.
 */

public class WasherPhoto {
    private String url;
    private PhotoType type;

    public WasherPhoto() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Exclude
    public PhotoType getTypeAsEnum() {
        return type;
    }

    @Exclude
    public void setType(PhotoType type) {
        this.type = type;
    }

    public String getType(){
        return type.name();
    }

    public void setType(String type){
        this.type = PhotoType.valueOf(type);
    }
}
