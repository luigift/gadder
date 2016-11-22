package co.gadder.gadder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Main Activity";
    private static final String SENDER_ID = "254610893779";
    private static final String SERVER_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String API_KEY = "AIzaSyDHV8lCJNf8VUIOx2L8KCI8zDYhlXinB70";

    public static final int REQUEST_LOCATION_PERMISSION = 10;
    public static final int REQUEST_READ_CONTACTS_PERMISSION = 11;

    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent = null;

    private RequestQueue requestQueue;

    public FirebaseAuth mAuth;
    public StorageReference mStorage;
    public DatabaseReference mDatabase;
    public FirebaseAuth.AuthStateListener mAuthListener;

    protected List<Friend> contacts;
    protected List<String> friendsId;
    protected Map<String, Friend> friends;

    protected FriendsRecyclerAdapter adapter;

    protected String loginState = null;
    protected Boolean friendsDownloaded = false;

    private static final int NUM_PAGES = 2;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    public String uid;

    public Boolean appBarExpanded;
    private AppBarLayout appBarLayout;

    protected final Location mLocation = new Location("");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocation.setLatitude(-23.6233303);
        mLocation.setLongitude(-46.6732878);

        friends = new HashMap<>();
        contacts = new ArrayList<>();
        friendsId = new ArrayList<>();
        adapter = new FriendsRecyclerAdapter(MainActivity.this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "AuthListener: " + loginState);
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    getFriends(user);
                    getFriendsFromContacts();
                    Log.d(TAG, "Logged in");
                    if (checkPermissions()) {
                        if (savedInstanceState == null && (loginState == null || !loginState.equals("friends"))) {
                            Log.d(TAG, "FriendsActivityFragment");
                            mPager = (ViewPager) findViewById(R.id.mainPager);
                            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                            mPager.setAdapter(mPagerAdapter);
                            mPager.setCurrentItem(1);

                            final ImageView profile = (ImageView) findViewById(R.id.goToProfile);
                            profile.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (appBarExpanded) {
                                        appBarLayout.setExpanded(false, true);
                                    }
                                    mPager.setCurrentItem(0, true);
                                }
                            });

                            final ImageView main = (ImageView) findViewById(R.id.goToMainScreen);
                            main.setColorFilter(Color.argb(255,33,34,89));
                            main.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (appBarExpanded) {
                                        appBarLayout.setExpanded(false, true);
                                    }
                                    mPager.setCurrentItem(1, true);
                                }
                            });

                            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                @Override
                                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                }

                                @Override
                                public void onPageSelected(int position) {
                                    if (position == 0 ) {
                                        profile.setColorFilter(Color.argb(255,33,34,89));
                                        main.clearColorFilter();
                                    } else if (position == 1) {
                                        profile.clearColorFilter();
                                        main.setColorFilter(Color.argb(255,33,34,89));
                                    } else {

                                    }
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });

                            startService(new Intent(MainActivity.this, UserActivityService.class));

                            loginState = "friends";
                        }
                    } else {
//                        if (savedInstanceState == null && (loginState == null || !loginState.equals("permissions"))) {
//                            Log.d(TAG, "FriendsActivityFragment");
//                            getSupportFragmentManager().beginTransaction()
//                                    .replace(R.id.activity_main, PhoneLoginFragment.newInstance())
//                                    .commit();
//                            loginState = "permissions";
//                        }
                    }
                } else {
                    Log.d(TAG, "Logged out");
                    if (null == savedInstanceState && (loginState == null || !loginState.equals("login"))) {
                        Log.d(TAG, "LoginFragment");
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.activity_main, LoginFragment.newInstance())
                                .commit();
                        loginState = "login";
                    }
                }
            }
        };


        mStorage = FirebaseStorage.getInstance().getReference();

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(false);
        appBarExpanded = false;
        if (appBarLayout.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }
        final Integer[] initialOffset = new Integer[]{null};
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (initialOffset[0] == null) {
                    initialOffset[0] = verticalOffset;
                } else if (verticalOffset != initialOffset[0]) {
                    appBarExpanded = true;
                } else {
                    appBarExpanded = false;
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activityFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack("inputFragment")
                        .add(R.id.activity_main, InputFragment.newInstance())
                        .commit();
            }
        });

        final FloatingActionButton privacyFab = (FloatingActionButton) findViewById(R.id.privacyFab);
        privacyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (privacyFab.)
