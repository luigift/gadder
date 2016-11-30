package co.gadder.gadder;

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.StrictMode;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_LOCATION_PERMISSION = 10;
    public static final int REQUEST_READ_CONTACTS_PERMISSION = 11;


    // Geofence
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent = null;

    // Firebase
    public FirebaseAuth mAuth;
    public StorageReference mStorage;
    public DatabaseReference mDatabase;
    public FirebaseAuth.AuthStateListener mAuthListener;

    // Friends & Contacts
    protected List<Friend> contacts;
    protected List<String> friendsId;
    protected Map<String, Friend> friends;
    protected Map<String, ValueEventListener> listeners;


    protected FriendsRecyclerAdapter adapter;

    protected String loginState = null;
    protected Boolean friendsDownloaded = false;

    // View pager
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    public String uid;
    protected Friend user;

    // Coordinator layout
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
        listeners = new HashMap<>();
        contacts = new ArrayList<>();
        friendsId = new ArrayList<>();
        adapter = new FriendsRecyclerAdapter(MainActivity.this);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "AuthListener: " + loginState);
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    getFriends(user);
//                    getFriendsFromContacts();
                    Log.d(TAG, "Logged in");
                    if (checkPermissions()) {
                        if (savedInstanceState == null && (loginState == null || !loginState.equals("friends"))) {
                            Log.d(TAG, "FriendsActivityFragment");

                            startService(new Intent(MainActivity.this, UserActivityService.class));

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
                                    } else {
                                        appBarLayout.setExpanded(true, false);
                                    }
                                    mPager.setCurrentItem(1, true);
                                }
                            });

                            final ImageView notification = (ImageView) findViewById(R.id.goToNotification);
                            notification.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (appBarExpanded) {
                                        appBarLayout.setExpanded(false, true);
                                    }
                                    mPager.setCurrentItem(2, true);
                                }
                            });

                            mDatabase.child(Constants.VERSION)
                                    .child(Constants.USERS)
                                    .child(user.getUid())
                                    .child("noNotifications")
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Integer noNotifications =
                                                    dataSnapshot.getValue(Integer.class);
                                            if (noNotifications != null && noNotifications > 0 ) {
                                                notification.setImageResource(R.drawable.ic_notifications_active_black_24dp);
                                            } else {
                                                notification.setImageResource(R.drawable.ic_notifications_black_24dp);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

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
                                        notification.clearColorFilter();
                                    } else if (position == 1) {
                                        profile.clearColorFilter();
                                        main.setColorFilter(Color.argb(255,33,34,89));
                                        notification.clearColorFilter();
                                    } else {
                                        profile.clearColorFilter();
                                        main.clearColorFilter();
                                        notification.setColorFilter(Color.argb(255,33,34,89));
                                    }
                                }

                                @Override
                                public void onPageScrollStateChanged(int state) {

                                }
                            });

                            loginState = "friends";
                        }
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                                .setMessage("Give the location permission bro")
                                .setTitle("Locatioooon!!")
                                .setPositiveButton("Ok, ok...", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        requestPermissions();
                                    }
                                });
                        builder.create().show();
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
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                        loginState = "login";
                    }
                }
            }
        };

        // Detect AppBarLayout Expanded
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(false);
        appBarExpanded = false;
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
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });

        final FloatingActionButton privacyFab = (FloatingActionButton) findViewById(R.id.privacyFab);
//        privacyFab.setImageBitmap(Constants.textAsBitmap(Objects.LOCK, 50, Color.WHITE));
        privacyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (privacyFab.)
