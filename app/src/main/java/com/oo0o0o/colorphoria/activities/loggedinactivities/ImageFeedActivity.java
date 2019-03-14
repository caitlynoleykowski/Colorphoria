package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.oo0o0o.colorphoria.backgroundtasks.GetImageAsyncTask;
import com.oo0o0o.colorphoria.backgroundtasks.RestHelper;
import com.oo0o0o.colorphoria.data.AppDatabase;
import com.oo0o0o.colorphoria.data.MediaEntity;
import com.oo0o0o.colorphoria.reusableuserinterface.ListRefreshView;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class ImageFeedActivity extends LoggedInActivity {

    private static final String TAG = ImageFeedActivity.class.getName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GET = 0;
    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";

    public static final String START_CAMERA = "START_CAMERA";

    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA = 0;
    public static final int REQUEST_PERMISSIONS_CALLBACK_CAMERA_WRITE_EXTERNAL_STORAGE = 2;

    private Uri mImageUri;

    private ImageAdapter adapter;
    private ProgressBar progressBar;

    private boolean refreshingFeed = false;

    AdView adView;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_feed);

        Utils.setupcolorphoriaRecentAppsBar(this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.actionbar_logo);

        MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        } else if (getIntent() != null) {
            if (getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
                mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
            }
            if (getIntent().hasExtra(START_CAMERA) &&
                    getIntent().getBooleanExtra(START_CAMERA, false) == true) {
                startCameraActivityCameraPermission();
            }
        }

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

        // Load the media urls from the database and display the images (if in db, should be cached)
        // Then, attempt to fetch the urls from the server
        new AsyncTask<String, String, List<MediaEntity>>() {
            @Override
            protected List<MediaEntity> doInBackground(String... strings) {
                AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "colorphoria-database")
                        .fallbackToDestructiveMigration()
                        .build();

                List<MediaEntity> mediaEntities = db.mediaDao().getAll();
                return mediaEntities;
            }

            @Override
            protected void onPostExecute(List<MediaEntity> mediaEntities) {
                super.onPostExecute(mediaEntities);
                if (mediaEntities.size() > 0) {
                    loadFeed(mediaEntities);
                }
                // Call to get the images
                getMyImages();
            }
        }.execute();


        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, ImageFeedActivity.class.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        ((BottomNavigationView) findViewById(R.id.bottom_navigation))
                .setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.image_feed_nav:
                                onHome();
                                return true;
                            case R.id.create_nav:
                                onAdd();
                                return true;
                            case R.id.account_nav:
                                onAccount();
                                return true;
                            default:
                                // Do nothing
                        }
                        return false;
                    }
                });
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }
    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (adView != null) {
            adView.resume();
        }
    }
    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void getMyImages() {
        if (!refreshingFeed) {
            progressBar.setVisibility(View.VISIBLE);
            adapter.setDoneLoading(false);
            refreshingFeed = true;

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            RestHelper.HttpCallbacks callbacks = new RestHelper.HttpCallbacks() {
                public void onSuccess(final String response) {
                    // AsyncTask needed for db access to save media urls from response
                    new AsyncTask<String, String, List<MediaEntity>>() {
                        @Override
                        protected List<MediaEntity> doInBackground(String... strings) {
                            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                                    AppDatabase.class, "colorphoria-database").build();
                            db.mediaDao().deleteAll();

                            List<MediaEntity> mediaEntities = new ArrayList<MediaEntity>();

                            try {
                                JSONObject responseJson = new JSONObject(response);
                                if (responseJson.has("mediaEntities")) {
                                    // TODO: Handle this better in case somehow it has "urls" but the array is empty

                                    JSONArray mediaEntitiesJson = responseJson.getJSONArray("mediaEntities");
                                    Gson gson = new Gson();
                                    for (int i = 0; i < mediaEntitiesJson.length(); i++) {
                                        String mediaEntityJson = (String) mediaEntitiesJson.get(i).toString();
                                        MediaEntity mediaEntity = gson.fromJson(mediaEntityJson, MediaEntity.class);
                                        mediaEntity.setId(i);
                                        db.mediaDao().insertAll(mediaEntity); // Change id from i
                                        mediaEntities.add(mediaEntity);
                                    }
                                }
                            } catch (JSONException e) {
                                // TODO: Handle this
                                Log.i(this.getClass().getName(), "Error parsing json.", e);
                            }
                            return mediaEntities;
                        }

                        @Override
                        protected void onPostExecute(List<MediaEntity> mediaEntities) {
                            super.onPostExecute(mediaEntities);
                            loadFeed(mediaEntities);

                        }
                    }.execute(response);
                }

                public void onFailure(String error) {
                    // AsyncTask needed for db access to fetch media urls
                    new AsyncTask<String, String, List<MediaEntity>>() {
                        @Override
                        protected List<MediaEntity> doInBackground(String... strings) {
                            AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                                    AppDatabase.class, "colorphoria-database").build();
                            List<MediaEntity> mediaEntities = db.mediaDao().getAll();
                            return mediaEntities;
                        }

                        @Override
                        protected void onPostExecute(List<MediaEntity> mediaEntities) {
                            super.onPostExecute(mediaEntities);
                            progressBar.setVisibility(View.GONE);
                            // TODO: Figure out why in the world networkResponse is null and come back to this
                            // TODO: so that we don't have to just loadFeed as if there's no images for today
                            Utils.showCustomToast(ImageFeedActivity.this, "Unable to refresh feed");
                            loadFeed(mediaEntities);
                            refreshingFeed = false;
                        }
                    }.execute();
                }
            };
            RestHelper.myImages(getApplicationContext(), user, callbacks);
        }
    }

    private void loadFeed(List<MediaEntity> mediaEntities) {
        adapter.setDoneLoading(true);
        adapter.clear(); // Remove all images (image urls) from the adapter
        for (int i = 0; i < mediaEntities.size(); i++) {
            adapter.addItem(mediaEntities.get(i));
        }
        adapter.notifyDataSetChanged(); // Make list view pick up changes
        refreshingFeed = false;
        progressBar.setVisibility(View.GONE);
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

    public void onAdd() {
        Intent intent = new Intent(this, SourceActivity.class);
        startActivity(intent);
    }

    public void onHome() {
        // Do nothing
    }

    public void onBrowse(View v) {
        startBrowseActivity(mImageUri);
    }

    public void onAccount() {
        Intent intent = new Intent(this, AccountActivity.class);
        startActivity(intent);
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
                Utils.showCustomToast(ImageFeedActivity.this, "No image selection found.");
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE) {
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

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI_STATE_KEY, mImageUri);
    }

    // Image Adapter
    // Adapter for Grid View
    private class ImageAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<MediaEntity> imageUrlList;
        private boolean doneLoading = false;

        public ImageAdapter(Context context) {
            this.context = context;
            this.imageUrlList = new ArrayList<MediaEntity>();
        }

        public boolean isDoneLoading() {
            return doneLoading;
        }

        public void setDoneLoading(boolean doneLoading) {
            this.doneLoading = doneLoading;
        }

        public void addItem(MediaEntity mediaEntity) {
            imageUrlList.add(mediaEntity);
        }

        public void addAll(List<MediaEntity> mediaEntities) {
            imageUrlList.addAll(mediaEntities);
        }

        public void clear() {
            imageUrlList.clear();
        }

        public int getCount() {
            // If doneLoading and the list is empty,
            // return 1 in order to show the feed unavailable image
            if (imageUrlList.size() == 0 && doneLoading) {
                return 1;
            } else {
                return imageUrlList.size();
            }
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

            if (imageUrlList.size() != 0) {
                ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.image_row_progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                holder.picturesView = (ImageView) convertView.findViewById(R.id.imageview);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.title_text_view);
                holder.dateTextView = (TextView) convertView.findViewById(R.id.date_text_view);
                holder.commentTextView = (TextView) convertView.findViewById(R.id.comment_text_view);
                if (position < imageUrlList.size()) {
                    holder.picturesView.setImageDrawable(getResources().getDrawable(R.drawable.bg_image_placeholder));
                    // This will wind up with pictures out of order if one async task finishes later than one that started later
                    // But it works well enough and its unlikely that the scenario will happen often
                    new GetImageAsyncTask(ImageFeedActivity.this, holder.picturesView, progressBar, imageUrlList.get(position).getDownloadUrl()).execute();
                    holder.titleTextView.setText(imageUrlList.get(position).getUsername());
                    holder.dateTextView.setText(DateFormat.getDateInstance().format(imageUrlList.get(position).getTimestamp()));
                    holder.commentTextView.setText(
                            imageUrlList.get(position).getComment() != null ? imageUrlList.get(position).getComment() : "");
                } else {
                    // Not sure if I should set the image here to blank/empty
                    holder.titleTextView.setText("");
                    holder.dateTextView.setText("");
                    holder.commentTextView.setText("");
                }
            } else {
                holder.picturesView = (ImageView) convertView.findViewById(R.id.imageview);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.title_text_view);
                holder.dateTextView = (TextView) convertView.findViewById(R.id.date_text_view);
                holder.commentTextView = (TextView) convertView.findViewById(R.id.comment_text_view);

                holder.picturesView.setImageDrawable(getResources().getDrawable(R.drawable.img_feed_unavailable));
                holder.titleTextView.setText("");
                holder.dateTextView.setText("");
                holder.commentTextView.setText("");
            }

            return convertView;
        }

        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            private ImageView picturesView;
            private TextView titleTextView;
            private TextView dateTextView;
            private TextView commentTextView;
        }
    }
}
