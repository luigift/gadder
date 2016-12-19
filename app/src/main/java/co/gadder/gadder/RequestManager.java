package co.gadder.gadder;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.gadder.gadder.emoji.Nature;

import static co.gadder.gadder.GadderFirebaseMessagingService.FRIENDSHIP;
import static co.gadder.gadder.GadderFirebaseMessagingService.NAME;
import static co.gadder.gadder.GadderFirebaseMessagingService.NOTIFY;
import static co.gadder.gadder.GadderFirebaseMessagingService.PICTURE_URL;
import static co.gadder.gadder.GadderFirebaseMessagingService.UPDATE;

public class RequestManager {

    private static final String TAG = RequestManager.class.getSimpleName();

    private static final String SENDER_ID = "254610893779";
    private static final String SERVER_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String API_KEY = "AIzaSyDHV8lCJNf8VUIOx2L8KCI8zDYhlXinB70";

    private static RequestManager mInstance;
    private static RequestQueue mRequestQueue;

    private RequestManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static RequestManager getInstance(Context context) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getInstance");

        if (mInstance == null) {
            mInstance = new RequestManager(context);
        }
        return mInstance;
    }

    public void downloadImage(String url, Response.Listener<Bitmap> listener) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "downloadImage");

        ImageRequest imgRequest = new ImageRequest(url, listener, 200, 200, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do stuff
                    }
                });
        mRequestQueue.add(imgRequest);
    }

    public void sendFriendshipRequestNotification(String token, String name, String pictureUrl) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendFriendshipRequestNotification");

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {

            data.put(FRIENDSHIP, "true");
            data.put(NAME, name);
            data.put(PICTURE_URL, pictureUrl);

            json.put("data", data);
            json.put("to", token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(json);
    }

    public void sendUpdateRequest(String token) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendUpdateRequest");

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {

            data.put(NOTIFY, "true");
            data.put(UPDATE, "true");

            json.put("data", data);
            json.put("to", token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(json);
    }

    public void sendUpdateRequest(String token, String name, String pictureUrl) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendUpdateRequest");

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {

            data.put(NOTIFY, "false"); // TODO change to true
            data.put(UPDATE, "true");
            data.put(NAME, name);
            data.put(PICTURE_URL, pictureUrl);

            json.put("data", data);
            json.put("to", token);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(json);
    }

    public void sendAllUpdatesRequest(List<String> tokens) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendAllUpdatesRequest");
        for (String t :tokens) {
            JSONObject data = new JSONObject();
            JSONObject json = new JSONObject();
            try {

                data.put(NOTIFY, "true");
                data.put(UPDATE, "true");

                json.put("data", data);
                json.put("to", t);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendRequest(json);
        }
    }

    public void sendRemoveNotificationRequest(Set<String> tokens) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendRemoveNotificationRequest");
        for (String t :tokens) {
            JSONObject data = new JSONObject();
            JSONObject json = new JSONObject();
            try {
                data.put("removeNotification", "true");
                data.put("notify", "false");
                data.put("update", "false");
                json.put("data", data);
                json.put("to", t);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendRequest(json);
        }
    }

    private void sendRequest(JSONObject json) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "sendRequest");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SERVER_URL, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "onResponse");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                FirebaseCrash.logcat(Log.DEBUG, TAG, "onErrorResponse" + error);
                FirebaseCrash.report(error.getCause());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "key=" + API_KEY);
                return header;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        mRequestQueue.add(request);
    }


    public void cancelAllRequests() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "cancelAllRequests");
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }
}
