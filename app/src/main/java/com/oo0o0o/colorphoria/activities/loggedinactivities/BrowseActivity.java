package com.oo0o0o.colorphoria.activities.loggedinactivities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.oo0o0o.colorphoria.backgroundtasks.GetBrowseThumbnailImageAsyncTask;
import com.oo0o0o.colorphoria.R;
import com.oo0o0o.colorphoria.reusableuserinterface.PositionImageView;
import com.oo0o0o.colorphoria.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrowseActivity extends LoggedInActivity {

    private static final String TAG = BrowseActivity.class.getName();

    private ArrayList<File> allcolorphoriaImages;
    private ArrayList<File> allOtherImages;

    private static final int MAX_THUMBNAIL_PIXELS = 50000;

    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";

    public static final int REQUEST_PERMISSIONS_CALLBACK_WRITE_EXTERNAL_STORAGE = 2;

    private Uri mImageUri;

    int selectedIndex = -1;
    View selectedView = null;
    Uri selectedUri = null;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        setupTabTitles();

        Utils.setupcolorphoriaRecentAppsBar(this);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null && getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
            mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, BrowseActivity.class.getName());
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        getFilesStoragePermission();
    }

    private void getFilesStoragePermission() {
        if (Utils.getPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getString(R.string.permission_write_external_storage_explanation),
                REQUEST_PERMISSIONS_CALLBACK_WRITE_EXTERNAL_STORAGE,
                new Utils.OnExplanationDeclinedHandler() {
                    @Override
                    public void onExplanationDeclined() {
                        startPicSourceActivity(mImageUri);
                    }
                })) {
            getFiles();
        }
    }
    
    private void getFiles() {

        new AsyncTask<Void, Void, ArrayList<File>>() {
            @Override
            protected ArrayList<File> doInBackground(Void... voids) {

                createcolorphoriaImageStorage();

                // Find colorphoria pictures
                File picturesStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                final File albumStorageDir = new File(picturesStorageDir, "colorphoria");
                ArrayList<File> allcolorphoriaImagesLocal = new ArrayList<File>();
                FilenameFilter allcolorphoriaImagesFF = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        // If filename is a directory or a picture accept it
                        if (!filename.contains(".") ||
                                filename.toLowerCase().endsWith(".png") ||
                                filename.toLowerCase().endsWith(".jpg") ||
                                filename.toLowerCase(). endsWith(".jpeg")) {
                            return true;
                        }
                        return false;
                    }
                };
                listFiles(albumStorageDir, allcolorphoriaImagesLocal, allcolorphoriaImagesFF);
                // Sort images by date ascending
                Collections.sort(allcolorphoriaImagesLocal, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        Long o1lastModified = o1.lastModified();
                        Long o2lastModified = o2.lastModified();
                        return o1lastModified.compareTo(o2lastModified);
                    }
                });

                return allcolorphoriaImagesLocal;
            }

            @Override
            protected void onPostExecute(ArrayList<File> allcolorphoriaImagesLocal) {
                super.onPostExecute(allcolorphoriaImagesLocal);

                allcolorphoriaImages = allcolorphoriaImagesLocal;

                GridView colorphoriaImagesGV = (GridView) findViewById(R.id.colorphoria_images_gridview);
                ImageAdapter adapter = new ImageAdapter(getApplicationContext(), allcolorphoriaImages);
                colorphoriaImagesGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long arg3) {
                        LinearLayout selectionOverlay = (LinearLayout) v.findViewById(R.id.thumbnail_item_selection_overlay);
                        if (selectionOverlay.getVisibility() == View.VISIBLE) {
                            selectionOverlay.setVisibility(View.INVISIBLE);
                            selectedIndex = -1;
                            selectedView = null;
                            selectedUri = null;
                        } else {
                            selectionOverlay.setVisibility(View.VISIBLE);
                            if (selectedIndex != -1) {
                                LinearLayout selectedViewSelectionOverlay = (LinearLayout) selectedView.findViewById(R.id.thumbnail_item_selection_overlay);
                                if (selectedViewSelectionOverlay != null) {
                                    selectedViewSelectionOverlay.setVisibility(View.INVISIBLE);
                                }
                            }
                            selectedIndex = pos;
                            selectedView = v;
                            selectedUri = Uri.fromFile(allcolorphoriaImages.get(getIdxFromPos(allcolorphoriaImages, selectedIndex)));
                        }
                    }
                });
                colorphoriaImagesGV.setAdapter(adapter);
            }
        }.execute();

        new AsyncTask<Void, Void, ArrayList<File>>() {
            @Override
            protected ArrayList<File> doInBackground(Void... voids) {

                // Find colorphoria pictures
                File picturesStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                final File albumStorageDir = new File(picturesStorageDir, "colorphoria");

                // Find all pictures other than those in colorphoria directory
                final File sdcardDir = Environment.getExternalStorageDirectory();
                ArrayList<File> allOtherImagesLocal = new ArrayList<File>();
                FilenameFilter allOtherImagesFF = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        // Don't accept the colorphoria storage directory here because that will be viewed elsewhere
                        if (dir.equals(albumStorageDir)) {
                            return false;
                        }
                        // If filename is a directory or a picture accept it
                        if (!filename.contains(".") ||
                                filename.toLowerCase().endsWith(".png") ||
                                filename.toLowerCase().endsWith(".jpg") ||
                                filename.toLowerCase(). endsWith(".jpeg")) {
                            return true;
                        }
                        return false;
                    }
                };
                listFiles(sdcardDir, allOtherImagesLocal, allOtherImagesFF);
                // Sort images by date ascending
                Collections.sort(allOtherImagesLocal, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        Long o1lastModified = o1.lastModified();
                        Long o2lastModified = o2.lastModified();
                        return o1lastModified.compareTo(o2lastModified);
                    }
                });

                return allOtherImagesLocal;
            }

            @Override
            protected void onPostExecute(ArrayList<File> allOtherImagesLocal) {
                super.onPostExecute(allOtherImagesLocal);

                allOtherImages = allOtherImagesLocal;

                GridView allImagesGV = (GridView) findViewById(R.id.all_images_gridview);
                ImageAdapter adapter2 = new ImageAdapter(getApplicationContext(), allOtherImages);
                allImagesGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long arg3) {
                        LinearLayout selectionOverlay = (LinearLayout) v.findViewById(R.id.thumbnail_item_selection_overlay);
                        if (selectionOverlay.getVisibility() == View.VISIBLE) {
                            selectionOverlay.setVisibility(View.INVISIBLE);
                            selectedIndex = -1;
                            selectedView = null;
                            selectedUri = null;
                        } else {
                            selectionOverlay.setVisibility(View.VISIBLE);
                            if (selectedIndex != -1) {
                                LinearLayout selectedViewSelectionOverlay = (LinearLayout) selectedView.findViewById(R.id.thumbnail_item_selection_overlay);
                                if (selectedViewSelectionOverlay != null) {
                                    selectedViewSelectionOverlay.setVisibility(View.INVISIBLE);
                                }
                            }
                            selectedIndex = pos;
                            selectedView = v;
                            selectedUri = Uri.fromFile(allOtherImages.get(getIdxFromPos(allOtherImages, selectedIndex)));
                        }
                    }
                });
                allImagesGV.setAdapter(adapter2);
            }
        }.execute();


    }

    private void createcolorphoriaImageStorage() {
        if (Utils.isExternalStorageWritable()) {
            // TODO: Don't really need to do this every time - check if its there first?
            //File albumStorageDir = getAlbumStorageDir("colorphoriaSaves");
            File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File albumStorageDir = new File(pictureStorageDir, "colorphoria");
            try {
                albumStorageDir.mkdir();
            }
            catch (Exception e) {
                Utils.showCustomToast(this, "Failed to create colorphoria directory");
                Log.e(TAG, "Failed to create colorphoria directory.");
                e.printStackTrace();
            }
        }
        else {
            // TODO: move these strings to strings file
            Utils.showCustomToast(this, "Create colorphoria storage failed. External storage not writeable.");
            Log.e(TAG, "Create colorphoria storage failed. External storage not writeable.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.browse_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.browse_menu_next:
                if (selectedIndex != -1 && selectedUri != null) {
                    chooseImageDestination(selectedUri);
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void chooseImageDestination(final Uri uri) {
//        Utils.showConfirmDialog(this,
//                getString(R.string.choose_image_destination),
//                getString(R.string.apply_effects),
//                getString(R.string.draw),
//                new Utils.ConfirmDialogHandler() {
//                    public void onNegativeButton(Dialog confirmDialog) {
//                        startDrawActivity(uri);
//                        confirmDialog.dismiss();
//                    }
//                    public void onPositiveButton(Dialog confirmDialog) {
//                        startPicEditActivity(uri);
//                        confirmDialog.dismiss();
//                    }
//                },
//                false);
        startPicEditActivity(uri);
    }

    private void setupTabTitles() {
        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec1 = host.newTabSpec(getString(R.string.browse_activity_everywhere));
        spec1.setContent(R.id.tab1);
        spec1.setIndicator(getString(R.string.browse_activity_everywhere));
        host.addTab(spec1);

        //Tab 2
        TabHost.TabSpec spec2 = host.newTabSpec(getString(R.string.app_name));
        spec2.setContent(R.id.tab2);
        spec2.setIndicator(getString(R.string.app_name));
        host.addTab(spec2);

    }

    private void listFiles(File directory, ArrayList<File> files, FilenameFilter filenameFilter) {
        // get all the files from a directory
        File[] fList = directory.listFiles(filenameFilter);
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listFiles(file, files, filenameFilter);
            }
        }
    }

    // Used to get the index into the allcolorphoriaImages list in order to retrieve the files in order of most recent first
    public static int getIdxFromPos(ArrayList<File> files, int pos) {
        return files.size() - pos - 1;
    }

    private void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicEditActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
            }

    private void startDrawActivity(Uri uri) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra(DrawActivity.SOURCE_URI, uri);
        startActivity(intent);
    }

    private void startPicSourceActivity(Uri uri) {
        Intent intent = new Intent(this, BottomNavActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
    }

    // Adapter for Grid View
    public class ImageAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<File> files;

        public ImageAdapter(Context context, ArrayList<File> files) {
            this.context = context;
            this.files = files;
        }

        public int getCount() {
            return files.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();

            if (convertView == null) {
                //Converting the Row Layout to be used in Grid View
                convertView = getLayoutInflater().inflate(R.layout.thumnail_item, parent, false);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.thumbnail_item_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
            LinearLayout selectionOverlay = (LinearLayout) convertView.findViewById(R.id.thumbnail_item_selection_overlay);
            selectionOverlay.setVisibility(View.INVISIBLE);

            PositionImageView positionImageView = (PositionImageView) convertView.findViewById(R.id.imageview);
            positionImageView.setPosition(position);
            holder.picturesView = positionImageView;
            holder.picturesView.setImageDrawable(getResources().getDrawable(R.drawable.bg_image_placeholder));
            // TODO: Is is possible this will wind up with pictures out of order if one async task finishes later than one that started later?
            // Answer: Yes. totally possible, but generally they finish in order.
            // And u will see images flashing in the order they finish
            new GetBrowseThumbnailImageAsyncTask(BrowseActivity.this, holder.picturesView, progressBar, files, position).execute();

            return convertView;
        }
        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            public PositionImageView picturesView;
        }
    }

    public static double getThumbnailScaleFactor(int bitmapWidth, int bitmapHeight) {
        int numPixels = bitmapWidth * bitmapHeight;
        if (numPixels >= MAX_THUMBNAIL_PIXELS) {
            double scaleFactor = Math.sqrt((double) MAX_THUMBNAIL_PIXELS / (double) numPixels);
            return scaleFactor;
        } else {
            return 1;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CALLBACK_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, so continue.
                    getFiles();
                } else {
                    // No permission granted so go back to main page (this page will have no content)
                    startPicSourceActivity(mImageUri);
                }
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
