package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.caitoo0o.filler.backgroundtasks.GetImageAsyncTask;
import com.caitoo0o.filler.backgroundtasks.RestHelper;
import com.caitoo0o.filler.reusableuserinterface.ListRefreshView;
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

public class PicSourceActivity extends LoggedInActivity {

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
    private ProgressBar progressBar;

    private boolean refreshingFeed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_source);

        Utils.setupFillerRecentAppsBar(this);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

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
        final ListRefreshView listView = (ListRefreshView) findViewById(R.id.image_list_view);

        listView.setAdapter(adapter);

        listView.setOnRefreshHandler(new ListRefreshView.OnRefreshHandler() {
            @Override
            public void onRefresh() {
                getMyImages();
            }
        });


        // Call to get the images
        getMyImages();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Call to test firebase endpoint auth
        RestHelper.firebaseEndpointAuthTest(this, user, new RestHelper.HttpCallbacks() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject responseJson = new JSONObject(response);
                    if (responseJson.has("email")) {
                        String message = responseJson.getString("email");
                        Toast.makeText(PicSourceActivity.this, "User's email:" + message, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PicSourceActivity.this, "Error: json does not contain email", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PicSourceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PicSourceActivity.this, "FAILURE: " + error, Toast.LENGTH_LONG).show();
            }
        });
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
        if (!refreshingFeed) {
            progressBar.setVisibility(View.VISIBLE);
            refreshingFeed = true;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            RestHelper.HttpCallbacks callbacks = new RestHelper.HttpCallbacks() {
                public void onSuccess(String response) {
                    loadFeed(response);
                }

                public void onFailure(String error) {
                    refreshingFeed = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PicSourceActivity.this, R.string.download_failure, Toast.LENGTH_LONG).show();
                    // TODO: Figure out why in the world networkResponse is null and come back to this
                    // TODO: so that we don't have to just loadFeed as if there's no images for today
                    loadFeed("{}");
                }
            };
            RestHelper.myImages(getApplicationContext(), user, callbacks);
        }
    }

    private void loadFeed(String response) {
        Log.i(this.getClass().getName(), response);

        try {
            adapter.clear(); // Remove all images (image urls) from the adapter
            JSONObject responseJson = new JSONObject(response);
            if (responseJson.has("urls")) {
                // TODO: Handle this better in case somehow it has "urls" but the array is empty
                JSONArray urlArrayJson = responseJson.getJSONArray("urls");
                for (int i = 0; i < urlArrayJson.length(); i++) {
                    String url = (String) urlArrayJson.get(i);
                    adapter.addItem(url);
                }
            } else {
                // TODO: This is weird to download this static image so change this later
                // TODO: when more time
                adapter.addItem("https://firebasestorage.googleapis.com/v0/b/filler-ea8ae.appspot.com/o/photos%2F1211826150?alt=media&token=df618132-a2a8-4f51-ad5e-65dd1754e8dd");
            }
            adapter.notifyDataSetChanged(); // Make list view pick up changes
        } catch (JSONException e) {
            // TODO: Handle this
            Log.i(this.getClass().getName(), "Error parsing json.", e);
        }
        refreshingFeed = false;
        progressBar.setVisibility(View.GONE);
    }

    private Runnable startPicEditDelayed = new Runnable() {
        public void run() {
            if (mImageUri != null) {
                startPicEditActivity(mImageUri);
            }
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
        // TODO: Is this code used anywhere anymore??
        // TODO: This looks like it was from the external image picker
        if (requestCode == REQUEST_IMAGE_GET) {
            if (data != null && data.getData() != null) {
                mImageUri = data.getData(); // save this as the uri so that on swipe left we can use it again
                if (mImageUri != null) {
                    startPicEditActivity(mImageUri);
                }
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
        if (uri != null) {
            Intent intent = new Intent(this, PicEditActivity.class);
            intent.putExtra(PicEditActivity.SOURCE_URI, uri);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_bottom_to_center, R.anim.slide_center_to_top);
        }
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

        public void clear() {
            imageUrlList.clear();
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
