package com.caitoo0o.filler.backgroundtasks;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caitoo0o.filler.data.Privacy;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by caitoo0o on 7/2/2017.
 */

public class RestHelper {

    public static void recordUpload(Context appContext, FirebaseUser user, String downloadUrl, Privacy privacy, String comment, HttpCallbacks callbacks) {

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("user_id", user.getUid());
        params.put("download_url", downloadUrl);
        params.put("privacy", privacy.toString());
        params.put("comment", comment);

        getData(appContext, "record_upload", params, callbacks);
    }

    public static void myImages(Context appContext, FirebaseUser user, HttpCallbacks callbacks) {

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("user_id", user.getUid());
        //params.put("user_id", "Xq7yUgPOVFXpTNmbMT4TcPuC93r2");

        getData(appContext, "my_images", params, callbacks);
    }

    public interface HttpCallbacks {
        void onSuccess(String response);
        void onFailure(String error);
    }

    private static String getProjectId() {
        return "fir-storage-bed15";
    }

    private static String getApiKey() {
        return "AIzaSyDdvcYtIkdlaFqbIXXm--l04ZOPXVLKatE";
    }

    private static void getData(Context appContext, final String endpoint, final Map<String, String> params, final HttpCallbacks callbacks) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(appContext);
        String url = "https://" + getProjectId() + ".appspot.com/_ah/api/echo/v1/" + endpoint;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(RestHelper.class.getName(), "Response is: " +
                                response);
                        callbacks.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(RestHelper.class.getName(), "Error when calling endpoint " + endpoint + ": " + error.getMessage());
                callbacks.onFailure(error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };;
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // End network calls
}
