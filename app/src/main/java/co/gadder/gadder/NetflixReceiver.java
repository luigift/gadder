package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetflixReceiver extends BroadcastReceiver {
    private final static String TAG = "NetflixReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "action: " + intent.getAction().toString());
        Log.d(TAG, "extras: " + intent.getExtras().toString());
    }
}
