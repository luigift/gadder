package co.gadder.gadder;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.crash.FirebaseCrash;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import co.gadder.gadder.emoji.Activity;
import co.gadder.gadder.emoji.Nature;
import co.gadder.gadder.emoji.Objects;
import co.gadder.gadder.emoji.People;
import co.gadder.gadder.emoji.Symbols;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.gadder.gadder.Constants.EMOJI_SIZE;

public class FriendsMapFragment extends Fragment {

    private final static String TAG = "FriendsMapFragment";

    private final static int DISPLAY_TIME = 2000;

    private GoogleMap mMap;
    private MapView mapView;
    private LatLngBounds.Builder mBoundsBuilder;

    private int bounderPadding = 150;

    private String friendFocused;

    View markerView;
    TextView marketName;
    ImageView markerActivity;
    CircleImageView markerImage;

    Map<String, Marker> markers;

    LinearLayout infoLayout;

    int friendIterator = 0;

    private MainActivity activity;

    public FriendsMapFragment() {
    }

    public static FriendsMapFragment newInstance() {
        return new FriendsMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
        activity = (MainActivity) getActivity();
        mBoundsBuilder = LatLngBounds.builder();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_friends_map, container, false);

        infoLayout = (LinearLayout) layout.findViewById(R.id.mapUserInfoLayout);

        markerView = inflater.inflate(R.layout.marker_person, container, false);

        marketName = (TextView) markerView.findViewById(R.id.markerName);
        markerActivity = (ImageView) markerView.findViewById(R.id.marketActivity);
        markerImage = (CircleImageView) markerView.findViewById(R.id.markerImage);

