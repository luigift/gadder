package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

public class PhoneStatusReceiver extends BroadcastReceiver {
    private final static String TAG = "PhoneStatusReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Shutdown");
        } else if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)){
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Localed changed");
        } else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "User present");
        } else if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Calling");
        }
    }
}
