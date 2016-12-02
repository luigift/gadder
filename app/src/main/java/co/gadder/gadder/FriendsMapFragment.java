package co.gadder.gadder;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import co.gadder.gadder.emoji.Activity;
import co.gadder.gadder.emoji.Nature;
import co.gadder.gadder.emoji.Objects;
import co.gadder.gadder.emoji.People;
import co.gadder.gadder.emoji.Symbols;
import co.gadder.gadder.emoji.Travel;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsMapFragment extends Fragment {

    private final static String TAG = "FriendsMapFragment";

    private final static float EMOJI_SIZE = 60f;

    private GoogleMap mMap;
    private MapView mapView;
    private LatLngBounds.Builder mBoundsBuilder;

    private int bounderPadding = 500;

    View markerView;
    TextView marketName;
    ImageView markerActivity;
    CircleImageView markerImage;

    Map<String, Marker> markers;

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

        markerView = inflater.inflate(R.layout.marker_name, container, false);

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

                Log.d(TAG, "friendId: " + activity.friendsId.toString());

                for (String friendId : activity.friendsId) {
                    Friend friend = activity.friends.get(friendId);

                    markerImage.setImageBitmap(friend.image);

                    LatLng friendLocation = friend.getLatLng();

                    mMap.addMarker(
                            new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)))
                                    .position(friendLocation)
                                    .title(friend.name)
                                    .snippet("battery: " + friend.battery + "%"));
                }

                if (PermissionManager.checkLocationPermission(getContext())) {
                    mMap.setMyLocationEnabled(false);
                }

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Toast.makeText(activity, "Click", Toast.LENGTH_SHORT).show();

                        return false;
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
                Friend f = activity.getFriendByPosition(friendIterator);
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(f.getLatLng()));

                    if (friendIterator == activity.friends.size() - 1) {
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
                    Friend f = activity.getFriendByPosition(friendIterator);
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

    public void updateFriendOnMap(final Friend friend) {
        Log.d(TAG, "updateFriendOnMap: " + friend.name);
        if (mMap != null && mapView != null && markerImage != null) {


            // create marker
            if (!markers.containsKey(friend.id)) {
                Log.d(TAG, "Create Marker: " + friend.name);
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                markerImage.setBorderWidth(5);
                markerImage.setBorderColor(color);
                markerImage.setImageBitmap(friend.image);
                marketName.setText(friend.name);
                markers.put(friend.id,
                        mMap.addMarker(
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)))
                                        .position(friend.getLatLng())
                                        .title(friend.name)
                                        .snippet("battery: " + friend.battery + "%")));
            }

            // update marker
            Marker marker = markers.get(friend.id);
            marker.setTitle(friend.name);
            marker.setSnippet("battery: " + friend.battery + "%");
            marker.setPosition(friend.getLatLng());

            if (friend.image == null) {
                Log.d(TAG, "null image " + friend.name);
            } else {
                Log.d(TAG, "image ok " + friend.name);
                markerImage.setImageBitmap(friend.image);
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)));
            }

            mBoundsBuilder.include(friend.getLatLng());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));

        }
    }

    public void focusFriend(Friend friend) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(friend.getLatLng()));
        for(Marker m : markers.values()) {
            m.setVisible(false);
        }
        markers.get(friend.id).setVisible(true);

        displayFriendInfo(friend);
    }

    public void displayFriendInfo(Friend friend) {
        LinearLayout infoLayout = (LinearLayout) getActivity().findViewById(R.id.mapUserInfoLayout);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        if (friend.sharing.musicSharing && friend.music.playing || true) {
            View info = inflater.inflate(R.layout.item_info, null);
//            info.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
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
                            }, 3000);
                }
            });

            infoLayout.addView(info);
        }

        if (friend.sharing.batterySharing) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
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
                            }, 3000);
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
                            }, 3000);
                }
            });


            infoLayout.addView(info);
        }

        // Share temperature
        if (friend.sharing.weatherSharing) {

            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
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
                            }, 3000);
                }
            });

            infoLayout.addView(info);
        }

        if (friend.sharing.locationSharing) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
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
                            }, 3000);
                }
            });


            infoLayout.addView(info);
        }

        if (friend.sharing.activitySharing) {
            // Inflate View
            View info = inflater.inflate(R.layout.item_info, null);
//            info.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ImageView image = (ImageView) info.findViewById(R.id.infoImage);
            final TextView description = (TextView) info.findViewById(R.id.infoText);
            TextView value = (TextView) info.findViewById(R.id.infoValue);

            // Set values
            image.setImageBitmap(Constants.textAsBitmap(Nature.EARTH_AMERICA, EMOJI_SIZE, Color.WHITE));
            description.setText(getString(R.string.activity));
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
                            }, 3000);
                }
            });
            infoLayout.addView(info);

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

