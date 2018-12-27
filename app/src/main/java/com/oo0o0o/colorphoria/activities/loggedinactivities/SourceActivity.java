package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.utils.Utils;

import java.io.File;

public class SourceActivity extends LoggedInActivity {

    private static final String TAG = SourceActivity.class.getName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA = 0;
    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE = 2;

    private FirebaseAnalytics firebaseAnalytics;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_24dp);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, SourceActivity.class.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            mImageUri = null;
            Log.e(TAG, "No Image Selected.");
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            getContentResolver().notifyChange(mImageUri, null);
            startPicEditActivity(mImageUri);
        }
    }

    private void startPicEditActivity(Uri uri) {
        if (uri != null) {
            Intent intent = new Intent(this, PicEditActivity.class);
            intent.putExtra(PicEditActivity.SOURCE_URI, uri);
            startActivity(intent);
        }
    }

    private void startBrowseActivity(Uri uri) {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra(BrowseActivity.IMAGE_URI_STATE_KEY, uri);
        startActivity(intent);
    }

    private void startDrawActivity(Uri uri) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
    }

    public void onDraw(View v) {
        startDrawActivity(mImageUri);
    }

    public void onCamera(View v) {
        if (firebaseAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CameraActivity");
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }
        startCameraActivityCameraPermission();
    }

    public void onAdd(View v) {
        Intent intent = new Intent(this, BrowseActivity.class);
        startActivity(intent);
    }

    public void onHome(View v) {
        // Do nothing
    }

    public void onBrowse(View v) {
        startBrowseActivity(mImageUri);
    }

    private void startCameraActivityCameraPermission() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (Utils.getPermission(this, Manifest.permission.CAMERA,
                    getString(R.string.permission_camera_explanation),
                    REQUEST_PERMISSIONS_CALLBACK_CAMERA,
                    new Utils.OnExplanationDeclinedHandler() {
                        @Override
                        public void onExplanationDeclined() {
                            // do nothing in this case since we're still on the main screen
                        }
                    })) {
                startCamerActivityWriteStoragePermission();
            }
        }
        else {
            Utils.showCustomToast(this, "Device has no camera.");
        }
    }

    private void startCamerActivityWriteStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        // do nothing in this case since we're still on the main screen
                    }
                })) {
            startCameraActivity();
        }
    }

    private void startCameraActivity() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo;
        try
        {
            // place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        }
        catch(Exception e)
        {
            Log.v(TAG, "Can't create file to take picture!", e);
            Utils.showCustomToast(this, "Please check SD card! Image shot is impossible!");
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < 24) {
            mImageUri = Uri.fromFile(photo);
        } else {
            mImageUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".myspecialfileprovider", photo);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        //start camera intent
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CALLBACK_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startCamerActivityWriteStoragePermission();
                }
                // Do nothing if permission was not granted
                return;
            }
            case REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    startCameraActivity();
                }
                // Do nothing if permission was not granted
                return;
            }

        }
    }
}
