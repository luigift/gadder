package co.gadder.gadder;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.IOException;
import java.util.HashMap;


public class CreateUserService extends Service {

    private final static String TAG = "CreateUserService";

    static int delay = 0;
    static HashMap<String, Object> userInfo;

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @SuppressWarnings("unchecked")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            userInfo = (HashMap<String, Object>) intent.getSerializableExtra(Constants.USER_INFO);
            createUser();
        } catch (ClassCastException e) {
            FirebaseCrash.report(e);
        }

        return START_STICKY;
    }

    /*
    *   Try creating user. Give up after 100000s = 27h
    * */
    private void createUser() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "createUser: " + delay);
        FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.VERSION)
                .updateChildren(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        stopSelf();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (delay < 100000) {
                            delay += 3000;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    createUser();
                                }
                            }, delay);
                        } else {
                            stopSelf();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