//                privacyFab.setImageResource(R.drawable.ic_lock_open_white_24dp);
                startService(new Intent(MainActivity.this, UserActivityService.class));
                Toast.makeText(MainActivity.this, "Update user", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue = Volley.newRequestQueue(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void sendUpdateRequest() {
        String t = FirebaseInstanceId.getInstance().getToken();

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            data.put("update", "true");
            json.put("to", t);
            json.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SERVER_URL, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error: " + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "key=" + API_KEY);
                return header;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        Log.d(TAG, "body: " + request.getBody().toString());
        Log.d(TAG, "content: " + request.getBodyContentType());
        try {
            Log.d(TAG, "headers: " + request.getHeaders().toString());
        } catch (AuthFailureError authFailureError) {
            Log.d(TAG, "AUTH ERROR");
            authFailureError.printStackTrace();
        }
        Log.d(TAG, request.toString());
        requestQueue.add(request);

        Toast.makeText(MainActivity.this, "Token: " + t, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Token: " + t);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
//        unregisterReceiver(mReceiver);
    }

    public Friend getFriendByPosition(int position) {
        return friends.get(friendsId.get(position));
    }

    private void getFriendsFromContacts() {
        Log.d(TAG, "getFriendsFromContacts");
        if (checkContactsPermission()) {
            Log.d(TAG, "got Contacts permission");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    while (phones.moveToNext()) {
                        final Friend friend = new Friend();
                        friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        friend.phone = rawPhone.replaceAll("\\D+", ""); // remove non number characters

                        mDatabase.child("user_phone").child(friend.phone).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d(TAG, "name" + friend.name + " phone: " + friend.phone);
                                    friend.id = dataSnapshot.getValue(String.class);
                                    if (!friends.containsKey(friend.id)) { // not a friend
                                        Log.d(TAG, friend.name + " is not a friend " + friend.id + friends.containsKey(friend.id));
                                        int index = getContactById(friend.id);
                                        if (index < 0) { // haven't been displayed
                                            contacts.add(friend);
                                            adapter.notifyItemChanged(friends.size() + contacts.size());

                                            mDatabase.child("users").child(friend.id).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Log.d(TAG, "contact info downloaded");
                                                    int index = getContactById(dataSnapshot.getKey());
                                                    if (index >= 0) {
                                                        Friend newFriend = dataSnapshot.getValue(Friend.class);
                                                        newFriend.id = dataSnapshot.getKey();
                                                        contacts.get(index).update(newFriend);
                                                        adapter.notifyItemChanged(friends.size() + index);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    phones.close();
                }
            }).start();
        }
    }

    private int getContactById(String id) {
        int index = 0;
        for(Friend contactFriend : contacts) {
            if (id.equals(contactFriend.id)) {
                return index;
            }
            index += 1;
        }
        return -1;
    }

    private void getFriends(final FirebaseUser user) {
        Log.d(TAG, "getFriends");
        if (!friendsDownloaded) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_FRIENDS)
                    .child(user.getUid())
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if ((!friendsId.contains(dataSnapshot.getKey())) {
                                friendsId.add(data.getKey());
                            }
                            friendsDownloaded = true;
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            Log.d(TAG, "onChildChanged: " + dataSnapshot);
//                            if (friendsId.contains())
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            friendsDownloaded = false;
                        }
                    });
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot data : dataSnapshot.getChildren()) {
//                        Log.d(TAG, "child: " + data + "\n");
//                        if ((Boolean) data.getValue()) {
//                            friendsId.add(data.getKey());
//                        }
//                    }
//                    addFriendsListener();
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    friendsDownloaded = false;
//                }
//            });
        }
    }

    private void addFriendsListener() {
        Log.d(TAG, "addFriendsListener: " + friendsId.size());
        for (final String uid : friendsId) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USERS)
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Friend friend = dataSnapshot.getValue(Friend.class);
                            if (friend != null) {
                                friend.id = dataSnapshot.getKey();
                                final int index = friendsId.indexOf(dataSnapshot.getKey());
                                Friend olFriend = friends.get(friendsId.get(index));
                                if(olFriend != null) { //update friend
                                    Log.d(TAG, "friend updated: " + olFriend.name);
                                    olFriend.update(friend);
                                    friends.get(friendsId.get(index)).update(friend);
                                    ((FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).updateFriendOnMap(friend);
                                    adapter.notifyItemChanged(index);
                                } else { // add friend
                                    Log.d(TAG, "friend added: " + friend.name + "picture: " + friend.pictureUrl);
//                            friends.put(friend.id, friend);
                                    ((FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).updateFriendOnMap(friend);
//                            adapter.notifyItemInserted(index);
                                    adapter.addItem(friend);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "Error: " + databaseError.getDetails() + databaseError.getDetails());
                        }
                    });
        }
    }

    public Boolean checkContactsPermission() {
        if (ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private Boolean checkPermissions() {
        Boolean permissionGranted = true;
        if (ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission Missing: Location");
            permissionGranted = false;
        }

        if (ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission Missing: Camera");
            permissionGranted = false;
        }

        if (ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission Missing: Storage");
            permissionGranted = false;
        }

        return  permissionGranted;
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = ProfileFragment.newInstance();
                    break;
                case 1:
                    fragment = FriendsActivityFragment.newInstance();
                    break;
                default:
                    fragment = new Fragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        if(appBarExpanded) {
            appBarLayout.setExpanded(false, true);
        } else if (mPager.getCurrentItem() != 1) {
            mPager.setCurrentItem(1, true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_CONTACTS_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "contact permission granted");
                    getFriendsFromContacts();
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_DWELL |
                        GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT
        );
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private void requestPermissions() {
        if (ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat
                    .requestPermissions(
                            MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_PERMISSION);
        }
    }

    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, getString(R.string.not_connected));
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.d(TAG, "Geofences Up");
                }
            });
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Security: " + securityException.toString());
        }
    }

    public void removeGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Log.d(TAG, getString(R.string.not_connected));
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()) {
                        Log.d(TAG, "Geofences Down");
                    }
                }
            });
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "security: " + securityException.toString());
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient");

        requestPermissions();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void populateGeofenceList() {

//        mDatabase.child("geofences").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
////                Event event = dataSnapshot.getValue(Event.class);
////                event.setKey(dataSnapshot.getKey());
////                Log.d(TAG, event.toString());
////                mGeofenceList.add(event.getGeofence());
////                addGeofences();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected");
        populateGeofenceList();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
