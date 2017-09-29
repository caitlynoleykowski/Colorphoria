package com.caitoo0o.filler.backgroundtasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.caitoo0o.filler.R;
import com.caitoo0o.filler.activities.loggedinactivities.BrowseActivity;
import com.caitoo0o.filler.reusableuserinterface.PositionImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Cait on 9/22/2016.
 *
 * This task gets an image with a specified url. It first checks the internal app cache
 * to see if the image has already been downloaded. If it has it gets it from there. If not,
 * it gets it from the url.
 */
public class GetBrowseThumbnailImageAsyncTask extends AsyncTask<Integer, Void, Bitmap> {
    private static final String TAG = GetBrowseThumbnailImageAsyncTask.class.getName();

    private WeakReference<Context> contextWeakReference;
    private WeakReference<PositionImageView> imageViewWeakReference;
    private WeakReference<ProgressBar> progressBarWeakReference;
    private WeakReference<ArrayList<File>> filesWeakReference;
    private int position;

    public GetBrowseThumbnailImageAsyncTask(Context context, PositionImageView imageView, ProgressBar progressBar,
                                            ArrayList<File> files, int position) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        contextWeakReference = new WeakReference<Context>(context);
        imageViewWeakReference = new WeakReference<PositionImageView>(imageView);
        progressBarWeakReference = new WeakReference<ProgressBar>(progressBar);
        filesWeakReference = new WeakReference<ArrayList<File>>(files);
        this.position = position;
    }

    // Apply effect to image in background
    @Override
    protected Bitmap doInBackground(Integer... params) {
        // Must loop here in case the file at the current index is corrupted/not found/can't be read
        Bitmap bm = null;
        while (bm == null) {
            ArrayList<File> files = filesWeakReference == null? null : filesWeakReference.get();
            Context context = contextWeakReference == null? null : contextWeakReference.get();
            // Don't need this image view below in this function but no point to wasting effort doing
            // this work if the image view we want to populate is not even around anymore
            ImageView imageView = imageViewWeakReference == null? null : imageViewWeakReference.get();
            if (files != null && context != null && imageView != null) {

                int filesIndex = BrowseActivity.getIdxFromPos(files, position);
                if (filesIndex < files.size()) {
                    Uri uri = Uri.fromFile(files.get(filesIndex));
                    InputStream is;
                    try {
                        is = context.getContentResolver().openInputStream(uri);
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(is, null, options);
                        double scaleFactor = BrowseActivity.getThumbnailScaleFactor(options.outWidth, options.outHeight);
                        BitmapFactory.Options finalOptions = new BitmapFactory.Options();
                        finalOptions.inSampleSize = (int) Math.ceil(1 / scaleFactor);
                        InputStream finalInputStream = context.getContentResolver().openInputStream(uri);
                        bm = BitmapFactory.decodeStream(finalInputStream, null, finalOptions);

                        // If bitmap is null, the image couldn't load (i.e. bad/corrupted file), so
                        // remove it from the list of files and move to the next one
                        if (bm == null) {
                            files.remove(filesIndex);
                            continue;
                        }

                        return bm;
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found to display thumbnail of image.");
                        files.remove(filesIndex);
                        continue;
                    }
                } else {
                    // We're at the end of the files so just stop
                    // TODO: We can remove the progress bar below but this will leave an extra
                    // TODO: thumbnail item in the view at the bottom unpopulated (i.e. awkward extra space)
                }
            }
        }

        return null;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewWeakReference != null && bitmap != null) {
            final PositionImageView imageView = imageViewWeakReference.get();
            if (imageView != null && imageView.getPosition() == position) {
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (progressBarWeakReference != null) {
                    ProgressBar progressBar = progressBarWeakReference.get();
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}