        // Initialize MapView
        mapView = (MapView) layout.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        unfocusFriend();
                    }
                });

                if (PermissionManager.checkLocationPermission(getContext())) {
                    mMap.setMyLocationEnabled(false);
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String uid = (String) marker.getTag();
                        Friend friend = activity.friends.get(uid);
                        focusFriend(friend);
                        return true;
                    }
                });
            }
        });

        return layout;
    }

    private Boolean checkSomeoneSharing() {
        ArrayList<Friend> array = new ArrayList<>(activity.friends.values());
        for(Friend f : array) {
            if (f.isSharingLocation() && f.friendship.equals(Friend.FRIEND))
                return true;
        }
        return false;
    }

    private void iterateThroughFriends() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "iterateThroughFriends");
        if (mMap != null && activity.friends.size() > 0) {
            Friend f = new ArrayList<>(activity.friends.values()).get(friendIterator);
            if (f != null) {

                if (friendIterator >= activity.friends.size() - 1) {
                    friendIterator = 0;
                } else {
                    friendIterator += 1;
                }

                if (f.isSharingLocation() && f.friendship.equals(Friend.FRIEND)) {
                    focusFriend(f);
                } else {
                    if (checkSomeoneSharing()) {
                        iterateThroughFriends();
                    }
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        ImageButton next = (ImageButton) getActivity().findViewById(R.id.nextFriendOnMap);
        ImageButton recenter = (ImageButton) getActivity().findViewById(R.id.recenterFriendsOnMap);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iterateThroughFriends();
            }
        });

        recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {
                    mBoundsBuilder = LatLngBounds.builder();

                    if (activity.user != null) {
                        mBoundsBuilder.include(activity.user.getLatLng());
                    }

                    for (Marker marker : markers.values()) {
                        mBoundsBuilder.include(marker.getPosition());
                    }

                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public void clearFriendInfo(){
        infoLayout.removeAllViews();
        friendFocused = null;
    }

    public void updateFriendOnMap(final Friend friend) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "updateFriendOnMap: " + friend.name);
        if (mMap != null &&
                mapView != null &&
                marketName != null &&
                markerImage != null &&
                markerActivity != null &&
                friend.isSharingLocation()) {

            // create marker
            if (!markers.containsKey(friend.id) ) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "Create Marker: " + friend.name);

                // set emoji
                if (friend.activity.type != null && !friend.activity.type.isEmpty()) {
                    GadderActivities.GadderActivity act = GadderActivities.ACTIVITY_MAP.get(friend.activity.type);
                    if (act != null) {
                        String emoji = act.emoji;
                        markerActivity.setImageBitmap(Constants.textAsBitmap(emoji, EMOJI_SIZE, Color.WHITE));
                    }
                } else {
                    markerActivity.setImageBitmap(null);
                }

                // Set image
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                marketName.setText(friend.name);

                if (friend.hasPictureUrl()) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "download pictureUrl: " + friend.pictureUrl + " " + friend.name);

                    markerImage.setBorderWidth(5);
                    markerImage.clearColorFilter();
                    markerImage.setBorderColor(color);

                    final Callback callback =  new Callback() {
                        @Override
                        public void onSuccess() {
                            FirebaseCrash.logcat(Log.DEBUG, TAG, "Picture downloaded: " + friend.name);

                            addMarker(friend);
                        }

                        @Override
                        public void onError() {
                            FirebaseCrash.logcat(Log.DEBUG, TAG, "Error downloading picture");
                        }
                    };

                    Picasso.with(getContext())
                            .load(friend.pictureUrl)
                            .resize(85, 85)
                            .centerCrop()
                            .onlyScaleDown()
                            .error(R.drawable.ic_face_black_24dp)
                            .placeholder(R.drawable.ic_face_black_24dp)
                            .into(markerImage, callback);

                } else {

                    FirebaseCrash.logcat(Log.DEBUG, TAG, "set drawable: " + friend.pictureUrl + " " + friend.name);
                    markerImage.setBorderWidth(0);
                    markerImage.setColorFilter(color);
                    markerImage.setImageResource(R.drawable.ic_face_black_24dp);
                    addMarker(friend);
                }
            } else {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "update marker: "+ friend.name);
                Marker marker = markers.get(friend.id);
                marker.setTag(friend.id);
                marker.setTitle(friend.name);
                marker.setPosition(friend.getLatLng());
                marker.setSnippet("battery: " + friend.battery + "%");
            }

            if (friendFocused != null && friendFocused.equals(friend.id)){
                clearFriendInfo();
                focusFriend(friend);
            }
        }
    }

    public void addMarker(Friend friend){
        Marker marker = mMap.addMarker(
                new MarkerOptions()
                        .icon(BitmapDescriptorFactory
                                .fromBitmap(getMarkerBitmapFromView(markerView)))
                        .position(friend.getLatLng())
                        .title(friend.name)
                        .snippet("battery: " + friend.battery + "%"));
        marker.setTag(friend.id);
        markers.put(friend.id, marker);

        mBoundsBuilder.include(friend.getLatLng());
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));
    }

    public void focusFriend(Friend friend) {
        if (friendFocused == null || !friendFocused.equals(friend.id)) {
            LatLng latLng = friend.getLatLng();
            if (latLng != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }

            // Display hide other markers
            for (Marker m : markers.values()) {
                if (m != null) {
                    m.setVisible(false);
                }
            }

            // Display selected marker
            Marker marker = markers.get(friend.id);
            if (marker != null) {
                marker.setVisible(true);
            }

            displayFriendInfo(friend);

            friendFocused = friend.id;
        }
    }

    private void unfocusFriend() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "unfocusFriend");

        if (friendFocused != null) {
            for (Marker m : markers.values()) {
                if (m != null) {
                    m.setVisible(true);
                }
            }
            clearFriendInfo();
        }
    }

    public void displayFriendInfo(Friend friend) {

        FirebaseCrash.logcat(Log.DEBUG, TAG, "displayFriendInfo: " + friend.name);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setMargins(20,20,20,20);


        // Set name
        if (friend.name != null && !friend.name.isEmpty()) {
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            final TextView value = (TextView) info.findViewById(R.id.infoValue);

            image.setImageBitmap(Constants.textAsBitmap(People.WINKING_FACE, EMOJI_SIZE, Color.BLACK));
            description.setText(getString(R.string.name));
            value.setText(friend.name);
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });

            infoLayout.addView(info);
        }

        // Set music
        if (friend.sharing.musicSharing && friend.music.playing && friend.displayMusic()) {
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            if (friend.headphone) {
                image.setImageBitmap(Constants.textAsBitmap(Activity.HEADPHONE, EMOJI_SIZE, Color.BLACK));
            } else {
                image.setImageBitmap(Constants.textAsBitmap(Symbols.MUSICAL_NOTE, EMOJI_SIZE, Color.WHITE));
            }
            description.setText(getString(R.string.music));
            value.setText(friend.music.song);
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });

            infoLayout.addView(info);
        }

        if (friend.sharing.batterySharing) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            // Set values
            image.setImageBitmap(Constants.textAsBitmap(Objects.BATTERY, EMOJI_SIZE, Color.WHITE));
            description.setText(getString(R.string.battery));
            String batteryValue = friend.battery + "%";
            value.setText(batteryValue);
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });

            infoLayout.addView(info);
        }

        if (friend.sharing.companySharing) {
            // TODO
        }

        // Share weather
        if (friend.sharing.weatherSharing) {

            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            String weather = "";
            String weatherText = "";
            for (String w : friend.weather) {
                switch (w) {
                    case Constants.CLEAR:
                        weatherText += getString(R.string.clear) + " ";
                        weather += Nature.SUN_FACE + " ";
                        break;
                    case Constants.FOGGY:
                        weatherText += getString(R.string.foggy) + " ";
                        break;
                    case Constants.CLOUDY:
                        weatherText += getString(R.string.cloudy) + " ";
                        weather += Nature.SUN_BEHIND_CLOUD + " ";
                        break;
                    case Constants.HAZY:
                        weatherText += getString(R.string.hazy) + " ";
                        weather += Nature.CLOUD + " ";
                        break;
                    case Constants.ICY:
                        weatherText += getString(R.string.icy) + " ";
                        weather += Nature.CLOUD_WITH_SNOW + " ";
                        break;
                    case Constants.RAINY:
                        weatherText += getString(R.string.rainy) + " ";
                        weather += Nature.UMBRELLA_WITH_RAIN_DROPS + " ";
                        break;
                    case Constants.SNOWY:
                        weatherText += getString(R.string.snowy) + " ";
                        weather += Nature.CLOUD_WITH_SNOW + " ";
                        break;
                    case Constants.STORMY:
                        weatherText += getString(R.string.stormy) + " ";
                        weather += Nature.CLOUD_LIGHTENING + " ";
                        break;
                    case Constants.WINDY:
                        weatherText += getString(R.string.windy) + " ";
                        weather += Nature.LEAF_WIND + " ";
                        break;
                    case Constants.UNKNOWN:
                        weatherText += getString(R.string.unknown) + " ";
                        weather += Symbols.WHITE_QUESTION_MARK_ORNAMENT + " ";
                        break;
                }
            }

            // Set values
            if (!weather.isEmpty()) {
                image.setImageBitmap(Constants.textAsBitmap(weather, EMOJI_SIZE, Color.WHITE));
                description.setText(getString(R.string.weather));

                value.setText(weatherText);
                description.setVisibility(View.GONE);
                info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        description.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        description.setVisibility(View.GONE);
                                    }
                                }, DISPLAY_TIME);
                    }
                });

                infoLayout.addView(info);
            }
        }
        
        // Share temperature
        if (friend.sharing.weatherSharing) {

            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            // Set values
            image.setImageBitmap(Constants.textAsBitmap(Objects.THERMOMETER, EMOJI_SIZE, Color.WHITE));
            description.setText(getString(R.string.temperature));
            String temperature = friend.temperature + getString(R.string.degrees);
            // TODO fahrenheit conversion
            value.setText(temperature);
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });

            infoLayout.addView(info);
        }

        if (friend.sharing.locationSharing) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            // Set values
            image.setImageBitmap(Constants.textAsBitmap(Nature.EARTH_AMERICA, EMOJI_SIZE, Color.WHITE));
            description.setText(getString(R.string.city));
            value.setText(friend.city);
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });


            infoLayout.addView(info);
        }

        if (friend.sharing.activitySharing && friend.displayActivity()) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            GadderActivities.GadderActivity activity = GadderActivities.ACTIVITY_MAP.get(friend.activity.type);

            // Set values
            if (activity != null && activity.emoji != null && !activity.emoji.isEmpty()) {
                image.setImageBitmap(Constants.textAsBitmap(activity.emoji, EMOJI_SIZE, Color.WHITE));
                description.setText(activity.description);
                value.setText(friend.activity.description);
                description.setVisibility(View.GONE);
                info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        description.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        description.setVisibility(View.GONE);
                                    }
                                }, DISPLAY_TIME);
                    }
                });
                infoLayout.addView(info);
            }
        }

        // Last update
        if (friend.lastUpdate != null && !friend.lastUpdate.isEmpty()) {

            View info = inflater.inflate(R.layout.item_info, null);
            info.setLayoutParams(layout);
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            // Set values
            image.setImageBitmap(Constants.textAsBitmap(Objects.TIMER_CLOCK, EMOJI_SIZE, Color.WHITE));
            description.setText(getString(R.string.last_update));
            description.setVisibility(View.GONE);
            info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    description.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    description.setVisibility(View.GONE);
                                }
                            }, DISPLAY_TIME);
                }
            });
            String timeLapse = friend.getTimeLapse(getResources());

            if (timeLapse != null) {
                value.setText(timeLapse);
                infoLayout.addView(info);
            }
        }
    }

    private Bitmap getMarkerBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

}

