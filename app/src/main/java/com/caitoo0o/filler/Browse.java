package com.caitoo0o.filler;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Browse extends AppCompatActivity {
    private File[] allFiles;

    private static final int MAX_THUMBNAIL_PIXELS = 50000;

    public static final String IMAGE_URI_STATE_KEY = "IMAGE_URI_STATE_KEY";

    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        if (savedInstanceState != null) {
            mImageUri = (Uri) savedInstanceState.getParcelable(IMAGE_URI_STATE_KEY);
        }
        else if (getIntent() != null && getIntent().hasExtra(IMAGE_URI_STATE_KEY)) {
            mImageUri = (Uri) getIntent().getParcelableExtra(IMAGE_URI_STATE_KEY);
        }

        View outerRelativeLayout = findViewById(R.id.outer_relative_layout);
        outerRelativeLayout.setOnTouchListener(new OnSwipeTouchListener(Browse.this) {
            public void onSwipeTop() {
                Toast.makeText(Browse.this, "top", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeRight() {
                Toast.makeText(Browse.this, "right", Toast.LENGTH_SHORT).show();
                startPicEditActivity(mImageUri);
            }

            public void onSwipeLeft() {
                Toast.makeText(Browse.this, "left", Toast.LENGTH_SHORT).show();
            }

            public void onSwipeBottom() {
                Toast.makeText(Browse.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });

        //Retriving Images from Database(SD CARD) by Cursor.
        File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File albumStorageDir = new File(pictureStorageDir, "Filler");
        allFiles = albumStorageDir.listFiles(); // todo add a file filter to make sure all files are images
        //Uri uri = Uri.fromFile(albumStorageDir);
        //cursor = getContentResolver().query(uri, list, null, null, MediaStore.Images.Thumbnails._ID);
        //columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

        GridView sdcardimage = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext());
        sdcardimage.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View v, int pos,
                                    long arg3) {
                // dont need but what is it??
                Object i = adapterView.getItemAtPosition(pos);
                // Load the image that was clicked into the editor
                Intent intent = new Intent(Browse.this, PicEditActivity.class);
                intent.putExtra(PicEditActivity.SOURCE_URI, Uri.fromFile(allFiles[getIdxFromPos(pos)]));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        sdcardimage.setAdapter(adapter);
    }

    // Used to get the index into the allFiles list in order to retrieve the files in order of most recent first
    private int getIdxFromPos(int pos) {
        return allFiles.length - pos - 1;
    }

    private void startPicEditActivity(Uri uri) {
        Intent intent = new Intent(this, PicEditActivity.class);
        intent.putExtra(PicEditActivity.SOURCE_URI, uri);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    // Adapter for Grid View
    private class ImageAdapter extends BaseAdapter {

        private Context context;

        public ImageAdapter(Context localContext) {
            context = localContext;
        }

        public int getCount() {
            return allFiles.length;
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

            Uri uri = Uri.fromFile(allFiles[getIdxFromPos(position)]);
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
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_URI_STATE_KEY, mImageUri);
    }
}
