package co.gadder.gadder;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TimingLogger;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
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
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import co.gadder.gadder.emoji.Objects;
import co.gadder.gadder.emoji.People;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final String FRAGMENT_LOCATION_TAG = "fragment_location_tag";
    private static final String FRAGMENT_CONTACTS_TAG = "fragment_contacts_tag";

    private static final int MESSAGE_COLOR = Color.argb(255,33,34,89);
    private static final int SELECTION_COLOR = Color.argb(0,33,34,89);

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
    protected Set<String> tokens;
    protected Map<String, Friend> friends;

    // Listeners
    protected ValueEventListener userListener;
    protected ChildEventListener friendsListener;
    protected Map<String, ValueEventListener> listeners;

    protected String loginState = null;
    protected Boolean friendsDownloaded = false;

    // View pager
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;

    protected Friend user;

    // Coordinator layout
    public Boolean appBarExpanded;
    private AppBarLayout appBarLayout;

    // Fragments
    private FriendsMapFragment mapFragment;
    private ProfileFragment profileFragment;
    private FriendsActivityFragment activityFragment;

    private int color;

    //////////////////// ACTIVITY LIFECYCLE ////////////////////
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        FirebaseCrash.log(TAG + " created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokens = new HashSet<>();
        friends = new HashMap<>();
        listeners = new HashMap<>();

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        hideKeyboard();
        setActivityButton();
        listenToCoordinatorExpansion();
        setViewPager();
        getMapFragment();
//        getColors();
//        setPrivacyButton();
//        displayPermissionDialog();

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    MainActivity.this.user = new Friend();
                    MainActivity.this.user.id = user.getUid();

                    if (loginState == null || !loginState.equals("friends")) {
                        loginState = "friends";

                        requestLocationPermission();
                        requestContactsPermission();

                        addUserListener(user.getUid());
                        addFriendsListener(user.getUid());

                        updateUser();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getFriendsFromContacts();
                            }
                        }, 25000);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadToken();
                            }
                        }, 15000);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadNumberOfFriends();
                            }
                        }, 30000);
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

        // Add Database listeners
        if (user != null) {
            addUserListener(user.id);
            addFriendsListener(user.id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove Database listeners
        removeUserListener();
        removeFriendsListener();
        removeAllChildFriendListeners();

        // Pause all volley requests than send remove notification
        RequestManager.getInstance(MainActivity.this).cancelAllRequests();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestManager.getInstance(MainActivity.this).sendRemoveNotificationRequest(tokens);
            }
        }, 5000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
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
            case PermissionManager.REQUEST_CONTACTS_PERMISSION:
                FirebaseCrash.logcat(Log.DEBUG, TAG, "request_contacts_permission");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "contact permission granted");
                    getFriendsFromContacts();

                    FragmentManager fm =  getSupportFragmentManager();
                    Fragment f = fm.findFragmentByTag(FRAGMENT_CONTACTS_TAG);
                    if (f != null) {
                        fm.beginTransaction()
                                .remove(f)
                                .commitAllowingStateLoss();
                    }
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
            case PermissionManager.REQUEST_LOCATION_PERMISSION:
                FirebaseCrash.logcat(Log.DEBUG, TAG, "request_location_permission");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "contact permission granted");

                    updateUser();

                    FragmentManager fm =  getSupportFragmentManager();
                    Fragment f = fm.findFragmentByTag(FRAGMENT_LOCATION_TAG);
                    if (f != null) {
                        fm.beginTransaction()
                                .remove(f)
                                .commitAllowingStateLoss();
                    }
                } else {
                    // TODO: EXPLAIN WHY WE NEED THE PERMISSION
                }
                break;
        }
    }

    ////////////////////// UI METHODS //////////////////////
    private void hideKeyboard() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "hideKeyboard");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void requireUserInfo() {
        mPager.setCurrentItem(0, true);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(new BitmapDrawable(getResources(), Constants.textAsBitmap(People.SMILING_FACE_WITH_OPEN_MOUTH, Constants.EMOJI_SIZE, Color.WHITE)))
                .setTitle(R.string.set_profile_title)
                .setMessage(getString(R.string.set_profile_message) + " " + People.ASTONISHED_FACE)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPager.setCurrentItem(0, true);
                    }
                })
                .setNegativeButton(getString(R.string.angry_no) + " " + People.ANGUISHED_FACE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPager.setCurrentItem(1, true);
                    }
                });
        builder.create().show();
    }

    private void requestLocationPermission() {
        FirebaseCrash.log("requestLocationPermission");

        if (!PermissionManager.checkLocationPermission(MainActivity.this)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainLayout, PermissionFragment.newInstance(PermissionFragment.LOCATION), FRAGMENT_LOCATION_TAG)
                    .commit();
        }
    }

    private void requestContactsPermission() {
        FirebaseCrash.log("requestContactsPermission");

        if (!PermissionManager.checkContactsPermission(MainActivity.this)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mainLayout, PermissionFragment.newInstance(PermissionFragment.CONTACTS), FRAGMENT_CONTACTS_TAG)
                    .commit();
        }
    }

    private void getColors() {
        color = Color.parseColor("#935aa4");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            color = getColor(R.color.colorPrimary);
        } else {
            color = getResources().getColor(R.color.colorPrimary);
        }
    }

    private void getMapFragment() {
        mapFragment = (FriendsMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void setViewPager() {
        long startTime = System.currentTimeMillis();

        mPager = (ViewPager) findViewById(R.id.mainPager);
        PagerAdapter mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(1);

        final ImageView profile = (ImageView) findViewById(R.id.goToProfile);
        profile.setImageBitmap(Constants.textAsBitmap(People.SMILING_FACE_WITH_OPEN_MOUTH, Constants.NAVIGATION_EMOJI_SIZE, Color.WHITE));
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
        main.setColorFilter(SELECTION_COLOR);
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
        notification.setImageBitmap(Constants.textAsBitmap(Objects.CLOSED_MAIL_BOX_WITH_LOWERED_FLAG, Constants.NAVIGATION_EMOJI_SIZE, Color.WHITE));
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
                    profile.setColorFilter(SELECTION_COLOR);
                    main.clearColorFilter();
                    notification.clearColorFilter();
                } else if (position == 1) {
                    profile.clearColorFilter();
                    main.setColorFilter(SELECTION_COLOR);
                    notification.clearColorFilter();
                } else {
                    profile.clearColorFilter();
                    main.clearColorFilter();
                    notification.setColorFilter(SELECTION_COLOR);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        long elapsedTime = System.currentTimeMillis() - startTime;
        FirebaseCrash.logcat(Log.DEBUG, TAG, "SetPagerMenu: " + elapsedTime + "ms");
    }

    public void setNotificationListener(int noNotifications) {
        final ImageView notification = (ImageView) findViewById(R.id.goToNotification);
        if (noNotifications > 0 ) {
            notification.clearColorFilter();
            notification.setImageBitmap(Constants.textAsBitmap(Objects.OPEN_MAIL_BOX_WITH_RAISED_FLAG, Constants.NAVIGATION_EMOJI_SIZE, Color.WHITE));
        } else {
            notification.clearColorFilter();
            notification.setColorFilter(SELECTION_COLOR);
            notification.setImageBitmap(Constants.textAsBitmap(Objects.CLOSED_MAIL_BOX_WITH_LOWERED_FLAG, Constants.NAVIGATION_EMOJI_SIZE, Color.WHITE));
        }
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
        FirebaseCrash.log("goToLoginActivity");

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
                    findViewById(R.id.toolbar).setBackgroundColor(Color.TRANSPARENT);
                } else {
                    findViewById(R.id.toolbar).setBackgroundColor(color);
                    appBarExpanded = false;
                }
            }
        });
    }

    private void setPrivacyButton() {
        final FloatingActionButton privacyFab = (FloatingActionButton) findViewById(R.id.privacyFab);
        privacyFab.setImageBitmap(Constants.textAsBitmap(Objects.LOCK, 200, Color.WHITE));
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
                FirebaseCrash.log("Activity Button Clicked");
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
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
                    NotificationFragment notificationFragment = NotificationFragment.newInstance();
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

    public void selectFriend(Friend friend) {
        if (friend != null && friend.sharing != null && friend.sharing.locationSharing != null && friend.sharing.locationSharing) {
            appBarLayout.setExpanded(true, true);
            appBarExpanded = true;
            if (mapFragment != null) {
                mapFragment.focusFriend(friend);
            }
        }
    }

    /////////////////// SERVER METHODS ///////////////////
    public void uploadToken() {
        FirebaseCrash.log("goToLoginActivity");

        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null && !token.isEmpty() && user != null) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_TOKEN)
                    .child(user.id)
                    .setValue(token)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report(e);
                        }
                    });
        }
    }

    public void uploadNumberOfFriends() {
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USERS)
                .child(user.id)
                .child(Constants.NO_FRIENDS)
                .setValue(friends.size())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FirebaseCrash.report(e);
                    }
                });
    }

    public void requestFriendship(String friendId) {

        if (user != null) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_NOTIFICATIONS)
                    .child(friendId)
                    .child(user.id)
                    .setValue("friendship")
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report(e);
                        }
                    });
        }

        notifyFriendshipRequest(friendId);
    }

    private void notifyFriendshipRequest(final String uid) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "requestUpdate: " + uid);
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USER_TOKEN)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseCrash.log("gotToken");

                        String token = dataSnapshot.getValue(String.class);
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "User: " + uid + " token: " + token);
                        if (token != null) {
                            tokens.add(token);

                            RequestManager
                                    .getInstance(MainActivity.this)
                                    .sendFriendshipRequestNotification(token, user.name, user.pictureUrl);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Canceled uid:token request: " + uid);
                        FirebaseCrash.report(new Throwable(databaseError.toString()));
                    }
                });
    }

    public void removeFriendship(String uid) {

        if (user != null) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_FRIENDS)
                    .child(user.id)
                    .child(uid)
                    .setValue(false)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseCrash.report(e);
                        }
                    });
        }


    }

    private void updateUser() {
        FirebaseCrash.log("updateUser");

        startService(new Intent(MainActivity.this, UserActivityService.class));
    }

    private void getFriendsFromContacts() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getFriendsFromContacts");
        if (PermissionManager.checkContactsPermission(this)) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "got Contacts permission");

            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            if (phones != null) {

                if (activityFragment != null) {
                    activityFragment.hideProgressBar();
                }

                while (phones.moveToNext()) {
                    final Friend friend = new Friend();
                    friend.name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String rawPhone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    friend.phone = rawPhone.replaceAll("\\D+", ""); // remove non number characters

                    if (friend.phone != null && !friend.phone.isEmpty()) {
                        mDatabase
                                .child(Constants.VERSION)
                                .child(Constants.USER_PHONE)
                                .child(friend.phone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            FirebaseCrash.logcat(Log.DEBUG, TAG, "name: " + friend.name + " phone: " + friend.phone);
                                            friend.id = dataSnapshot.getValue(String.class);
                                            if (!friends.containsKey(friend.id)) { // not a friend
                                                FirebaseCrash.logcat(Log.DEBUG, TAG, friend.name + " is not a friend " + friend.id + friends.containsKey(friend.id));

                                                mDatabase
                                                        .child(Constants.VERSION)
                                                        .child(Constants.USERS)
                                                        .child(friend.id)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                FirebaseCrash.logcat(Log.DEBUG, TAG, "contact info downloaded");
                                                                Friend newFriend = dataSnapshot.getValue(Friend.class);
                                                                if (newFriend != null) {


                                                                    newFriend.id = dataSnapshot.getKey();
                                                                    newFriend.friendship = Friend.CONTACT;
                                                                    if (newFriend.name == null || newFriend.name.isEmpty()) {
                                                                        newFriend.name = friend.name;
                                                                    }

                                                                    // Display contact
                                                                    friends.put(newFriend.id, newFriend);
                                                                    if (activityFragment != null) {
                                                                        activityFragment.updateRecycler();
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                }
                phones.close();
            }
        }
    }

    private void requestUpdate(final String uid) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "requestUpdate: " + uid);
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USER_TOKEN)
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FirebaseCrash.log("gotToken");

                        String token = dataSnapshot.getValue(String.class);
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "User: " + uid + " token: " + token);
                        if (token != null) {
                            tokens.add(token);

                            RequestManager
                                    .getInstance(MainActivity.this)
                                    .sendUpdateRequest(token, user.name, user.pictureUrl);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Canceled uid:token request: " + uid);
                        FirebaseCrash.report(new Throwable(databaseError.toString()));
                    }
                });
    }

    private ValueEventListener getUserListener() {
        if (userListener == null) {
            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FirebaseCrash.log("gotUser");

                    FirebaseCrash.logcat(Log.DEBUG, TAG, "snapshot: "+ dataSnapshot.toString());
                    user = dataSnapshot.getValue(Friend.class);
                    if (user != null) {
                        user.id = dataSnapshot.getKey();

                        if (activityFragment != null) {
                            activityFragment.updateRecycler();
                        }

                        if (profileFragment != null) {
                            profileFragment.setUser(user);
                        }

                        // set activity to fab
                        if (user.activity != null && user.activity.type != null) {
                            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activityFab);
                            GadderActivities.GadderActivity act = GadderActivities.ACTIVITY_MAP.get(user.activity.type);
                            if (fab != null && act != null ) {
                                fab.setImageBitmap(Constants.textAsBitmap(act.emoji, Constants.EMOJI_SIZE, Color.WHITE));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    FirebaseCrash.report(new Throwable(databaseError.toString()));
                }
            };
        }
        return userListener;
    }

    private void addUserListener(final String uid) {
        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USERS)
                .child(uid)
                .addValueEventListener(getUserListener());
    }

    private void removeUserListener() {
        if (user != null) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USERS)
                    .child(user.id)
                    .addValueEventListener(getUserListener());
        }
    }

    private ChildEventListener getFriendsListener() {
        if (friendsListener == null) {
            friendsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snap, String s) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "onChildAdded: " + snap.toString());
                    if ((Boolean) snap.getValue()) {
                        requestUpdate(snap.getKey());
                        addChildFriendListener(snap.getKey());
                    }
                    friendsDownloaded = true;
                }

                @Override
                public void onChildChanged(DataSnapshot snap, String s) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "onChildChanged: " + snap.toString());
                    if ((Boolean) snap.getValue()) {
                        addChildFriendListener(snap.getKey());
                    } else {
                        removeChildFriendListener(snap.getKey());
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot snap) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "onChildRemoved: " + snap.toString());
                    removeChildFriendListener(snap.getKey());
                }

                @Override
                public void onChildMoved(DataSnapshot snap, String s) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "onChildMoved: " + snap.toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    friendsDownloaded = false;
                }
            };
        }
        return friendsListener;
    }

    private void removeFriendsListener() {
        if (user != null) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "unsyncFriend");
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USER_FRIENDS)
                    .child(user.id)
                    .removeEventListener(getFriendsListener());
        }
    }

    private void addFriendsListener(String uid) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "addFriendsListener");

        mDatabase
                .child(Constants.VERSION)
                .child(Constants.USER_FRIENDS)
                .child(uid)
                .addChildEventListener(getFriendsListener());
    }

    private void removeChildFriendListener(String uid) {
        friends.remove(uid);
        listeners.remove(uid);
    }

    private void addChildFriendListener(String uid) {
        FirebaseCrash.log("addChildFriendListener");

        FirebaseCrash.logcat(Log.DEBUG, TAG, "addChildFriendListener: " + friends.size());

        if (activityFragment != null) {
            activityFragment.hideProgressBar();
        }

        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "onDataChange: " + dataSnapshot.toString());
                final Friend friend = dataSnapshot.getValue(Friend.class);
                if (friend != null) {

                    FirebaseCrash.logcat(Log.DEBUG, TAG, "got friend: " + friend.name);

                    // Save friends
                    friend.friendship = Friend.FRIEND;
                    friend.id = dataSnapshot.getKey();

                    if(friends.containsKey(friend.id)) {

                        // Pass old image if same
                        String oldUrl = friends.get(friend.id).pictureUrl;
                        if (friend.pictureUrl != null && oldUrl != null && friend.pictureUrl.equals(oldUrl)) {
                            friend.image = friends.get(friend.id).image;
                        }

                        friends.put(friend.id, friend);
                        if (activityFragment != null && activityFragment.adapter != null) {
                            activityFragment.adapter.updateItem(friend.id);
                        }
                    } else {
                        friends.put(friend.id, friend);
                        if (activityFragment != null && activityFragment.adapter != null) {
                            activityFragment.adapter.addItem(friend.id);
                        }
                    }

                    // update fragments
                    if (mapFragment != null) {
                        mapFragment.updateFriendOnMap(friend);
                    }
                    if (activityFragment != null) {
                        activityFragment.updateRecycler();
                    }

                    // Update friends after random time
                    if (friend.id != null && user != null && user.id != null && !friend.id.equals(user.id) && friend.friendship.equals(Friend.FRIEND)) {
                        int time = 5000 * (new Random().nextInt(5) + 1);
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "update: " + friend.name + " after " + time);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                requestUpdate(friend.id);
                            }
                        }, time);
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

        listeners.put(uid, listener);
    }

    private void removeAllChildFriendListeners() {
        for (String uid : listeners.keySet()) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USERS)
                    .child(uid)
                    .removeEventListener(listeners.get(uid));
        }
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
            FirebaseCrash.logcat(Log.DEBUG, TAG, getString(R.string.not_connected));
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
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Geofences Up");
                }
            });
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, "Security: " + securityException.toString());
        }
    }

    public void removeGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, getString(R.string.not_connected));
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
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Geofences Down");
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
        FirebaseCrash.logcat(Log.DEBUG, TAG, "buildGoogleApiClient");

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
////                FirebaseCrash.logcat(Log.DEBUG, TAG, event.toString());
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
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onConnected");
        populateGeofenceList();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }
}
