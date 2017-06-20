package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.caitoo0o.filler.uihelpers.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.Utils;

import java.io.File;

public class PicSourceActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;
    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";
    public static final String START_PIC_EDIT = "START_PIC_EDIT";

    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA = 0;
    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE = 2;

    private Uri mImageUri;

    private boolean startPicEdit = false;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        Utils.setupFillerRecentAppsBar(this);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null) {
            if (getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
                mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
            }
            if (getIntent().hasExtra(START_PIC_EDIT) &&
                    getIntent().getBooleanExtra(START_PIC_EDIT, false) == true &&
                    getIntent().hasExtra(PicEditActivity.SOURCE_URI) &&
                    getIntent().getParcelableExtra(PicEditActivity.SOURCE_URI) != null ) {
                startPicEdit = true;
                mImageUri = getIntent().getParcelableExtra(PicEditActivity.SOURCE_URI);
            }
        }

        final View outerLinearLayout = findViewById(R.id.outer_layout);
        outerLinearLayout.setOnTouchListener(new OnSwipeTouchListener(PicSourceActivity.this) {
            public void onSwipeTop() {
                // Swipe bottom to top
                if (mImageUri != null) {
                    startPicEditActivity(mImageUri);
                }
            }

            public void onSwipeRight() {
                // Swipe left to right
                startDrawActivity();
            }

            public void onSwipeLeft() {
                // Swipe right to left
                startBrowseActivity(mImageUri);
            }

            public void onSwipeBottom() {
                // Swipe top to bottom
                startCameraActivityCameraPermission();
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (startPicEdit) {
            startPicEdit = false;
            handler.postDelayed(startPicEditDelayed, getResources().getInteger(R.integer.anim_duration));
        }
    }

    private Runnable startPicEditDelayed = new Runnable() {
        public void run() {
            startPicEditActivity(mImageUri);
        }
    };

    public void onMore(View v) {
        startActivity(new Intent(this, AccountActivity.class));
    }

    public void onCamera(View v) {
        startCameraActivityCameraPermission();
    }

    public void onBrowse(View v) {
        startBrowseActivity(mImageUri);
    }

    public void onDraw(View v) {
        startDrawActivity();
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
            Log.e("picsource", "No Image Selected.");
            return;
        }

        if (requestCode == REQUEST_IMAGE_GET) {
            if (data != null && data.getData() != null) {
                mImageUri = data.getData(); // save this as the uri so that on swipe left we can use it again
                startPicEditActivity(data.getData());
//                Bitmap bm = grabImage(data.getData());
//                if (bm != null) {
//                    startPicEditActivity(bm);
//                } else {
//                    Log.e("picsource", "bitmap from grabImage is null.");
//                    Toast.makeText(getApplicationContext(), "No image selection found.", Toast.LENGTH_LONG).show();
//                }
            } else {
                Log.e("picsource", "data or data.getData from image picker intent is null.");
                Toast.makeText(getApplicationContext(), "No image selection found.", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
            getContentResolver().notifyChange(mImageUri, null);
            handler.postDelayed(startPicEditDelayed,getResources().getInteger(R.integer.anim_duration));
        }
    }

    private void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicEditActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_bottom_to_center, R.anim.slide_center_to_top);
    }

    private void startBrowseActivity(Uri uri) {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra(BrowseActivity.IMAGE_URI_STATE_KEY, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_right_to_center, R.anim.slide_center_to_left);
    }

    private void startDrawActivity() {
        Intent intent = new Intent(this, DrawActivity.class);
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_left_to_center, R.anim.slide_center_to_right);
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
            Toast.makeText(getApplicationContext(), "Device has no camera.", Toast.LENGTH_LONG).show();
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
            Log.v("picsource", "Can't create file to take picture!", e);
            Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
            return;
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        //start camera intent
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        overridePendingTransition(R.anim.slide_top_to_center, R.anim.slide_center_to_bottom);
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

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI_STATE_KEY, mImageUri);
    }
}
