package com.caitoo0o.filler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class PicSourceActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        View outerLinearLayout = findViewById(R.id.outer_linear_layout);
        outerLinearLayout.setOnTouchListener(new OnSwipeTouchListener(PicSourceActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(PicSourceActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                Toast.makeText(PicSourceActivity.this, "right", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeLeft() {
                Toast.makeText(PicSourceActivity.this, "left", Toast.LENGTH_SHORT).show();
                startPicEditActivity();
            }

            public void onSwipeBottom() {
                Toast.makeText(PicSourceActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });

        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        getPermission(Manifest.permission.CAMERA);

        if (savedInstanceState != null) {
            // do nothing
        }
        else {
            //do nothing
        }
    }

    void getPermission(String permissionKind) {
        final int REQUEST = 1;
        String[] PERMISSIONS_STORAGE = {
                permissionKind,
        };
        int permission = ActivityCompat.checkSelfPermission(this, permissionKind);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST
            );
        }
    }

    public void onOpen(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("return-data", true);

        // Verify there is an app to accept this type intent before starting it
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    public void onCamera(View v) {
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        else {
            Toast.makeText(getApplicationContext(), "Device has no camera.", Toast.LENGTH_LONG).show();
        }
    }

    public void onBrowse(View v) {
        Intent intent = new Intent(this, Browse.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
            Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_LONG).show();
            return;
        }

        if (requestCode == REQUEST_IMAGE_GET) {
            if (data != null && data.getData() != null) {
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
            startPicEditActivity(mImageUri);
//            Bitmap image = grabImage(mImageUri);
//            if (image != null) {
//                startPicEditActivity(image);
//            }
        }
    }

    private void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicEditActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void startPicEditActivity() {
        Intent intent = new Intent(this, PicEditActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        // do nothing
    }
}
