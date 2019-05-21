package com.oo0o0o.colorphoria.backgroundtasks;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.Task;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.data.Privacy;
import com.oo0o0o.colorphoria.activities.loggedinactivities.UploadActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Service to handle uploading files to Firebase Storage.
 */
public class MyUploadService extends MyBaseTaskService {

    private static final String TAG = "MyUploadService";

    /** Intent Actions **/
    public static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";
    public static final String UPLOAD_ADULT_CONTENT_ERROR = "upload_adult_content_error";

    /** Intent Extras **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_COMMENT = "comment";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    // [START declare_ref]
    private StorageReference mStorageRef;
    // [END declare_ref]

    @Override
    public void onCreate() {
        super.onCreate();

        // [START get_storage_ref]
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // [END get_storage_ref]
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
            String comment = intent.getStringExtra(EXTRA_COMMENT);
            uploadFromUri(fileUri, comment);
        }

        return START_REDELIVER_INTENT;
    }

    // [START upload_from_uri]
    private void uploadFromUri(final Uri fileUri, final String comment) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START_EXCLUDE]
        taskStarted();
        // [END_EXCLUDE]

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = mStorageRef.child("photos")
                .child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        // Use this in future to indicate progress if desired
//                        showProgress(getString(R.string.progress_uploading),
//                                taskSnapshot.getBytesTransferred(),
//                                taskSnapshot.getTotalByteCount());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL

                        final Task<Uri> downloadUriTask = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                        downloadUriTask.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri downloadUri) {

                                // TODO : Should this success callback come from the higher level?
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                RestHelper.HttpCallbacks callbacks = new RestHelper.HttpCallbacks() {
                                    public void onSuccess(String response) {
                                        try {
                                            JSONObject responseJson = new JSONObject(response);
                                            if (responseJson.has("message")) {
                                                String message = responseJson.getString("message");
                                                if (message.equals("adult_content")) {
                                                    broadcastUploadFinished(null, fileUri, true);
                                                    taskCompleted();
                                                } else {
                                                    broadcastUploadFinished(downloadUri, fileUri, false);
                                                    taskCompleted();
                                                }
                                            } else {
                                                // Shouldn't ever get here but if it happens count it as a failure
                                                // by passing null downloadUri
                                                broadcastUploadFinished(null, fileUri, false);
                                                taskCompleted();
                                            }
                                        } catch (JSONException e) {
                                            // Shouldn't ever get here but if it happens count it as a failure
                                            // by passing null downloadUri
                                            broadcastUploadFinished(null, fileUri, false);
                                            taskCompleted();
                                        }
                                    }
                                    public void onFailure(String error) {
                                        // TODO: If record upload fails u need to retry or notify the user
                                        broadcastUploadFinished(null, fileUri, false);
                                        taskCompleted();
                                    }
                                };
                                RestHelper.recordUpload(getApplicationContext(), user, downloadUri.toString(), fileUri.getLastPathSegment(), Privacy.EVERYONE, comment, callbacks);
                            }
                        });

                        downloadUriTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Count this as a failure by passing null downloadUri
                                broadcastUploadFinished(null, fileUri, false);
                                taskCompleted();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, fileUri, false);
                        taskCompleted();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END upload_from_uri]

    /**
     * Broadcast finished upload (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri, boolean adultContent) {
        String action;
        if (downloadUrl != null) {
            action = UPLOAD_COMPLETED;
        } else if (adultContent) {
            action = UPLOAD_ADULT_CONTENT_ERROR;
        } else {
            action = UPLOAD_ERROR;
        }

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);
        filter.addAction(UPLOAD_ADULT_CONTENT_ERROR);

        return filter;
    }

}
