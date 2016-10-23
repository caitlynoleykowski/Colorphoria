package com.caitoo0o.filler;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Browse extends AppCompatActivity {
//cursor and columnIndex defined Globally so we can use it in below class.
    private Cursor cursor;
    private int columnIndex;
    private File[] allFiles;

    private static final int MAX_THUMBNAIL_PIXELS = 50000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);

        //Searching Images ID's from Gallery. _ID is the Default id code for all. You can retrive image,contacts,music id in the same way.
        //String[] list = {MediaStore.Images.Media._ID};

        //Retriving Images from Database(SD CARD) by Cursor.
        File pictureStorageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File albumStorageDir = new File(pictureStorageDir, "Filler");
        allFiles = albumStorageDir.listFiles();
        //Uri uri = Uri.fromFile(albumStorageDir);
        //cursor = getContentResolver().query(uri, list, null, null, MediaStore.Images.Thumbnails._ID);
        //columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);

        GridView sdcardimage = (GridView) findViewById(R.id.gridview);
        ImageAdapter adapter = new ImageAdapter(getApplicationContext());
        sdcardimage.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View v, int pos,
                                    long arg3) {
                Object i = adapterView.getItemAtPosition(pos);
                if (v instanceof ImageView) {
                    Log.i("browse", "toot toot!");
                }
                Log.i("browse", "poop poop!!");
            }
        });
        sdcardimage.setAdapter(adapter);
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

            Uri uri = Uri.fromFile(allFiles[position]);
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
}
