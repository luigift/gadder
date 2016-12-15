package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Friend {

    private static final String TAG = "Friend";

    public static final String FRIEND = "friend";
    public static final String CONTACT = "contact";

    public static class Music {
        public Music() {
            song = "";
            playing = false;
        }
        public String song;
        public Boolean playing;
    }

    public static class Activity {
        public Activity() {
            time = "";
            type = "";
            description = "";
            location = new Coordinates();
        }

        public String time;
        public String type;
        public String description;
        public Coordinates location;
    }

    public static class Sharing {
        public Sharing() {
            musicSharing = false;
            weatherSharing = false;
            batterySharing = false;
            companySharing = false;
            activitySharing = false;
            locationSharing = false;
        }

        public Boolean musicSharing;
        public Boolean weatherSharing;
        public Boolean batterySharing;
        public Boolean companySharing;
        public Boolean activitySharing;
        public Boolean locationSharing;
    }

    public static class Notifications {
        public Notifications() {
            nearbyNotification = false;
            requestNotification = false;
        }

        public Boolean nearbyNotification;
        public Boolean requestNotification;
    }

    public static class Coordinates {

        public Coordinates() {
            latitude = 0f;
            longitude = 0f;
        }

        public Float latitude;
        public Float longitude;
    }

    public String id;
    public String name;
    public String phone;
    public String pictureUrl;

    public Bitmap image;

    public String friendship;

    public String city;
    public Integer battery;
    public String lastUpdate;
    public Boolean headphone;
    public Integer temperature;
    public List<String> weather = new ArrayList<>();

    public Music music = new Music();
    public Sharing sharing = new Sharing();
    public Activity activity = new Activity();
    public Coordinates coordinates = new Coordinates();
    public Notifications notification = new Notifications();

    protected LatLng getLatLng() {
        return new LatLng(coordinates.latitude,coordinates.longitude);
    }

    protected Location getLocation() {
        if (coordinates.latitude == null || coordinates.longitude == null)
            throw new AssertionError();

        Location location = new Location("");
        location.setLatitude(coordinates.latitude);
        location.setLongitude(coordinates.longitude);

        return location;
    }

//    public Date getLastUpdate() {
//        return new SimpleDateFormat(Constants.DATE_FORMAT).parse(lastUpdate);
//    }

    protected Boolean isSharingLocation() {
        return sharing != null && sharing.locationSharing != null && sharing.locationSharing;
    }

    protected Boolean hasPictureUrl() {
        return pictureUrl != null && !pictureUrl.isEmpty();
    }

    @Override
    public String toString() {
        return "id: "+ id + "\nname: " + name;
    }
}


