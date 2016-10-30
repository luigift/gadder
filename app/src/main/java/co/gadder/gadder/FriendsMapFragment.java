package co.gadder.gadder;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;

public class FriendsMapFragment extends Fragment {

    private final static String TAG = "FriendsMapFragment";

    private GoogleMap mMap;
    private MapView mapView;

    private MainActivity mainActivity;

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
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_friends_map, container, false);

        // Initialize MapView
        mapView = (MapView) layout.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                Log.d(TAG, "friendId: " + mainActivity.friendsId.toString());
                for(String friendId : mainActivity.friendsId) {
                    Friend friend = mainActivity.friends.get(friendId);

                    LatLng friendLocation = new LatLng(friend.latitude, friend.longitude);
                    mMap.addMarker(
                            new MarkerOptions()
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

        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fabFriendActivity);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("friendMap")
                        .replace(R.id.activity_main, FriendsActivityFragment.newInstance())
                        .commit();
            }
        });

        return layout;
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
