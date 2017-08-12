package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.caitoo0o.filler.backgroundtasks.GetImageAsyncTask;
import com.caitoo0o.filler.backgroundtasks.RestHelper;
import com.caitoo0o.filler.reusableuserinterface.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class PicSourceActivity extends AppCompatActivity {

    private static final String TAG = PicSourceActivity.class.getName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;
    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";
    public static final String START_PIC_EDIT = "START_PIC_EDIT";
    public static final String START_DRAW = "START_DRAW";

    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA = 0;
    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE = 2;

    private Uri mImageUri;

    private boolean startPicEdit = false;
    private boolean startDraw = false;

    private Handler handler = new Handler();

    private ImageAdapter adapter;

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
            } else if (getIntent().hasExtra(START_DRAW) &&
                    getIntent().getBooleanExtra(START_DRAW, false) == true &&
                    getIntent().hasExtra(DrawActivity.SOURCE_URI) &&
                    getIntent().getParcelableExtra(DrawActivity.SOURCE_URI) != null ) {
                startDraw = true;
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
                startDrawActivity(mImageUri);
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


        // Handle image feed
        // Create the adapter to convert the array to views
        adapter = new ImageAdapter(getApplicationContext());
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.image_list_view);
        listView.setAdapter(adapter);
        // Call to get the images
        getMyImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (startPicEdit) {
            startPicEdit = false;
            handler.postDelayed(startPicEditDelayed, getResources().getInteger(R.integer.anim_duration));
        } else if (startDraw) {
            startDraw = false;
            handler.postDelayed(startDrawDelayed, getResources().getInteger(R.integer.anim_duration));
        }
    }

    private void getMyImages() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        RestHelper.HttpCallbacks callbacks = new RestHelper.HttpCallbacks() {
            public void onSuccess(String response) {
                Log.i(this.getClass().getName(), response);

                try {
                    JSONObject responseJson = new JSONObject(response);
                    JSONArray urlArrayJson = responseJson.getJSONArray("urls");
                    for (int i = 0; i < urlArrayJson.length(); i++) {
                        String url = (String) urlArrayJson.get(i);
                        adapter.addItem(url);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    // TODO: Handle this
                    Log.i(this.getClass().getName(), "Error parsing json.", e);
                }
            }

            public void onFailure(String error) {
                // If get images fails notify the user
                // TODO: Use Utils.showConfirmDialog
                new android.app.AlertDialog.Builder(PicSourceActivity.this)
                        .setMessage(getString(R.string.download_failure))
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing, just close
                                    }
                                }).show();
            }
        };
        RestHelper.myImages(getApplicationContext(), user, callbacks);

    }

    private Runnable startPicEditDelayed = new Runnable() {
        public void run() {
            startPicEditActivity(mImageUri);
        }
    };

    private Runnable startDrawDelayed = new Runnable() {
        public void run() {
            startDrawActivity(mImageUri);
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
        startDrawActivity(mImageUri);
    }

    public void onUpload(View v) {
        startUploadActivity(mImageUri);
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
            Log.e(TAG, "No Image Selected.");
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
//                    Log.e(TAG, "bitmap from grabImage is null.");
//                    Toast.makeText(getApplicationContext(), "No image selection found.", Toast.LENGTH_LONG).show();
//                }
            } else {
                Log.e(TAG, "data or data.getData from image picker intent is null.");
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

    private void startDrawActivity(Uri uri) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        this.overridePendingTransition(R.anim.slide_left_to_center, R.anim.slide_center_to_right);
    }

    private void startUploadActivity(Uri uri) {
        Intent intent = new Intent(this, UploadActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_bottom_to_center, R.anim.slide_center_to_top);
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
            Log.v(TAG, "Can't create file to take picture!", e);
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

    // Image Adapter
    // Adapter for Grid View
    private class ImageAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String> imageUrlList;

        public ImageAdapter(Context context) {
            this.context = context;
            this.imageUrlList = new ArrayList<String>();
        }

        public void addItem(String url) {
            imageUrlList.add(url);
        }

        public int getCount() {
            return imageUrlList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageAdapter.ViewHolder holder = new ImageAdapter.ViewHolder();

            if (convertView == null) {
                holder.picturesView = new ImageView(context);
                //Converting the Row Layout to be used in Grid View
                convertView = getLayoutInflater().inflate(R.layout.image_row, parent, false);

                convertView.setTag(holder);

            } else {
                holder = (ImageAdapter.ViewHolder) convertView.getTag();
            }

            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.image_row_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            holder.picturesView = (ImageView) convertView.findViewById(R.id.imageview);
            if (position < imageUrlList.size()) {
                holder.picturesView.setImageDrawable(getResources().getDrawable(R.drawable.image_placeholder));
                // TODO: Is is possible this will wind up with pictures out of order if one async task finishes later than one that started later?
                new GetImageAsyncTask(PicSourceActivity.this, holder.picturesView, progressBar, imageUrlList.get(position)).execute();
            }

            return convertView;
        }

        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            private ImageView picturesView;
        }
    }
}
