package com.caitoo0o.filler.backgroundtasks;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.data.Privacy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by caitoo0o on 7/2/2017.
 */

public class RestHelper {

    public static void recordNewUser(Context appContext, FirebaseUser user,
            String fullName, String username, String birthdayTimestamp,
            HttpCallbacks callbacks) {

        RequestParams  params = new RequestParams();
        params.add("key", getApiKey());
        params.add("email", user.getEmail());
        params.add("full_name", fullName);
        params.add("username", username);
        params.add("birthday_timestamp_str", birthdayTimestamp);

        postDataWrapper(appContext, user, "mobile", "record_new_user", params, callbacks);
    }

    public static void userRecordedStatus(Context appContext, FirebaseUser user,
                                     HttpCallbacks callbacks) {

        RequestParams  params = new RequestParams();
        params.add("key", getApiKey());
        postDataWrapper(appContext, user, "mobile", "user_recorded_status", params, callbacks);
    }

    public static void recordUpload(Context appContext, FirebaseUser user, String downloadUrl, String filename, Privacy privacy, String comment, HttpCallbacks callbacks) {

        RequestParams  params = new RequestParams();
        params.add("key", getApiKey());
        params.add("download_url", downloadUrl);
        params.add("filename", filename);
        params.add("privacy", privacy.toString());
        params.add("comment", comment);

        postDataWrapper(appContext, user, "mobile", "record_upload", params, callbacks);
    }

    public static void myImages(Context appContext, FirebaseUser user, HttpCallbacks callbacks) {

//        Map<String, String>  params = new HashMap<String, String>();
//        params.put("key", getApiKey());
//        params.put("user_id", user.getUid());
//        //params.put("user_id", "Xq7yUgPOVFXpTNmbMT4TcPuC93r2");
//
//        getData(appContext, "my_images", params, callbacks);

        RequestParams  params = new RequestParams();
        params.add("key", getApiKey());
        params.add("timestamp", "" + System.currentTimeMillis());

        postDataWrapper(appContext, user, "mobile", "date_images", params, callbacks);
    }

//    public static void firebaseEndpointAuthTest(Context appContext, FirebaseUser user, HttpCallbacks callbacks) {
//        Map<String, String>  params = new HashMap<String, String>();
//        params.put("key", getApiKey());
//        getData(appContext, user, Request.Method.GET, "mobile", "firebase_user", params, callbacks);
//    }

    public static void firebaseEndpointAuthTest(final Context appContext, final FirebaseUser user, final HttpCallbacks callbacks) {
        RequestParams  params = new RequestParams();
        params.add("key", getApiKey());
        postDataWrapper(appContext, user, "mobile", "firebase_user2", params, callbacks);
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

    private static void postDataWrapper(
            final Context context,
            final FirebaseUser user,
            final String api,
            final String endpointName,
            final RequestParams requestParams,
            final HttpCallbacks callbacks) {
        user.getToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                postData(context, user, getTokenResult.getToken(), api, endpointName, requestParams, callbacks);            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to get token from Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void getData(
            final Context context,
            final FirebaseUser user,
            final String authToken,
            final String api,
            final String endpointName,
            final HttpCallbacks callbacks) {
        String url = getEndpointUrl(api, endpointName);

        AsyncHttpClient apiClient = new AsyncHttpClient();
        apiClient.addHeader("Authorization", "Bearer " + authToken);
        apiClient.get(context, url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = null;
                try {
                    response = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    response = e.getMessage();
                }
                callbacks.onSuccess(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
                callbacks.onFailure(error.getMessage());
            }
        });
    }

    private static void postData(
            final Context context,
            final FirebaseUser user,
            final String authToken,
            final String api,
            final String endpointName,
            final RequestParams params,
            final HttpCallbacks callbacks) {
        String url = getEndpointUrl(api, endpointName);

        AsyncHttpClient apiClient = new AsyncHttpClient();
        apiClient.addHeader("Authorization", "Bearer " + authToken);
        apiClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = null;
                try {
                    response = new String(responseBody, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    response = e.getMessage();
                }
                Log.i(RestHelper.class.getName(), "Response is: " + response);
                callbacks.onSuccess(response);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
                Log.i(RestHelper.class.getName(), "Error when calling endpoint " + endpointName + ": " + error.getMessage());
                callbacks.onFailure(error.getMessage());
            }
        });
    }

    // End network calls
}
