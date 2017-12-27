package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TabHost;

import com.caitoo0o.filler.backgroundtasks.GetBrowseThumbnailImageAsyncTask;
import com.caitoo0o.filler.reusableuserinterface.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.reusableuserinterface.PositionImageView;
import com.caitoo0o.filler.utils.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrowseActivity extends LoggedInActivity {

    private static final String TAG = BrowseActivity.class.getName();

    private ArrayList<File> allFillerImages;
    private ArrayList<File> allOtherImages;

    private static final int MAX_THUMBNAIL_PIXELS = 50000;

    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";

    public static final int REQUEST_PERMISSIONS_CALLBACK_WRITE_EXTERNAL_STORAGE = 2;

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        setupTabTitles();

        Utils.setupFillerRecentAppsBar(this);

        ((ImageButton) findViewById(R.id.drawButton)).setImageResource(R.drawable.ic_brush_dark_24dp);
        ((ImageButton) findViewById(R.id.cameraButton)).setImageResource(R.drawable.ic_camera_alt_dark_24dp);
        ((ImageButton) findViewById(R.id.homeButton)).setImageResource(R.drawable.ic_home_dark_24dp);
        ((ImageButton) findViewById(R.id.openExistingButton)).setImageResource(R.drawable.ic_collections_white_24dp);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null && getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
            mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
        }

        getFilesStoragePermission();
    }

    public void onDraw(View v) {
        startDrawActivity(mImageUri);
    }

    public void onCamera(View v) {
        startCameraActivity(mImageUri);
    }

    public void onHome(View v) {
        startPicSourceActivity(mImageUri);
    }

    public void onBrowse(View v) {
        // Do nothing
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
        createFillerImageStorage();

        // Find Filler pictures
        File picturesStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        final File albumStorageDir = new File(picturesStorageDir, "Filler");
        allFillerImages = new ArrayList<File>();
        FilenameFilter allFillerImagesFF = new FilenameFilter() {
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
        listFiles(albumStorageDir, allFillerImages, allFillerImagesFF);
        // Sort images by date ascending
        Collections.sort(allFillerImages, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                Long o1lastModified = o1.lastModified();
                Long o2lastModified = o2.lastModified();
                return o1lastModified.compareTo(o2lastModified);
            }
        });

        // Find all pictures other than those in Filler directory
        final File sdcardDir = Environment.getExternalStorageDirectory();
        allOtherImages = new ArrayList<File>();
        FilenameFilter allOtherImagesFF = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                // Don't accept the Filler storage directory here because that will be viewed elsewhere
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
        listFiles(sdcardDir, allOtherImages, allOtherImagesFF);
        // Sort images by date ascending
        Collections.sort(allOtherImages, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                Long o1lastModified = o1.lastModified();
                Long o2lastModified = o2.lastModified();
                return o1lastModified.compareTo(o2lastModified);
            }
        });

        GridView fillerImagesGV = (GridView) findViewById(R.id.filler_images_gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), allFillerImages);
        fillerImagesGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View v, int pos,
                                    long arg3) {
                // dont need but what is it??
                Object i = adapterView.getItemAtPosition(pos);
                // Load the image that was clicked into the editor
                chooseImageDestination(Uri.fromFile(allFillerImages.get(getIdxFromPos(allFillerImages, pos))));
            }
        });
        fillerImagesGV.setAdapter(adapter);

        GridView allImagesGV = (GridView) findViewById(R.id.all_images_gridview);
        ImageAdapter adapter2 = new ImageAdapter(getApplicationContext(), allOtherImages);
        allImagesGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View v, int pos,
                                    long arg3) {
                // Load the image that was clicked into the editor
                chooseImageDestination(Uri.fromFile(allOtherImages.get(getIdxFromPos(allOtherImages, pos))));
            }
        });
        allImagesGV.setAdapter(adapter2);
    }

    private void chooseImageDestination(final Uri uri) {
        Utils.showConfirmDialog(this,
                getString(R.string.choose_image_destination),
                getString(R.string.fill),
                getString(R.string.draw),
                new Utils.ConfirmDialogHandler() {
                    public void onNegativeButton(Dialog confirmDialog) {
                        startDrawActivity(uri);
                        confirmDialog.dismiss();
                    }
                    public void onPositiveButton(Dialog confirmDialog) {
                        startPicEditActivity(uri);
                        confirmDialog.dismiss();
                    }
                },
                false);
    }

    private void setupTabTitles() {
        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec(getString(R.string.app_name));
        spec.setContent(R.id.tab1);
        spec.setIndicator(getString(R.string.app_name));
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec(getString(R.string.browse_activity_everywhere));
        spec.setContent(R.id.tab2);
        spec.setIndicator(getString(R.string.browse_activity_everywhere));
        host.addTab(spec);
    }

    private void createFillerImageStorage() {
        if (Utils.isExternalStorageWritable()) {
            // TODO: Don't really need to do this every time - check if its there first?
            //File albumStorageDir = getAlbumStorageDir("FillerSaves");
            File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            File albumStorageDir = new File(pictureStorageDir, "Filler");
            try {
                albumStorageDir.mkdir();
            }
            catch (Exception e) {
                Utils.showCustomToast(this, "Failed to create Filler directory");
                Log.e(TAG, "Failed to create Filler directory.");
                e.printStackTrace();
            }
        }
        else {
            // TODO: move these strings to strings file
            Utils.showCustomToast(this, "Create Filler storage failed. External storage not writeable.");
            Log.e(TAG, "Create Filler storage failed. External storage not writeable.");
        }
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

    // Used to get the index into the allFillerImages list in order to retrieve the files in order of most recent first
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

    private void startCameraActivity(Uri uri) {
        Intent intent = new Intent(this, PicSourceActivity.class);
        intent.putExtra(DrawActivity.SOURCE_URI, uri);
        intent.putExtra(PicSourceActivity.START_CAMERA, true);
        startActivity(intent);
    }

    private void startPicSourceActivity(Uri uri) {
        Intent intent = new Intent(this, PicSourceActivity.class);
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

            PositionImageView positionImageView = (PositionImageView) convertView.findViewById(R.id.imageview);
            positionImageView.setPosition(position);
            holder.picturesView = positionImageView;
            holder.picturesView.setImageDrawable(getResources().getDrawable(R.drawable.image_placeholder));
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
