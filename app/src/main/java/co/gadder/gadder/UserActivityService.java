package co.gadder.gadder;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserActivityService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "UserActivityService";
    protected GoogleApiClient mGoogleApiClient;

    // Firebase
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;

    private Location location;

    private Boolean gotPlaces = false;
    private Boolean gotWeather = false;
    private Boolean gotActivity = false;
    private Boolean gotHeadphone = false;

    private Intent batteryStatus;

    Map<String, Object> childUpdates = new HashMap<>();

    public UserActivityService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        // Init firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buildGoogleApiClient();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Ask for location permission");
        } else {
            getLocation();
            getCity();
            getBatteryLevel();
            getPlaces();                                                                            // Get places
            getDetectedActivity();                                                                  // Get user activity through Awareness API
            getWeather();
            getHeadphone();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void getLocation() throws SecurityException {
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        childUpdates.put("latitude", location.getLatitude());
        childUpdates.put("longitude", location.getLongitude());
    }

    private void getCity() {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            String city = addresses.get(0).getLocality();
            Log.d(TAG, "city: " + city);
            childUpdates.put("city", city);
        }
    }

    private  void getBatteryLevel() {
        batteryStatus = registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        Log.d(TAG, "battery: " + (level*100)/ 100);
        childUpdates.put("battery", (level * 100)/100);
    }

    private void getDetectedActivity() {
        Log.d(TAG, "getDetectedActivity");
        Awareness.SnapshotApi.getDetectedActivity(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DetectedActivityResult>() {
                    @Override
                    public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {

                        if (!detectedActivityResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Could not get the current activity.");
                        } else {
                            ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
                            DetectedActivity probableActivity = ar.getMostProbableActivity();
                            childUpdates.put("activityType", probableActivity.getType());
                            childUpdates.put("activityConfidence", probableActivity.getConfidence());
                            Log.i(TAG, probableActivity.toString());
                        }
                        gotActivity = true;

                        checkStopSelf();
                    }
                });
    }

    private void getWeather() throws  SecurityException {
        Awareness.SnapshotApi.getWeather(mGoogleApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                Float temperature = weatherResult.getWeather().getTemperature(Weather.CELSIUS);
                int[] conditions = weatherResult.getWeather().getConditions();

                Log.d(TAG, "Temperature: " + temperature);
                Log.d(TAG, "Conditions: " + "\n");
                String textCondition = "";
                for(int cond : conditions) {
                    if (cond == Weather.CONDITION_CLEAR) {
                        textCondition = "Clear";
                    } else if (cond == Weather.CONDITION_FOGGY) {
                        textCondition = "Foggy";
                    } else if (cond == Weather.CONDITION_CLOUDY) {
                        textCondition = "Cloudy";
                    } else if (cond == Weather.CONDITION_HAZY) {
                        textCondition = "Hazy";
                    } else if (cond == Weather.CONDITION_ICY) {
                        textCondition = "Icy";
                    } else if (cond == Weather.CONDITION_RAINY) {
                        textCondition = "Rainy";
                    } else if (cond == Weather.CONDITION_SNOWY) {
                        textCondition = "Snowy";
                    } else if (cond == Weather.CONDITION_STORMY) {
                        textCondition = "Stormy";
                    } else if (cond == Weather.CONDITION_WINDY) {
                        textCondition = "Windy";
                    } else if (cond == Weather.CONDITION_UNKNOWN) {
                        textCondition = "Unknown";
                    }
                    Log.d(TAG, "            " + textCondition + "\n");
                }

                gotWeather = true;
                checkStopSelf();
            }
        });
    }

    private void getHeadphone() {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient).setResultCallback(new ResultCallback<HeadphoneStateResult>() {
            @Override
            public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                if (headphoneStateResult.getStatus().isSuccess()) {
                    int state = headphoneStateResult.getHeadphoneState().getState();
                    if (state == HeadphoneState.PLUGGED_IN) {
                        childUpdates.put("headphone", true);
                        Log.d(TAG, "Headphone: plugged in");
                    } else if (state == HeadphoneState.UNPLUGGED){
                        childUpdates.put("headphone", false);
                        Log.d(TAG, "Headphone: unplugged");
                    }
                } else {
                    Log.d(TAG, "Headphone: unknown");
                }
                gotHeadphone = true;
                checkStopSelf();
            }
        });
    }

    private void getPlaces() throws SecurityException {
        Log.d(TAG, "getPlaces");
        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                Log.d(TAG, "gotPlaces");
                if (placesResult.getPlaceLikelihoods() != null && placesResult.getPlaceLikelihoods().size() > 0) {
                    Log.d(TAG, "places: " + placesResult.getPlaceLikelihoods().toString());
                }
                gotPlaces = true;
                checkStopSelf();
            }
        });
    }

    private void checkStopSelf() {
        if (gotPlaces && gotActivity && gotWeather && gotHeadphone) {
            update();
        }
    }

    private void update() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase
                    .getDatabase()
                    .getReference()
                    .child(Constants.USERS)
                    .child(user.getUid())
                    .updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "stopSelf");
                    stopSelf();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error updating real-time database");
                    stopSelf();
                }
            });
        }
    }
}