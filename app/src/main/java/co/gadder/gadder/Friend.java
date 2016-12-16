package co.gadder.gadder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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

    protected Bitmap image;
    protected String friendship;

    public String city;
    public Integer battery;
    public Integer noFriends;
    public Boolean headphone;
    public String lastUpdate;
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
        return sharing != null &&
                sharing.locationSharing != null &&
                sharing.locationSharing &&
                coordinates != null &&
                (coordinates.latitude != 0 &&
                        coordinates.longitude != 0);
    }

    protected Boolean hasPictureUrl() {
        return pictureUrl != null && !pictureUrl.isEmpty();
    }


    protected String getTimeLapse(Resources resources) {
        if (lastUpdate != null && !lastUpdate.isEmpty()) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(Constants.DATE_FORMAT);
                Date date = Calendar.getInstance().getTime();
                Date lastDate = sdf.parse(lastUpdate);

                long dif = Math.abs(date.getTime() - lastDate.getTime());
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long hoursInMilli = minutesInMilli * 60;
                long daysInMilli = hoursInMilli * 24;
                long monthsInMilli = daysInMilli * 30;

                long elapsedMonths = dif / monthsInMilli;
                dif = dif % monthsInMilli;

                long elapsedDays = dif / daysInMilli;
                dif = dif % daysInMilli;

                long elapsedHours = dif / hoursInMilli;
                dif = dif % hoursInMilli;

                long elapsedMinutes = dif / minutesInMilli;
                dif = dif % minutesInMilli;

                long elapsedSeconds = dif / secondsInMilli;

                String elapsedTime = "";
                if (elapsedMonths > 0) {
                    elapsedTime = elapsedMonths + " " + resources.getString(R.string.months);
                } else if (elapsedDays > 0) {
                    elapsedTime = elapsedDays + " " + resources.getString(R.string.days);
                } else if (elapsedHours > 0) {
                    elapsedTime = elapsedHours + " " + resources.getString(R.string.hours);
                } else if (elapsedMinutes > 0) {
                    elapsedTime = elapsedMinutes + " " + resources.getString(R.string.minutes);
                } else if (elapsedSeconds > 0) {
                    elapsedTime = elapsedSeconds + " " + resources.getString(R.string.seconds);
                }
                return elapsedTime;
            } catch (ParseException e) {
                FirebaseCrash.report(e);
            }
        }
        return "unknown";
    }

    @Override
    public String toString() {
        return "id: "+ id + "\nname: " + name;
    }
}


