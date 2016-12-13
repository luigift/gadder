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

    private int bounderPadding = 250;

    private Boolean friendFocused = false;

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
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        markers = new HashMap<>();
        activity = (MainActivity) getActivity();
        mBoundsBuilder = LatLngBounds.builder();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        ImageButton next = (ImageButton) getActivity().findViewById(R.id.nextFriendOnMap);
        ImageButton previous = (ImageButton) getActivity().findViewById(R.id.previousFriendOnMap);
        ImageButton recenter = (ImageButton) getActivity().findViewById(R.id.recenterFriendsOnMap);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friend f =  new ArrayList<>(activity.friends.values()).get(friendIterator);
                if (mMap != null) {
                    if (f.isSharingLocation()) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(f.getLatLng()));
                    }

                    if (friendIterator >= activity.friends.size() - 1) {
                        friendIterator = 0;
                    } else {
                        friendIterator += 1;
                    }
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {
                    Friend f =  new ArrayList<>(activity.friends.values()).get(friendIterator);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(f.getLatLng()));

                    if (friendIterator == 0) {
                        friendIterator = activity.friends.size() - 1;
                    } else {
                        friendIterator -= 1;
                    }
                }
            }
        });

        recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {
                    mBoundsBuilder = LatLngBounds.builder();
                    mBoundsBuilder.include(activity.user.getLatLng());
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
    }

    public void updateFriendOnMap(final Friend friend) {
        Log.d(TAG, "updateFriendOnMap: " + friend.name);
        if (mMap != null &&
                mapView != null &&
                marketName != null &&
                markerImage != null &&
                markerActivity != null &&
                friend.isSharingLocation()) {

            // create marker
            if (!markers.containsKey(friend.id) ) {
                Log.d(TAG, "Create Marker: " + friend.name);

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

                if (friend.pictureUrl != null && !friend.pictureUrl.isEmpty()) {
                    Log.d(TAG, "download pictureUrl: " + friend.pictureUrl + " " + friend.name);

                    markerImage.setBorderWidth(5);
                    markerImage.clearColorFilter();
                    markerImage.setBorderColor(color);

                    Picasso.with(getContext())
                            .load(friend.pictureUrl)
                            .resize(112, 112)
                            .centerCrop()
                            .onlyScaleDown()
                            .error(R.drawable.ic_face_black_24dp)
                            .placeholder(R.drawable.ic_face_black_24dp)
                            .into(markerImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                    addMarker(friend);
                                }

                                @Override
                                public void onError() {

                                }
                            });
                } else {
                    Log.d(TAG, "set drawable: " + friend.pictureUrl + " " + friend.name);
                    markerImage.setBorderWidth(0);
                    markerImage.setColorFilter(color);
                    markerImage.setImageResource(R.drawable.ic_face_black_24dp);
                    addMarker(friend);
                }
            } else {
                Log.d(TAG, "update marker: "+ friend.name);
                Marker marker = markers.get(friend.id);
                marker.setTag(friend.id);
                marker.setTitle(friend.name);
                marker.setPosition(friend.getLatLng());
                marker.setSnippet("battery: " + friend.battery + "%");
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));
    }

    public void focusFriend(Friend friend) {
        LatLng latLng = friend.getLatLng();
        if (latLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        // Display focused marker
        for (Marker m : markers.values()) {
            if (m != null) {
                m.setVisible(false);
            }
        }
        Marker marker = markers.get(friend.id);
        if (marker != null) {
            marker.setVisible(true);
        }

        displayFriendInfo(friend);

        friendFocused = true;
    }

    private void unfocusFriend() {
        Log.d(TAG, "unfocusFriend");

        if (friendFocused) {
            for (Marker m : markers.values()) {
                if (m != null) {
                    m.setVisible(true);
                }
            }
            infoLayout.removeAllViews();
        }
        friendFocused = false;
    }

    public void displayFriendInfo(Friend friend) {

        Log.d(TAG, "displayFriendInfo: " + friend.name);

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
        if (friend.sharing.musicSharing && friend.music.playing) {
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
            for(String w : friend.weather) {
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

        if (friend.sharing.activitySharing) {
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

            try {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
                Date date = Calendar.getInstance().getTime();
                Date lastDate = sdf.parse(friend.lastUpdate);

                long dif  = Math.abs(date.getTime() - lastDate.getTime());
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
                if (elapsedMonths > 0 ) {
                    elapsedTime = elapsedMonths + " " + getString(R.string.months);
                } else if (elapsedDays > 0 ) {
                    elapsedTime = elapsedDays + " " + getString(R.string.days);
                } else if (elapsedHours > 0 ) {
                    elapsedTime = elapsedHours + " " + getString(R.string.hours);
                } else if (elapsedMinutes > 0 ) {
                    elapsedTime = elapsedMinutes + " " + getString(R.string.minutes);
                } else if (elapsedSeconds > 0 ) {
                    elapsedTime = elapsedSeconds + " " + getString(R.string.seconds);
                }

                value.setText(elapsedTime);

                infoLayout.addView(info);
            } catch (ParseException e) {
                Log.e(TAG, "Couldn't parse date: " + e + " " + friend.name);
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

