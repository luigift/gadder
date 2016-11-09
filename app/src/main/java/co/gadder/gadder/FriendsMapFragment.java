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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsMapFragment extends Fragment {

    private final static String TAG = "FriendsMapFragment";

    private GoogleMap mMap;
    private MapView mapView;

    private MainActivity activity;

    public FriendsMapFragment() {
        // Required empty public constructor
    }

    public static FriendsMapFragment newInstance() {
        return new FriendsMapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View layout = inflater.inflate(R.layout.fragment_friends_map, container, false);

        final View markerView = inflater.inflate(R.layout.marker_person, container, false);
        final CircleImageView markerImage = (CircleImageView) markerView.findViewById(R.id.markerImage);

        // Initialize MapView
        mapView = (MapView) layout.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                Log.d(TAG, "friendId: " + activity.friendsId.toString());

                for(String friendId : activity.friendsId) {
                    Friend friend = activity.friends.get(friendId);

                    markerImage.setImageBitmap(friend.image);

                    LatLng friendLocation = new LatLng(friend.latitude, friend.longitude);
                    mMap.addMarker(
                            new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(markerView)))
                                    .position(friendLocation)
                                    .title(friend.name)
                                    .snippet("battery: " + friend.battery+"%"));
                }
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
//                MapsInitializer.initialize(getActivity());

                // initialize TextViews
                CameraPosition cameraPosition = mMap.getCameraPosition();

//                // Updates the location and zoom of the MapView
//                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
//                mMap.animateCamera(cameraUpdate);
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fabFriendActivity);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getFragmentManager()
//                        .beginTransaction()
//                        .addToBackStack("friendMap")
//                        .replace(R.id.activity_main, FriendsActivityFragment.newInstance())
//                        .commit();
//            }
//        });

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

        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.mapRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new SmallFriendRecyclerAdapter(activity));
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(),
                        recyclerView,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Friend friend = activity.friends.get(activity.friendsId.get(position));
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(friend.getLatLng()));
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                getFragmentManager().beginTransaction()
                                        .addToBackStack("profile")
                                        .add(R.id.activity_main,
                                                FloatingProfileFragment.newInstance(activity.friends.get(activity.friendsId.get(position))))
                                        .commit();
                            }
                        }));
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
