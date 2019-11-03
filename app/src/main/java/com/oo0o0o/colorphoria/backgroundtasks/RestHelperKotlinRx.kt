package com.oo0o0o.colorphoria.backgroundtasks


import android.content.Context
import android.util.Log

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.oo0o0o.colorphoria.R
import com.oo0o0o.colorphoria.data.Privacy
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.loopj.android.http.*

import java.io.UnsupportedEncodingException
import java.util.Date
import java.util.HashMap
import java.util.concurrent.ExecutionException

import cz.msebera.android.httpclient.Header
import io.reactivex.*
import io.reactivex.schedulers.Schedulers
import java.nio.charset.Charset

/**
 * Created by oo0o0o on 10/27/2019.
 */

object RestHelperKotlinRx {

//    fun recordNewUser(appContext: Context, user: FirebaseUser,
//                      fullName: String, username: String, birthdayTimestamp: String,
//                      callbacks: HttpCallbacks) {
//
//        val params = RequestParams()
//        params.add("key", getApiKey(appContext))
//        params.add("email", user.email)
//        params.add("full_name", fullName)
//        params.add("username", username)
//        params.add("birthday_timestamp_str", birthdayTimestamp)
//
//        postDataWrapper(appContext, user, "mobile", "record_new_user", params, callbacks)
//    }
//
//    fun userRecordedStatus(appContext: Context, user: FirebaseUser,
//                           callbacks: HttpCallbacks) {
//
//        val params = RequestParams()
//        params.add("key", getApiKey(appContext))
//        postDataWrapper(appContext, user, "mobile", "user_recorded_status", params, callbacks)
//    }
//
//    fun recordUpload(appContext: Context, user: FirebaseUser, downloadUrl: String, filename: String, privacy: Privacy, comment: String, callbacks: HttpCallbacks) {
//
//        val params = RequestParams()
//        params.add("key", getApiKey(appContext))
//        params.add("download_url", downloadUrl)
//        params.add("filename", filename)
//        params.add("privacy", privacy.toString())
//        params.add("comment", comment)
//
//        postDataWrapper(appContext, user, "mobile", "record_upload", params, callbacks)
//    }

    fun recentImages(appContext: Context, user: FirebaseUser): Single<String> {
        val params = RequestParams()
        params.add("key", getApiKey(appContext))
        params.add("timestamp", "" + System.currentTimeMillis())

        return postDataWrapper(appContext, user, "mobile", "date_image_metadata", params)
    }

//    fun myImages(appContext: Context, user: FirebaseUser, callbacks: HttpCallbacks) {
//
//        val params = RequestParams()
//        params.add("key", getApiKey(appContext))
//        params.add("timestamp", "" + System.currentTimeMillis())
//
//        postDataWrapper(appContext, user, "mobile", "my_image_metadata", params, callbacks)
//    }

    interface HttpCallbacks {
        fun onSuccess(response: String?)
        fun onFailure(error: String)
    }

    private fun getProjectId(context: Context): String {
        return context.resources.getString(R.string.gcp_project_id)
    }

    private fun getApiKey(context: Context): String {
        return context.resources.getString(R.string.gcp_api_key)
    }

    private fun getEndpointUrl(context: Context, api: String, endpointName: String): String {
        return "https://" + getProjectId(context) + ".appspot.com/_ah/api/" + api + "/v1/" + endpointName
    }

    private fun postDataWrapper(
            context: Context,
            user: FirebaseUser,
            api: String,
            endpointName: String,
            requestParams: RequestParams): Single<String> {
        return Single.create<String> { emitter ->
            user.getIdToken(true)
                    .addOnSuccessListener { getTokenResult ->
                        getTokenResult.token?.let { token ->
                            emitter.onSuccess(token)
                        } ?: run {
                            emitter.onError(Throwable("Empty auth token"))
                        }
                    }
                    .addOnFailureListener { e ->
                        emitter.onError(Throwable("Failed to get token from Firebase", e))
                    }
        }.flatMap { token ->
            return@flatMap postData(context, token, api, endpointName, requestParams)
        }
    }

    private fun postData(
            context: Context,
            authToken: String?,
            api: String,
            endpointName: String,
            params: RequestParams): Single<String> {

        // Create Completable that completes or errors on endpoint call success or failure
        return Single.create<String> { emitter ->

            val url = getEndpointUrl(context, api, endpointName)

            val apiClient = SyncHttpClient()
            apiClient.setTimeout(60 * 1000) // 60 s
            apiClient.addHeader("Authorization", "Bearer " + authToken!!)
            apiClient.post(url, params, object : TextHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: String) {

                    Log.i(RestHelper::class.java.name, "Response is: " + responseBody)
                    emitter.onSuccess(responseBody)
                }

                override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: String,
                                       error: Throwable) {
                    Log.i(RestHelper::class.java.name, "Error when calling endpoint " + endpointName + ": " + error.message)
                    emitter.onError(error)
                }
            })
        }.subscribeOn(Schedulers.io())
    }

    // End network calls
}
