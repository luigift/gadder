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

import java.util.Date;
import java.util.List;

public class Friend {

    private static final String TAG = "Friend";

    public static class Music {
        public Music() {}
        public String song;

        public Boolean playing;
    }
    public static class Sharing {

        public Sharing() {}
        public Boolean musicSharing;

        public Boolean weatherSharing;
        public Boolean batterySharing;
        public Boolean companySharing;
        public Boolean activitySharing;
        public Boolean locationSharing;
    }
    public static class Notifications {

        public Notifications() {}
        public Boolean friendsNearby;
        public Boolean requestActivity;
    }
    public static class Coordinates {

        public Coordinates() {}
        public Float latitude;
        public Float longitude;
    }

    public String id;
    public String name;
    public String phone;
    public String pictureUrl;

    public String time;
    public String city;
    public int battery;
    public int noFriends;
    public String activity;
    public int temperature;
    public Boolean headphone;
    public TimeZone timeZone;
    public String lastUpdate;
    public List<String> weather;

    public Music music = new Music();
    public Sharing sharing = new Sharing();
    public Coordinates coordinates = new Coordinates();
    public Notifications notification = new Notifications();

    public Bitmap image;
    public int position;
    public Marker marker;
    private LatLng latLng;
    private Location location;

    public void update(Friend updatedFriend) {

        Log.d(TAG, "ids: " + id + " " + updatedFriend.id);
        if (!this.id.equals(updatedFriend.id)) throw new AssertionError();

        this.name = updatedFriend.name;
        this.time = updatedFriend.time;
        this.city = updatedFriend.city;
        this.image = updatedFriend.image;
        this.marker = updatedFriend.marker;
        this.weather = updatedFriend.weather;
        this.battery = updatedFriend.battery;
        this.activity = updatedFriend.activity;
        this.timeZone = updatedFriend.timeZone;
        this.noFriends = updatedFriend.noFriends;
        this.headphone = updatedFriend.headphone;
        this.pictureUrl = updatedFriend.pictureUrl;
        this.lastUpdate = updatedFriend.lastUpdate;
        this.temperature = updatedFriend.temperature;

        this.music = updatedFriend.music;
        this.sharing = updatedFriend.sharing;
        this.coordinates = updatedFriend.coordinates;
        this.notification = updatedFriend.notification;

        if (this.marker != null) {
            this.marker.setPosition(updatedFriend.getLatLng());
        }
    }

    public LatLng getLatLng() {
        if (latLng == null) {
            latLng = new LatLng(coordinates.latitude,coordinates.longitude);
        }
        return latLng;
    }

    public Location getLocation() {
        if (coordinates.latitude == null || coordinates.longitude == null)
            throw new AssertionError();

        if (location == null) {
            location = new Location("");
            location.setLatitude(coordinates.latitude);
            location.setLongitude(coordinates.longitude);
        }

        return location;
    }

//    public Date getLastUpdate() {
//        return new SimpleDateFormat(Constants.DATE_FORMAT).parse(lastUpdate);
//    }

    public void downloadImage(final MainActivity activity) {
        if (activity == null) throw new AssertionError();
        Log.d(TAG, "downloadImage: " + name);
        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            Log.d(TAG, "downloadImage pictureUrl ok: " + name);

            final Target target = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    System.out.printf("IMAGE DOWNLOADED");
                    Log.v(TAG, "Friend image downloaded: " + name);
                    image = bitmap;
                    activity.adapter.notifyItemChanged(activity.friendsId.indexOf(id));
                    FriendsMapFragment fragment =
                            ((FriendsMapFragment) activity.getSupportFragmentManager()
                                    .findFragmentById(R.id.map));
                    if(fragment != null) {
                        Log.d(TAG, "Friend updated on map: " + name);
                        fragment.updateFriendOnMap(Friend.this);
                    } else {
                        Log.d(TAG, "FriendMapFragment null");
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d(TAG, "Error: " + errorDrawable.toString());
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };

            Picasso.with(activity)
                    .load(pictureUrl)
                    .placeholder(R.drawable.progress_animation)
                    .into(target);
        }
    }

}


