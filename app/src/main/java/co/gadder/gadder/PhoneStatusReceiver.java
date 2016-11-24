package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PhoneStatusReceiver extends BroadcastReceiver {
    private final static String TAG = "PhoneStatusReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            Log.d(TAG, "Shutdown");
        } else if(intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)){
            Log.d(TAG, "Localed changed");
        } else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.d(TAG, "User present");
        } else if(intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            Log.d(TAG, "Calling");
        }
    }
}
