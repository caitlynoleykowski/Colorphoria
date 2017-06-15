package com.caitoo0o.filler.activities.loggedinactivities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.caitoo0o.filler.uihelpers.OnSwipeTouchListener;
import com.caitoo0o.filler.R;
import com.caitoo0o.filler.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BrowseActivity extends LoggedInActivity {
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

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null && getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
            mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
        }

        View outerRelativeLayout = findViewById(R.id.outer_layout);
        outerRelativeLayout.setOnTouchListener(new OnSwipeTouchListener(BrowseActivity.this) {
            public void onSwipeTop() {
                //Toast.makeText(BrowseActivity.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                //Toast.makeText(BrowseActivity.this, "right", Toast.LENGTH_SHORT).show();
                //startPicEditActivity(mImageUri);
                //startPicSourceActivity(mImageUri);
                startPicSourceActivity(mImageUri);
            }

            public void onSwipeLeft() {
                //Toast.makeText(BrowseActivity.this, "left", Toast.LENGTH_SHORT).show();
//                if (mImageUri != null) {
//                    startPicEditActivity(mImageUri);
//                }
            }

            public void onSwipeBottom() {
                //Toast.makeText(BrowseActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });

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
                startPicEditActivity(Uri.fromFile(allFillerImages.get(getIdxFromPos(allFillerImages, pos))));
            }
        });
        fillerImagesGV.setAdapter(adapter);

        GridView allImagesGV = (GridView) findViewById(R.id.all_images_gridview);
        ImageAdapter adapter2 = new ImageAdapter(getApplicationContext(), allOtherImages);
        allImagesGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View v, int pos,
                                    long arg3) {
                // dont need but what is it??
                Object i = adapterView.getItemAtPosition(pos);
                // Load the image that was clicked into the editor
                Intent intent = new Intent(BrowseActivity.this, PicEditActivity.class);
                intent.putExtra(PicEditActivity.SOURCE_URI, Uri.fromFile(allOtherImages.get(getIdxFromPos(allOtherImages, pos))));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        allImagesGV.setAdapter(adapter2);
    }

    private void setupTabTitles() {
        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Filler");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Filler");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Everywhere");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Everywhere");
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
                Toast.makeText(getApplicationContext(), "Failed to create Filler directory", Toast.LENGTH_LONG).show();
                Log.e("picsource", "Failed to create Filler directory.");
                e.printStackTrace();
            }
        }
        else {
            // TODO: move these strings to strings file
            Toast.makeText(getApplicationContext(), "Create Filler storage failed. External storage not writeable.", Toast.LENGTH_LONG).show();
            Log.e("picsource", "Create Filler storage failed. External storage not writeable.");
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
    private int getIdxFromPos(ArrayList<File> files, int pos) {
        return files.size() - pos - 1;
    }

    private void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicSourceActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        intent.putExtra(PicSourceActivity.START_PIC_EDIT, true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_to_center, R.anim.slide_center_to_right);
    }

    private void startPicSourceActivity(Uri uri) {
        Intent intent = new Intent(this, PicSourceActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_to_center, R.anim.slide_center_to_right);
    }

    // Adapter for Grid View
    private class ImageAdapter extends BaseAdapter {

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
                holder.picturesView = new ImageView(context);
                //Converting the Row Layout to be used in Grid View
                convertView = getLayoutInflater().inflate(R.layout.row, parent, false);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Uri uri = Uri.fromFile(files.get(getIdxFromPos(files, position)));
            InputStream is;
            try {
                is = context.getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(is, null, options);
                double scaleFactor = getScaleFactor(options.outWidth, options.outHeight);
                BitmapFactory.Options finalOptions = new BitmapFactory.Options();
                finalOptions.inSampleSize = (int) Math.ceil(1 / scaleFactor);
                InputStream finalInputStream = context.getContentResolver().openInputStream(uri);
                Bitmap bm = BitmapFactory.decodeStream(finalInputStream, null, finalOptions);

                //Bitmap bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(finalInputStream, null, finalOptions),
                //        THUMBSIZE, THUMBSIZE, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                holder.picturesView = (ImageView) convertView.findViewById(R.id.imageview);
                holder.picturesView.setImageBitmap(bm);
                holder.picturesView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (FileNotFoundException e) {
                Log.e("browse", "File not found to display thumbnail of image.");
            }

            return convertView;
        }
        // View Holder pattern used for Smooth Scrolling. As View Holder pattern recycle the findViewById() object.
        class ViewHolder {
            private ImageView picturesView;
        }
    }

    private static double getScaleFactor(int bitmapWidth, int bitmapHeight) {
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
