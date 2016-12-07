package co.gadder.gadder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MusicReceiver extends BroadcastReceiver {
    private final static String TAG = "MusicReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Boolean playing = intent.getBooleanExtra("playing", false);

        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        String artist = intent.getStringExtra("artist");
        String song = artist + ":" + album + ":" + track;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("song", song);
            childUpdates.put("playing", playing);
            FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(Constants.VERSION)
                    .child(Constants.USERS)
                    .child(user.getUid())
                    .child(Constants.MUSIC)
                    .updateChildren(childUpdates);
        }
    }
}
