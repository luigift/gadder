package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Friend {

    public String id;
    public String name;
    public String city;
    public String music;
    public String phone;
    public int position;
    public int noFriends;
    public int battery;
    public String Weather;
    public Bitmap image;
    public float latitude;
    public float longitude;
    public String activity;
    public Boolean headphone;
    public float temperature;
    public String pictureUrl;

    private LatLng mLatLng;
    private Location mLocation;

    public LatLng getLatLng() {
        if (mLatLng == null) {
            mLatLng = new LatLng(latitude,longitude);
        }
        return mLatLng;
    }

    public Location getLocation() {
        if (mLocation == null) {
            mLocation = new Location("");
            mLocation.setLatitude(latitude);
            mLocation.setLongitude(longitude);
        }

        return mLocation;
    }

}


