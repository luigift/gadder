package co.gadder.gadder;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsMapFragment extends Fragment {

    private final static String TAG = "FriendsMapFragment";

    private GoogleMap mMap;
    private MapView mapView;
    private LatLngBounds.Builder mBoundsBuilder;

    private int bounderPadding = 350;

    View markerView;
    TextView marketName;
    CircleImageView markerImage;

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
        activity = (MainActivity) getActivity();
        mBoundsBuilder = LatLngBounds.builder();
    }

    public void updateFriendOnMap(final Friend friend) {
        if (mMap != null && mapView != null && markerImage != null) {
            if (friend.marker == null) {
                Random rnd = new Random();
                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                markerImage.setBorderWidth(5);
                markerImage.setBorderColor(color);
                marketName.setText(friend.name);
                friend.marker =
                        mMap.addMarker(
                                new MarkerOptions()
                                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)))
                                        .position(friend.getLatLng())
                                        .title(friend.name)
                                        .snippet("battery: " + friend.battery + "%"));
            }
            friend.marker.setTitle(friend.name);
            friend.marker.setSnippet("battery: " + friend.battery + "%");
            friend.marker.setPosition(friend.getLatLng());
            mBoundsBuilder.include(friend.getLatLng());
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_friends_map, container, false);

        markerView = inflater.inflate(R.layout.marker_name, container, false);
        marketName = (TextView) markerView.findViewById(R.id.markerName);
        markerImage = (CircleImageView) markerView.findViewById(R.id.markerImage);

        // Initialize MapView
        mapView = (MapView) layout.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
//        mapView.setAlpha((float) 0.5); // change map transparency
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
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(false);

                // initialize TextViews
                CameraPosition cameraPosition = mMap.getCameraPosition();

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Toast.makeText(activity, "Click", Toast.LENGTH_SHORT).show();
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        return false;
                    }
                });

            }
        });

        return layout;
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
                mMap.animateCamera(CameraUpdateFactory.newLatLng(f.getLatLng()));

                if (friendIterator == activity.friends.size()-1) {
                    friendIterator = 0;
                } else {
                    friendIterator += 1;
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Friend f = activity.getFriendByPosition(friendIterator);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(f.getLatLng()));

                if (friendIterator == 0) {
                    friendIterator = activity.friends.size()-1;
                } else {
                    friendIterator -= 1;
                }
            }
        });

        recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBoundsBuilder.build(), bounderPadding));
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
}
