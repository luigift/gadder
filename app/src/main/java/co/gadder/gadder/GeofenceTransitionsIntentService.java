package co.gadder.gadder;

import java.util.List;

import android.util.Log;
import android.content.Intent;
import android.app.IntentService;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.Geofence;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";

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
        if(user != null) {
            DatabaseReference ref =
                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(user.getUid())
                            .child("encounter");
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {                         // Friend possible encounter

                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                for (Geofence geofence : triggeringGeofences) {
                    Log.d(TAG,"Enter: " + geofence.getRequestId());
                    ref.setValue(true);
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {                   // Update user's geofence

                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                for (Geofence geofence : triggeringGeofences) {
                    if (geofence.getRequestId().equals(user.getUid())) {                            // User left geofence, create new one

                    }
                    Log.d(TAG,"Exit: " + geofence.getRequestId());
                    ref.setValue(false);
                }
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {                  // Friend probable encounter
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                for (Geofence geofence : triggeringGeofences) {
                    Log.d(TAG, "Dwell: " + geofence.getRequestId());
                }
            }
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }
}