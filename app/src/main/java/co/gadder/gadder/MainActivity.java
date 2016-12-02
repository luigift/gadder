package co.gadder.gadder;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimingLogger;
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

    // Time
    private TimingLogger timings = new TimingLogger(TAG, "");

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

    protected Friend user;

    // Coordinator layout
    public Boolean appBarExpanded;
    private AppBarLayout appBarLayout;

    // Fragments
    private FriendsMapFragment mapFragment;
    private ProfileFragment profileFragment;
    private NotificationFragment notificationFragment;
    private FriendsActivityFragment activityFragment;


    /*
    *       Activity Lifecycle
    * */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (loginState == null || !loginState.equals("friends")) {
                        loginState = "friends";

                        setPrivacyButton();
                        setActivityButton();
                        getMapFragment();
                        syncFriends(user.getUid());
                        syncUserProfile(user.getUid());
                        displayPermissionDialog();
                        updateUser();
                        setViewPager();
                        setNotificationListener(user.getUid());
                        listenToCoordinatorExpansion();
                        getFriendsFromContacts();

                    }
                } else {
                    if (loginState == null || !loginState.equals("login")) {
                        loginState = "login";
                        goToLoginActivity();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Glide.with(getApplicationContext()).onStart();

        List<String> tokens = new ArrayList<>();
        tokens.add(FirebaseInstanceId.getInstance().getToken());
        RequestManager.getInstance(MainActivity.this).sendUpdateRequest(tokens);
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
        Glide.with(getApplicationContext()).onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.with(getApplicationContext()).onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Glide.with(getApplicationContext()).pauseRequests();
        Glide.with(getApplicationContext()).onDestroy();
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
            case PermissionManager.REQUEST_CONTACTS_PERMISSION:
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

    /*
    *       UI methods
    * */
    private void getMapFragment() {
        mapFragment = (FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void setViewPager() {
        long startTime = System.currentTimeMillis();

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



        long elapsedTime = System.currentTimeMillis() - startTime;
        Log.d(TAG, "SetPagerMenu: " + elapsedTime + "ms");
    }

    private void setNotificationListener(String uid) {
        final ImageView notification = (ImageView) findViewById(R.id.goToNotification);
        mDatabase.child(Constants.VERSION)
                .child(Constants.USERS)
                .child(uid)
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
    }

    private void displayPermissionDialog() {
        if (!PermissionManager.checkLocationPermission(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                    .setMessage("Give the location permission bro")
                    .setTitle("Locatioooon!!")
                    .setPositiveButton("Ok, ok...", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PermissionManager.requestLocationPermission(MainActivity.this);
                        }
                    });
            builder.create().show();
        }
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void listenToCoordinatorExpansion() {
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
    }

    private void setPrivacyButton() {
        final FloatingActionButton privacyFab = (FloatingActionButton) findViewById(R.id.privacyFab);
//        privacyFab.setImageBitmap(Constants.textAsBitmap(Objects.LOCK, 50, Color.WHITE));
        privacyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Change user privacy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setActivityButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activityFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InputActivity.class));
            }
        });
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
                    profileFragment = ProfileFragment.newInstance();
                    fragment = profileFragment;
                    break;
                case 1:
                    activityFragment = FriendsActivityFragment.newInstance();
                    fragment = activityFragment;
                    break;
                case 2:
                    notificationFragment = NotificationFragment.newInstance();
                    fragment = notificationFragment;
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

    public void selectFriend(int position) {
        appBarLayout.setExpanded(true, true);
        appBarExpanded = true;
        mapFragment.focusFriend(getFriendByPosition(position));
    }

    /*
    *       Server methods
    * */
    public void requestFriendship(int position) {
        if (user != null) {
            Friend contact = contacts.get(friends.size() - position);
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_NOTIFICATIONS)
                    .child(contact.id)
                    .child(user.id)
                    .setValue("friendship");

            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_FRIENDS)
                    .child(contact.id)
                    .child(user.id)
                    .setValue(false);
        }
    }

    private void updateUser() {
        startService(new Intent(MainActivity.this, UserActivityService.class));
    }

    private void syncTokens(){}

    public void syncUserProfile(String uid) {
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USERS)
                .child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "snapshot: "+ dataSnapshot.toString());
                        user = dataSnapshot.getValue(Friend.class);
                        if (user != null) {
                            user.id = dataSnapshot.getKey();
                            Glide.with(MainActivity.this).load(user.pictureUrl).asBitmap().into(
                                    new SimpleTarget<Bitmap>(200, 200) {
                                        @Override
                                        public void onResourceReady(Bitmap resource,
                                                                    GlideAnimation<? super Bitmap> glideAnimation) {
                                            user.image = resource;
                                            profileFragment.setUser(user);
                                        }
                                    });
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
        if (PermissionManager.checkContactsPermission(this)) {
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

    private void syncFriends(String uid) {
        Log.d(TAG, "syncFriends");
        if (!friendsDownloaded) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_FRIENDS)
                    .child(uid)
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
    }

    private void removeFriend(String uid) {
        Log.d(TAG, "removeFriend: "+  uid);
        friendsId.remove(uid);
        ValueEventListener listener = listeners.get(uid);
        if (listener != null) {
            mDatabase.removeEventListener(listener);
        }
        listeners.remove(uid);
        adapter.notifyDataSetChanged();
//        adapter.notifyItemRemoved(index);
    }

    private ValueEventListener addFriendListener(String uid) {
        Log.d(TAG, "addFriendListener: " + friendsId.size());

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                final Friend friend = dataSnapshot.getValue(Friend.class);
                if (friend != null) {
                    Log.d(TAG, "got friend: " + friend.name);
                    friend.id = dataSnapshot.getKey();

                    final int index = friendsId.indexOf(dataSnapshot.getKey());
                    final Friend olFriend = friends.get(friendsId.get(index));
                    Log.d(TAG, "oldFriend: " + olFriend.toString());
                    if(olFriend != null) { //update friend
                        Log.d(TAG, "friend updated: " + olFriend.name);
//                        olFriend.update(friend);
                        friends.get(friendsId.get(index)).update(friend);
                        Log.d(TAG, "updated oldFriend: " + friends.get(friendsId.get(index)).toString());
                        if (olFriend.image == null) {
                            Glide.with(MainActivity.this)
                                    .load(friend.pictureUrl)
                                    .asBitmap()
                                    .into(new SimpleTarget<Bitmap>(200, 200) {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            olFriend.image = resource;
                                            mapFragment.updateFriendOnMap(olFriend);
                                            adapter.notifyDataSetChanged();
//                                            adapter.notifyItemInserted(index);
//                                            adapter.notifyItemChanged(index, olFriend);
                                            Log.d(TAG, "Friend image loaded: "+ olFriend.name);
                                        }
                                    });
                        } else {
                            mapFragment.updateFriendOnMap(friend);
                            adapter.notifyItemChanged(index);
                        }
                    } else { // add friend
                        Log.d(TAG, "friend added: " + friend.name + "picture: " + friend.pictureUrl);
                        mapFragment.updateFriendOnMap(friend);
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


    ////////////////////// GEOFENCES //////////////////////

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

        if (PermissionManager.checkLocationPermission(this)) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        } else {
            PermissionManager.requestLocationPermission(this);
        }
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
