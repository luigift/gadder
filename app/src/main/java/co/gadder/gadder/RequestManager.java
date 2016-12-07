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
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (mInstance == null) {
            mInstance = new RequestManager(context);
        }
        return mInstance;
    }

    public void downloadImage(String url, Response.Listener<Bitmap> listener) {
        ImageRequest imgRequest = new ImageRequest(url, listener, 200, 200, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do stuff
                    }
                });
        mRequestQueue.add(imgRequest);
    }

    public void sendUpdateRequest(String token) {

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            data.put("notify", "true");
            data.put("update", "true");
            json.put("data", data);
            json.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(json);
    }
    public void sendUpdateRequest(String token, String name, String pictureUrl) {

        JSONObject data = new JSONObject();
        JSONObject json = new JSONObject();
        try {
            data.put("notify", "false"); // TODO change to true
            data.put("update", "true");
            data.put("name", name);
            data.put("pictureUrl", pictureUrl);
            json.put("data", data);
            json.put("to", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(json);
    }

    public void sendAllUpdateRequest(List<String> tokens) {

        for (String t :tokens) {
            JSONObject data = new JSONObject();
            JSONObject json = new JSONObject();
            try {
                data.put("notify", "true");
                data.put("update", "true");
                json.put("data", data);
                json.put("to", t);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendRequest(json);
        }
    }

    public void sendRemoveNotificationRequest(Set<String> tokens) {
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
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, SERVER_URL, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error: " + error);
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

        Log.d(TAG, "body: " + request .getBody().toString());
        Log.d(TAG, "content: " + request.getBodyContentType());
        try {
            Log.d(TAG, "headers: " + request.getHeaders().toString());
        } catch (AuthFailureError authFailureError) {
            Log.d(TAG, "AUTH ERROR");
            authFailureError.printStackTrace();
        }
        Log.d(TAG, request.toString());
        mRequestQueue.add(request);
    }

}
