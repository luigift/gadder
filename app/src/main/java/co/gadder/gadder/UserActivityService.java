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
import android.os.Handler;
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
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class UserActivityService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final static String TAG = "UserActivityService";
    protected GoogleApiClient mGoogleApiClient;

    // Firebase
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;

    private Location location;

    private Boolean gotTime = false;
    private Boolean gotCity = false;
    private Boolean gotPlaces = false;
    private Boolean gotBattery = false;
    private Boolean gotWeather = false;
    private Boolean gotActivity = false;
    private Boolean gotHeadphone = false;

    private Intent batteryStatus;

    private Calendar calendar;

    Map<String, Object> childUpdates = new HashMap<>();

    public UserActivityService() {
    }

    @Override
    public void onCreate() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        super.onCreate();

        // Init firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buildGoogleApiClient();

        calendar = Calendar.getInstance();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onStartCommand");
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDestroy");
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onBind");
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
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Ask for location permission");
        } else {
            getTime();
            getLocation();
            getCity();
            getBatteryLevel();
//            getPlaces();                                                                            // Get places
            getDetectedActivity();                                                                  // Get user activity through Awareness API
            getWeather();
            getHeadphone();

            // safety stopSelf()
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopSelf();
                }
            }, 20000);
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
        if (location != null) {
            childUpdates.put("coordinates/latitude", location.getLatitude());
            childUpdates.put("coordinates/longitude", location.getLongitude());
        } else {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Turn on location");
        }
    }

    private void getCity() {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        if (location != null) {
            try {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    String city = addresses.get(0).getLocality();
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "city: " + city);
                    childUpdates.put("city", city);
                    gotCity = true;
                    checkStopSelf();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        FirebaseCrash.logcat(Log.DEBUG, TAG, "battery: " + (level*100)/ 100);
        childUpdates.put("battery", (level * 100)/100);
        gotBattery = true;
        checkStopSelf();
    }

    private void getDetectedActivity() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getDetectedActivity");
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
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "activityCheckStop");
                    }
                });
    }

    private void getWeather() throws  SecurityException {
        Awareness.SnapshotApi.getWeather(mGoogleApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                Weather weather = weatherResult.getWeather();

                if (weather != null) {
                    int temperature = Math.round(weather.getTemperature(Weather.CELSIUS));
                    int[] conditions = weather.getConditions();

                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Temperature: " + temperature);
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Conditions: " + "\n");
                    String textCondition = "";

                    List<String> condition = new ArrayList<String>();
                    for (int cond : conditions) {
                        if (cond == Weather.CONDITION_CLEAR) {
                            textCondition = Constants.CLEAR;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_FOGGY) {
                            textCondition = Constants.FOGGY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_CLOUDY) {
                            textCondition = Constants.CLOUDY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_HAZY) {
                            textCondition = Constants.HAZY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_ICY) {
                            textCondition = Constants.ICY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_RAINY) {
                            textCondition = Constants.RAINY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_SNOWY) {
                            textCondition = Constants.SNOWY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_STORMY) {
                            textCondition = Constants.STORMY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_WINDY) {
                            textCondition = Constants.WINDY;
                            condition.add(textCondition);
                        } else if (cond == Weather.CONDITION_UNKNOWN) {
                            textCondition = Constants.UNKNOWN;
                            condition.add(textCondition);
                        }
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "            " + textCondition + "\n");
                    }

                    childUpdates.put("weather", condition);
                    childUpdates.put("temperature", temperature);

                    gotWeather = true;
                    checkStopSelf();
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "weatherCheckStop");
                }
            }
        });
    }

    private void getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
        String date = sdf.format(calendar.getTime());
        FirebaseCrash.logcat(Log.DEBUG, TAG, "time: " + date);
        childUpdates.put("lastUpdate", date); //DateFormat.getDateTimeInstance().format(new Date()));//calendar.getTime());
        gotTime = true;
        checkStopSelf();
        FirebaseCrash.logcat(Log.DEBUG, TAG, "timeCheckStop");
    }

    private void getHeadphone() {
        Awareness.SnapshotApi.getHeadphoneState(mGoogleApiClient).setResultCallback(new ResultCallback<HeadphoneStateResult>() {
            @Override
            public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                if (headphoneStateResult.getStatus().isSuccess()) {
                    int state = headphoneStateResult.getHeadphoneState().getState();
                    if (state == HeadphoneState.PLUGGED_IN) {
                        childUpdates.put("headphone", true);
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Headphone: plugged in");
                    } else if (state == HeadphoneState.UNPLUGGED){
                        childUpdates.put("headphone", false);
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "Headphone: unplugged");
                    }
                } else {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Headphone: unknown");
                }
                gotHeadphone = true;
                checkStopSelf();
                FirebaseCrash.logcat(Log.DEBUG, TAG, "HeadphoneCheckStop");
            }
        });
    }

    private void getPlaces() throws SecurityException {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getPlaces");
        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "gotPlaces");
                if (placesResult.getPlaceLikelihoods() != null && placesResult.getPlaceLikelihoods().size() > 0) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "places: " + placesResult.getPlaceLikelihoods().toString());
                }
                gotPlaces = true;
                checkStopSelf();
            }
        });
    }

    private void checkStopSelf() {
        if (gotCity && gotActivity && gotWeather && gotHeadphone && gotTime && gotBattery) {
            update();
        }
    }

    private void update() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "update");
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase
                    .child(Constants.VERSION)
                    .child(Constants.USERS)
                    .child(user.getUid())
                    .updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "stopSelf");
                    stopSelf();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error updating real-time database");
                    stopSelf();
                }
            });
        } else {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "user null");
        }
    }
}
