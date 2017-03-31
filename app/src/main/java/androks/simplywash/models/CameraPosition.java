package androks.simplywash.models;

/**
 * Created by androks on 4/1/2017.
 */

public class CameraPosition {
    private double latitude;
    private double longitude;
    private int zoom;

    public CameraPosition() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }
}
