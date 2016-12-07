package co.gadder.gadder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import com.android.volley.Response;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GadderFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "Firebase Messaging";
    private static final Set<String> names = new HashSet<>();
    private static final int ACTIVITY_REQUEST_ID = 1;
    private static final int ACTIVITY_INPUT_REQUEST_CODE = 1;

    private NotificationManager mNotificationManger;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {                                    // Application on the foreground
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Data Payload: " + remoteMessage.getData());
        Map<String, String> data = remoteMessage.getData();
        if(data.containsKey("update") && Boolean.valueOf(data.get("update"))) {
            Log.d(TAG, "updateRequest");
            startService(new Intent(this, UserActivityService.class));
        }

        if (data.containsKey("notify") && Boolean.valueOf(data.get("notify"))) {
            notifyUser(data);
        }

        if (data.containsKey("removeNotification") && Boolean.valueOf(data.get("removeNotification"))) {
            removeNotification();
        }

        if(data.containsKey("wakeUp") && Boolean.valueOf(data.get("wakeUp"))) {
            wakePhoneUp();
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

    private void removeNotification() {
        getNotificationManager().cancel(ACTIVITY_REQUEST_ID);
    }

    private void notifyUser(Map<String, String> data) {
        Log.d(TAG, "notifyUser");
        final Notification.Builder builder = new Notification.Builder(getApplicationContext());

//        builder.setCustomContentView(new RemoteViews(getPackageName(), R.layout.notification_activity));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.setPriority(Notification.PRIORITY_MAX);
        }

        builder.setSmallIcon(R.drawable.ic_bubble_chart_white_48dp);
        builder.setContentTitle(getString(R.string.app_name));

        if (data.containsKey("name")) {
            String name = data.get("name");
            String message = name;

            if (names.size() > 2) { // latest_friend + rnd_friend + and + #other friends
                message += ", " + names.iterator().next() + " " + getString(R.string.and) + " " + (names.size()-1) +  getString(R.string.other_friends);
            } else if (names.size() > 1) { // latest_friend + and + single_friend
                message += " " + getString(R.string.and) + " " + names.iterator().next();
            }
            message += " " + getString(R.string.request_friend_activity_message);
            builder.setContentText(message);

            if (!names.contains(name)) {
                names.add(name);
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Intent resultIntent = new Intent(getApplicationContext(), InputActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
            stackBuilder.addParentStack(InputActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    ACTIVITY_INPUT_REQUEST_CODE,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);
        }

        if (data.containsKey("pictureUrl")) {

            Response.Listener<Bitmap> listener = new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    builder.setLargeIcon(Constants.getCircularBitmap(bitmap));

                    Notification notification;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        notification = builder.build();
                    } else {
                        notification = builder.getNotification();
                    }
                    getNotificationManager().notify(ACTIVITY_REQUEST_ID, notification);

                }
            };
            RequestManager.getInstance(getApplicationContext()).downloadImage(data.get("pictureUrl"), listener);
        } else {
            Notification notification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build();
            } else {
                notification = builder.getNotification();
            }
            getNotificationManager().notify(ACTIVITY_REQUEST_ID, notification);
        }

    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManger == null) {
            mNotificationManger =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManger;
    }

    private void wakePhoneUp() {
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
//            PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK),"gadder_wake_up");
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.FULL_WAKE_LOCK),"gadder_wake_up");
        wakeLock.acquire();
        Log.d(TAG, "waking device up");
    }
}
