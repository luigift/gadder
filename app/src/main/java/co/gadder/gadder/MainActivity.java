package co.gadder.gadder;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    private static final String SENDER_ID = "254610893779";
    private static final String SERVER_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String API_KEY = "AIzaSyDHV8lCJNf8VUIOx2L8KCI8zDYhlXinB70";

    private RequestQueue requestQueue;

    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;
    public FirebaseAuth.AuthStateListener mAuthListener;

    protected List<String> friendsId;
    protected Map<String, Friend> friends;
    protected FriendsRecyclerAdapter adapter;

    protected String loginState = null;
    protected Boolean friendsDownloaded = false;

    private static final int NUM_PAGES = 3;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;


    protected final Location mLocation = new Location("");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocation.setLatitude(-23.6233303);
        mLocation.setLongitude(-46.6732878);

        friends = new HashMap<>();
        friendsId = new ArrayList<>();
        adapter = new FriendsRecyclerAdapter(MainActivity.this);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "AuthListener: " + loginState);
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    getFriends(user);
                    Log.d(TAG, "Logged in");
                    if (checkPermissions()) {
                        if (savedInstanceState == null && (loginState == null || !loginState.equals("friends"))) {
                            Log.d(TAG, "FriendsActivityFragment");
                            mPager = (ViewPager) findViewById(R.id.mainPager);
                            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                            mPager.setAdapter(mPagerAdapter);
                            mPager.setCurrentItem(1);
//                            getSupportFragmentManager().beginTransaction()
//                                    .replace(R.id.activity_main, FriendsActivityFragment.newInstance())
//                                    .commit();
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

        requestQueue = Volley.newRequestQueue(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ImageButton friendButton = (ImageButton) findViewById(R.id.mainFriendsButton);
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1, true);
            }
        });


        ImageButton settings = (ImageButton) findViewById(R.id.mainSettingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(0, true);
            }
        });

        ImageButton mapButton = (ImageButton) findViewById(R.id.mainMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(2, true);
            }
        });

    }


//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Action: " + intent.getAction() + " extras: " + intent.getExtras().toString());
//        }
//    };


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

    private void getFriends(final FirebaseUser user) {
        Log.d(TAG, "getFriends");
        if (!friendsDownloaded) {
            mDatabase.child(Constants.USER_FRIENDS).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Log.d(TAG, "child: " + data + "\n");
                        if ((Boolean) data.getValue()) {
                            friendsId.add(data.getKey());
                        }
                    }
                    addFriendsListener();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    friendsDownloaded = false;
                }
            });
        friendsDownloaded = true;
        }
    }

    private void addFriendsListener() {
        Log.d(TAG, "addFriendsListener: " + friendsId.size());
        for (final String uid : friendsId) {
            mDatabase.child(Constants.USERS).child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (friend != null) {
                        friend.id = dataSnapshot.getKey();
                        adapter.updateItem(friend);
                        Log.d(TAG, "update: " + friend.name);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "Error: " + databaseError.getDetails() + databaseError.getDetails());
                }
            });
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
                    fragment = ProfileFragment.newInstance(new Friend());
                    break;
                case 1:
                    fragment = FriendsActivityFragment.newInstance();
                    break;
                case 2:
                    fragment = FriendsMapFragment.newInstance();
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
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
}