//                privacyFab.setImageResource(R.drawable.ic_lock_open_white_24dp);
//                startService(new Intent(MainActivity.this, UserActivityService.class));


                Toast.makeText(MainActivity.this, "Update user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        List<String> tokens = new ArrayList<>();
        tokens.add(FirebaseInstanceId.getInstance().getToken());
        RequestManager.getInstance(MainActivity.this).sendUpdateRequest(tokens);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(mReceiver, intentFilter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        List<String> tokens = new ArrayList<>();
        tokens.add(FirebaseInstanceId.getInstance().getToken());
        RequestManager.getInstance(MainActivity.this).sendRemoveNotificationRequest(tokens);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
//        unregisterReceiver(mReceiver);
    }

    public void getUserProfile() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "snapshot: "+ dataSnapshot.toString());
                user = dataSnapshot.getValue(Friend.class);
                if (user != null) {
                    Log.d(TAG, "user: " + user.toString());

                    user.id = dataSnapshot.getKey();
                    Log.d("ProfileFragment", "user: " + dataSnapshot.toString());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Friend getFriendByPosition(int position) {
        return friends.get(friendsId.get(position));
    }

    private void getFriendsFromContacts() {
        Log.d(TAG, "getFriendsFromContacts");
        if (checkContactsPermission()) {
            Log.d(TAG, "got Contacts permission");

            final StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
            AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                            .permitDiskWrites()
                            .build());
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    StrictMode.setThreadPolicy(old);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                    while (phones.moveToNext()) {
                        final Friend friend = new Friend();
                        friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        friend.phone = rawPhone.replaceAll("\\D+", ""); // remove non number characters

                        mDatabase
                                .child(Constants.VERSION)
                                .child(Constants.USER_PHONE)
                                .child(friend.phone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Log.d(TAG, "name: " + friend.name + " phone: " + friend.phone);
                                            friend.id = dataSnapshot.getValue(String.class);
                                            if (!friends.containsKey(friend.id)) { // not a friend
                                                Log.d(TAG, friend.name + " is not a friend " + friend.id + friends.containsKey(friend.id));
                                                int index = getContactById(friend.id);
                                                if (index < 0) { // haven't been displayed
                                                    contacts.add(friend);
                                                    adapter.notifyItemChanged(friends.size() + contacts.size());

                                                    mDatabase
                                                            .child(Constants.VERSION)
                                                            .child(Constants.USERS)
                                                            .child(friend.id)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
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
                    return "done";
                }
            };
            task.execute();
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
                        public void onChildAdded(DataSnapshot snap, String s) {
                            Log.d(TAG, "onChildAdded: " + snap.toString());
                            if ((Boolean) snap.getValue()) {
                                addFriend(snap.getKey());
                            }
                            friendsDownloaded = true;
                        }

                        @Override
                        public void onChildChanged(DataSnapshot snap, String s) {
                            Log.d(TAG, "onChildChanged: " + snap.toString());
                            if ((Boolean) snap.getValue()) {
                                addFriend(snap.getKey());
                            } else {
                                removeFriend(snap.getKey());
                            }
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot snap) {
                            Log.d(TAG, "onChildRemoved: " + snap.toString());
                            removeFriend(snap.getKey());
                        }

                        @Override
                        public void onChildMoved(DataSnapshot snap, String s) {
                            Log.d(TAG, "onChildMoved: " + snap.toString());

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            friendsDownloaded = false;
                        }
                    });
        }
    }

    private void addFriend(String uid) {
        Log.d(TAG, "add Friend: " + uid);
        if (!friendsId.contains(uid)) {
            Friend friend = new Friend();
            friend.id = uid;
            friendsId.add(uid);
            friends.put(uid, friend);
            ValueEventListener listener = addFriendListener(uid);
            listeners.put(uid, listener);
        }
        Log.d(TAG, "FriendsId: " + friendsId.size() + " " + friendsId.toString());
        Log.d(TAG, "Friends: " + friends.size() +  " " + friends.keySet().toString());
        Log.d(TAG, "Listeners: " + listeners.size() + " " + listeners.keySet().toString());
    }

    private void removeFriend(String uid) {
        Log.d(TAG, "removeFriend: "+  uid);
        int index = friendsId.indexOf(uid);
        friendsId.remove(uid);
        ValueEventListener listener = listeners.get(uid);
        if (listener != null) {
            mDatabase.removeEventListener(listener);
        }
        listeners.remove(uid);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "FriendsId: " + friendsId.size() + " " + friendsId.toString());
        Log.d(TAG, "Friends: " + friends.size() +  " " + friends.keySet().toString());
        Log.d(TAG, "Listeners: " + listeners.size() + " " + listeners.keySet().toString());
//        adapter.notifyItemRemoved(index);
    }

    private ValueEventListener addFriendListener(String uid) {
        Log.d(TAG, "addFriendListener: " + friendsId.size());

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Friend friend = dataSnapshot.getValue(Friend.class);
                if (friend != null) {
                    friend.id = dataSnapshot.getKey();
                    final int index = friendsId.indexOf(dataSnapshot.getKey());
                    final Friend olFriend = friends.get(friendsId.get(index));
                    if(olFriend != null) { //update friend
                        Log.d(TAG, "friend updated: " + olFriend.name);
                        olFriend.update(friend);
//                        friends.get(friendsId.get(index)).update(friend);
                        if (olFriend.image == null) {
                            Glide.with(MainActivity.this)
                                    .load(friend.pictureUrl)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>(200, 200) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            olFriend.image = resource;
                                            ((FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).updateFriendOnMap(friend);
                                            adapter.notifyItemChanged(index);
                                        }
                                    });
                        } else {
                            ((FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).updateFriendOnMap(friend);
                            adapter.notifyItemChanged(index);
                        }
                    } else { // add friend
                        Log.d(TAG, "friend added: " + friend.name + "picture: " + friend.pictureUrl);
//                            friends.put(friend.id, friend);
//                            adapter.notifyItemInserted(index);
                        ((FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).updateFriendOnMap(friend);
                        adapter.addItem(friend);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error: " + databaseError.getDetails() + databaseError.getDetails());
            }
        };

        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USERS)
                .child(uid)
                .addValueEventListener(listener);
        return listener;
    }

    public Boolean checkContactsPermission() {
        return ActivityCompat
                .checkSelfPermission(
                        MainActivity.this,
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
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

//        if (ActivityCompat
//                .checkSelfPermission(
//                        MainActivity.this,
//                        Manifest.permission.CAMERA) !=
//                PackageManager.PERMISSION_GRANTED ) {
//            Log.d(TAG, "Permission Missing: Camera");
//            permissionGranted = false;
//        }
//
//        if (ActivityCompat
//                .checkSelfPermission(
//                        MainActivity.this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "Permission Missing: Storage");
//            permissionGranted = false;
//        }

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
                case 2:
                    fragment = NotificationFragment.newInstance();
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
