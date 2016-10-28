package co.gadder.gadder;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    protected String loginState = null;
    protected Boolean friendsDownloaded = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        friends = new HashMap<>();
        friendsId = new ArrayList<>();

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
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.activity_main, FriendsActivityFragment.newInstance())
                                    .commit();
                            loginState = "friends";
                        }
                    } else {
                        if (savedInstanceState == null && (loginState == null || !loginState.equals("permissions"))) {
                            Log.d(TAG, "FriendsActivityFragment");
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.activity_main, PermissionFragment.newInstance())
                                    .commit();
                            loginState = "permissions";
                        }
                    }
                } else {
                    Log.d(TAG, "Logged out");
                    if (null == savedInstanceState && (loginState == null || !loginState.equals("login"))) {
                        Log.d(TAG, "LoginFragment");
                        getFragmentManager().beginTransaction()
                                .replace(R.id.activity_main, FindFriendsFragment.newInstance())
                                .commit();
                        loginState = "login";
                    }
                }
            }
        };

        requestQueue = Volley.newRequestQueue(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button findFriends = (Button) findViewById(R.id.buttonFindFriends);
        findFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_CONTACTS}, 32);
                    return;
                }
                getFragmentManager().beginTransaction()
                        .addToBackStack("findFriends")
                        .replace(R.id.activity_main, FindFriendsFragment.newInstance())
                        .commit();
            }
        });

        Button token = (Button) findViewById(R.id.token);
        token.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });


        Button logout = (Button) findViewById(R.id.send);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginState = null;
                mAuth.signOut();
            }
        });

        Button profile = (Button) findViewById(R.id.profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Profile added");
                getFragmentManager().beginTransaction()
                        .addToBackStack("profile")
                        .add(R.id.activity_main, ProfileFragment.newInstance(), "profile")
                        .commit();
            }
        });

    }


//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.d(TAG, "Action: " + intent.getAction() + " extras: " + intent.getExtras().toString());
//        }
//    };

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
        Log.d(TAG, "addFriendsListener");
        for (String uid : friendsId) {
            mDatabase.child(Constants.USERS).child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "DataSnapshot: " + dataSnapshot.toString());
                    Friend friend = dataSnapshot.getValue(Friend.class);
                    if (friend != null) {
                        friends.put(dataSnapshot.getKey(), friend);
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
}
