package co.gadder.gadder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.content.Intent;
import android.app.IntentService;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.Geofence;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static final String TAG = "GeofenceTransitionsIS";

    protected DatabaseReference mDatabase;

    protected Location mLastLocation;
    protected GoogleApiClient mGoogleApiClient;

    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Handling intent");

        // Get receiver from Geofencing service
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    geofencingEvent.getErrorCode());
//            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
             mDatabase =
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(user.getUid());
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {                         // Friend possible encounter
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                for (Geofence geofence : triggeringGeofences) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Enter: " + geofence.getRequestId());
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {                   // Update user's geofence
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                for (Geofence geofence : triggeringGeofences) {
                    if (geofence.getRequestId().equals(user.getUid())) {                            // User left geofence, create new one
                        updateUserGeofence();
                    }
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Exit: " + geofence.getRequestId());
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {                  // Friend probable encounter
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                for (Geofence geofence : triggeringGeofences) {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "Dwell: " + geofence.getRequestId());
                }
            }
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    public void updateUserGeofence() {
        // get current location
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(GeofenceTransitionsIntentService.this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat
                .checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Map<String, Object>  childUpdates = new HashMap<>();
            childUpdates.put("latitude", mLastLocation.getLatitude());
            childUpdates.put("longitude", mLastLocation.getLongitude());
            mDatabase.child("coordinates").updateChildren(childUpdates);
        }
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}