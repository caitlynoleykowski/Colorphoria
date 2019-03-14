/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.backgroundtasks.MyUploadService;
import com.oo0o0o.colorphoria.effecthelpers.BitmapLoaderAsyncTask;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity to upload and download photos from Firebase Storage.
 *
 * See {@link MyUploadService} for upload example.
 */
public class UploadActivity extends LoggedInActivity {

    private static final String TAG = UploadActivity.class.getName();

    public static final String IMAGE_URI = "IMAGE_URI";

    public static final int REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE = 1;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
            ImageView iv = (ImageView) findViewById(R.id.upload_image_view);
            new BitmapLoaderAsyncTask(this, iv, mFileUri).execute();
        } else if (getIntent().hasExtra(IMAGE_URI)) {
            mFileUri = (Uri)getIntent().getParcelableExtra(IMAGE_URI);
            ImageView iv = (ImageView) findViewById(R.id.upload_image_view);
            new BitmapLoaderAsyncTask(this, iv, mFileUri).execute();
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressBar();

                switch (intent.getAction()) {
                    case MyUploadService.UPLOAD_COMPLETED:
                        onUploadResultIntent(intent);
                        startPicSourceActivity();
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                    case MyUploadService.UPLOAD_ADULT_CONTENT_ERROR:
                        onUploadResultIntent(intent);
                        Utils.showAlertDialog(
                                UploadActivity.this,
                                getString(R.string.upload_adult_content_error),
                                getString(R.string.ok),
                                new Utils.AlertDialogHandler() {
                                    @Override
                                    public void onPositiveButton(Dialog confirmDialog) {
                                        confirmDialog.dismiss();
                                    }
                                },
                                false
                        );
                        break;
                }
            }
        };
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
        super.onSaveInstanceState(out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.upload_menu_share:
                onInternalShare(((EditText)findViewById(R.id.upload_comment_edit_text)).getText().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadFromUri(Uri fileUri, String comment) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Clear the last download, if any
        updateUI(mAuth.getCurrentUser());
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .putExtra(MyUploadService.EXTRA_COMMENT, comment)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        showProgressBar();
    }

    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);

        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        // Signed in or Signed out
        if (user != null) {
            findViewById(R.id.layout_storage).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_storage).setVisibility(View.GONE);
        }
    }

    private void startPicSourceActivity() {
        Intent intent = new Intent(this, ImageFeedActivity.class);
        startActivity(intent);
    }

    public void onShare(View v) {
        shareCurrImageStoragePermission();
    }

    private void shareCurrImageStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        Utils.showCustomToast(UploadActivity.this, getString(R.string.unable_to_save_image));
                    }
                })) {
            shareCurrImage();
        }
    }

    private void shareCurrImage() {
        ImageView iv = (ImageView) findViewById(R.id.upload_image_view);
        Bitmap bm =((BitmapDrawable)iv.getDrawable()).getBitmap();
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "Export from colorphoria", null);
        Uri uri = Uri.parse(path);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CALLBACK_SHARE_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    shareCurrImage();
                } else {
                    Utils.showCustomToast(UploadActivity.this, getString(R.string.unable_to_share_image));
                }
                return;
            }
        }
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    public void onInternalShare(String comment) {
        if (mFileUri != null) {
            uploadFromUri(mFileUri, comment != null? comment : "");
        } else {
            Log.w(TAG, "File URI is null");
        }
    }
}
