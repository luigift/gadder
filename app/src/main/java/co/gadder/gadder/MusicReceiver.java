package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MusicReceiver extends BroadcastReceiver {
    private final static String TAG = "MusicReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String cmd = intent.getStringExtra("command");
        Log.v(TAG, action + " / " + cmd);
        String artist = intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        Log.v(TAG, artist + ":" + album + ":" + track);
        Toast.makeText(context, track, Toast.LENGTH_SHORT).show();
    }
}
