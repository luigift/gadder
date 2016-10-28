package co.gadder.gadder;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.google.android.gms.common.api.BooleanResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class GadderFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Firebase Messaging";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {                                    // Application on the foreground
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Data Payload: " + remoteMessage.getData());
        Map<String, String> data = remoteMessage.getData();
        if(data.containsKey("update") && Boolean.valueOf(data.get("update"))) {
            Log.d(TAG, "UPDATE");
            startService(new Intent(this, UserActivityService.class));
        } else if(data.containsKey("wakeUp") && Boolean.valueOf(data.get("wakeUp"))) {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
//            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK),"gadder_wake_up");
            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK),"gadder_wake_up");
            wakeLock.acquire();
            Log.d(TAG, "waking device up");
        }
    }

    @Override
    public void onMessageSent(String s) {
        Log.d(TAG, "Message Sent");
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Log.d(TAG, "Message Error");
    }
}
