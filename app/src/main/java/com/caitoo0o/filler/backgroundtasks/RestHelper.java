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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by caitoo0o on 7/2/2017.
 */

public class RestHelper {

    public static void recordNewUser(Context appContext, FirebaseUser user,
            String fullName, String username, String birthdayTimestamp,
            HttpCallbacks callbacks) {

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("user_id", user.getUid());
        params.put("email", user.getEmail());
        params.put("full_name", fullName);
        params.put("username", username);
        params.put("birthday_timestamp_str", birthdayTimestamp);

        getData(appContext, "usermetadata", "record_new_user", params, callbacks);
    }

    public static void userRecordedStatus(Context appContext, FirebaseUser user,
                                     HttpCallbacks callbacks) {

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("user_id", user.getUid());
        getData(appContext, "usermetadata", "user_recorded_status", params, callbacks);
    }

    public static void recordUpload(Context appContext, FirebaseUser user, String downloadUrl, String filename, Privacy privacy, String comment, HttpCallbacks callbacks) {

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("user_id", user.getUid());
        params.put("download_url", downloadUrl);
        params.put("filename", filename);
        params.put("privacy", privacy.toString());
        params.put("comment", comment);

        getData(appContext, "mediametadata", "record_upload", params, callbacks);
    }

    public static void myImages(Context appContext, FirebaseUser user, HttpCallbacks callbacks) {

//        Map<String, String>  params = new HashMap<String, String>();
//        params.put("key", getApiKey());
//        params.put("user_id", user.getUid());
//        //params.put("user_id", "Xq7yUgPOVFXpTNmbMT4TcPuC93r2");
//
//        getData(appContext, "my_images", params, callbacks);

        Map<String, String>  params = new HashMap<String, String>();
        params.put("key", getApiKey());
        params.put("timestamp", "" + System.currentTimeMillis());
        //params.put("user_id", "Xq7yUgPOVFXpTNmbMT4TcPuC93r2");

        getData(appContext, "mediametadata", "date_images", params, callbacks);
    }

    public interface HttpCallbacks {
        void onSuccess(String response);
        void onFailure(String error);
    }

    private static String getProjectId() {
        return "filler-ea8ae";
    }

    private static String getApiKey() {
        return "AIzaSyC2F1FVP1yAqkRJomg8hoL1GR614low1AE";
    }

    private static String getEndpointUrl(String api, String endpointName) {
        return "https://" + getProjectId() + ".appspot.com/_ah/api/" + api + "/v1/" + endpointName;
    }

    private static void getData(
                                Context appContext,
                                final String api,
                                final String endpointName,
                                final Map<String, String> params,
                                final HttpCallbacks callbacks) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(appContext);
        String url = getEndpointUrl(api, endpointName);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(RestHelper.class.getName(), "Response is: " + response);
                        callbacks.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String failureMessage = "NONE";
                        if (error == null) {
                            Log.i(RestHelper.class.getName(), "error is null when calling endpoint " + endpointName);
                            failureMessage = "error is null";
                        } else if (error.networkResponse == null) {
                            Log.i(RestHelper.class.getName(), "error.networkResponse is null when calling endpoint " + endpointName);
                            failureMessage = "error.networkResponse is null. error.getMessage: " + error.getMessage();
                        } else if (error.getMessage() == null) {
                            Log.i(RestHelper.class.getName(), "error.getMessage() returns null when calling endpoint " + endpointName);
                            failureMessage = "error message is null. statusCode is: " + error.networkResponse.statusCode;
                        } else {
                            failureMessage = error.getMessage();
                        }
                        Log.i(RestHelper.class.getName(), "Error when calling endpoint " + endpointName + ": " + failureMessage);
                        // TODO: Figure out why in the world networkResponse is null and come back to this
                        callbacks.onFailure(failureMessage);
                        callbacks.onSuccess("{}");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // End network calls
}
