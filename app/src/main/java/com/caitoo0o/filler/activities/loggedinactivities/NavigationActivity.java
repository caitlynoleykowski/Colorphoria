package com.caitoo0o.filler.activities.loggedinactivities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.uihelpers.OnSwipeTouchListener;

import java.io.File;

/**
 * Created by caitoo0o on 6/13/2017.
 */

public class NavigationActivity extends LoggedInActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";

    protected Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null && getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
            mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
        }

        final View outerLinearLayout = findViewById(R.id.outer_layout);
        outerLinearLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeTop() {
                // This is if you swipe from bottom to top to move down
                if (mImageUri != null) {
                    startPicEditActivity(mImageUri);
                }
            }

            public void onSwipeRight() {
                // This is if you swipe left to right to move left
                startDrawActivity();
            }

            public void onSwipeLeft() {
                // This is if you swipe right to left to move right
                startBrowseActivity(mImageUri);
            }

            public void onSwipeBottom() {
                // This is if you swipe from top to bottom to move up
                //Toast.makeText(PicSourceActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                startCameraActivity();
            }

        });
    }

    protected void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicEditActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void startBrowseActivity(Uri uri) {
        Intent intent = new Intent(this, BrowseActivity.class);
        intent.putExtra(BrowseActivity.IMAGE_URI_STATE_KEY, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void startDrawActivity() {
        Intent intent = new Intent(this, DrawActivity.class);
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_left_to_right, R.anim.slide_right_to_left);
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startCameraActivity() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

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
                Log.v("picsource", "Can't create file to take picture!");
                Toast.makeText(this, "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG).show();
                return;
            }
            mImageUri = Uri.fromFile(photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            //start camera intent
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        else {
            Toast.makeText(getApplicationContext(), "Device has no camera.", Toast.LENGTH_LONG).show();
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
}
